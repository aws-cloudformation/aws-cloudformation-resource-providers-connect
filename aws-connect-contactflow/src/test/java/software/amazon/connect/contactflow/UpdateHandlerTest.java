package software.amazon.connect.contactflow;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowContentRequest;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowContentResponse;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowMetadataRequest;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowMetadataResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.CONTACT_FLOW_ARN;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.CONTACT_FLOW_NAME;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.FLOW_CONTENT;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.FLOW_STATE_ACTIVE;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.TAGS_ONE;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.TAGS_SET_ONE;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.TAGS_TWO;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.VALID_TAG_KEY_THREE;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.buildContactFlowDesiredStateResourceModel;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.buildContactFlowPreviousStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private UpdateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success() {
        final ArgumentCaptor<UpdateContactFlowContentRequest> updateContactFlowContentRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateContactFlowContentRequest.class);
        final ArgumentCaptor<UpdateContactFlowMetadataRequest> updateContactFlowMetadataRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateContactFlowMetadataRequest.class);

        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_THREE);

        final UpdateContactFlowContentResponse updateContactFlowContentResponse = UpdateContactFlowContentResponse.builder().build();
        when(proxyClient.client().updateContactFlowContent(updateContactFlowContentRequestArgumentCaptor.capture())).thenReturn(updateContactFlowContentResponse);

        final UpdateContactFlowMetadataResponse updateContactFlowMetadataResponse = UpdateContactFlowMetadataResponse.builder().build();
        when(proxyClient.client().updateContactFlowMetadata(updateContactFlowMetadataRequestArgumentCaptor.capture())).thenReturn(updateContactFlowMetadataResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowDesiredStateResourceModel())
                .previousResourceState(buildContactFlowPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateContactFlowMetadata(updateContactFlowMetadataRequestArgumentCaptor.capture());
        assertThat(updateContactFlowMetadataRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateContactFlowMetadataRequestArgumentCaptor.getValue().contactFlowId()).isEqualTo(CONTACT_FLOW_ARN);

        verify(proxyClient.client()).updateContactFlowContent(updateContactFlowContentRequestArgumentCaptor.capture());
        assertThat(updateContactFlowContentRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateContactFlowContentRequestArgumentCaptor.getValue().contactFlowId()).isEqualTo(CONTACT_FLOW_ARN);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(CONTACT_FLOW_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(CONTACT_FLOW_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(connectClient, times(4)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateDescriptionNull() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .contactFlowArn(CONTACT_FLOW_ARN)
                .name(CONTACT_FLOW_NAME)
                .content(FLOW_CONTENT)
                .description(null)
                .tags(TAGS_SET_ONE)
                .state(FLOW_STATE_ACTIVE)
                .build();

        final ArgumentCaptor<UpdateContactFlowMetadataRequest> updateContactFlowMetadataRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateContactFlowMetadataRequest.class);
        final ArgumentCaptor<UpdateContactFlowContentRequest> updateContactFlowContentRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateContactFlowContentRequest.class);

        final UpdateContactFlowMetadataResponse updateContactFlowMetadataResponse = UpdateContactFlowMetadataResponse.builder().build();
        when(proxyClient.client().updateContactFlowMetadata(updateContactFlowMetadataRequestArgumentCaptor.capture())).thenReturn(updateContactFlowMetadataResponse);

        final UpdateContactFlowContentResponse updateContactFlowContentResponse = UpdateContactFlowContentResponse.builder().build();
        when(proxyClient.client().updateContactFlowContent(updateContactFlowContentRequestArgumentCaptor.capture())).thenReturn(updateContactFlowContentResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildContactFlowPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateContactFlowMetadata(updateContactFlowMetadataRequestArgumentCaptor.capture());
        assertThat(updateContactFlowMetadataRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateContactFlowMetadataRequestArgumentCaptor.getValue().contactFlowId()).isEqualTo(CONTACT_FLOW_ARN);

        verify(proxyClient.client()).updateContactFlowContent(updateContactFlowContentRequestArgumentCaptor.capture());
        assertThat(updateContactFlowContentRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateContactFlowContentRequestArgumentCaptor.getValue().contactFlowId()).isEqualTo(CONTACT_FLOW_ARN);

        verify(connectClient, times(2)).serviceName();
    }
}
