package software.amazon.connect.contactflowmodule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowModuleContentRequest;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowModuleContentResponse;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowModuleMetadataRequest;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowModuleMetadataResponse;
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
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.CONTACT_FLOW_MODULE_ARN;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.CONTACT_FLOW_MODULE_ID;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.CONTACT_FLOW_MODULE_NAME;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.FLOW_MODULE_CONTENT;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.TAGS_ONE;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.TAGS_SET_ONE;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.TAGS_TWO;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.VALID_TAG_KEY_THREE;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.buildContactFlowModuleDesiredStateResourceModel;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.buildContactFlowModulePreviousStateResourceModel;

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
        final ArgumentCaptor<UpdateContactFlowModuleContentRequest> updateContactFlowModuleContentRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateContactFlowModuleContentRequest.class);
        final ArgumentCaptor<UpdateContactFlowModuleMetadataRequest> updateContactFlowModuleMetadataRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateContactFlowModuleMetadataRequest.class);

        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_THREE);

        final UpdateContactFlowModuleContentResponse updateContactFlowModuleContentResponse = UpdateContactFlowModuleContentResponse.builder().build();
        when(proxyClient.client().updateContactFlowModuleContent(updateContactFlowModuleContentRequestArgumentCaptor.capture())).thenReturn(updateContactFlowModuleContentResponse);

        final UpdateContactFlowModuleMetadataResponse updateContactFlowModuleMetadataResponse = UpdateContactFlowModuleMetadataResponse.builder().build();
        when(proxyClient.client().updateContactFlowModuleMetadata(updateContactFlowModuleMetadataRequestArgumentCaptor.capture())).thenReturn(updateContactFlowModuleMetadataResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowModuleDesiredStateResourceModel())
                .previousResourceState(buildContactFlowModulePreviousStateResourceModel())
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

        verify(proxyClient.client()).updateContactFlowModuleMetadata(updateContactFlowModuleMetadataRequestArgumentCaptor.capture());
        assertThat(updateContactFlowModuleMetadataRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateContactFlowModuleMetadataRequestArgumentCaptor.getValue().contactFlowModuleId()).isEqualTo(CONTACT_FLOW_MODULE_ARN);

        verify(proxyClient.client()).updateContactFlowModuleContent(updateContactFlowModuleContentRequestArgumentCaptor.capture());
        assertThat(updateContactFlowModuleContentRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateContactFlowModuleContentRequestArgumentCaptor.getValue().contactFlowModuleId()).isEqualTo(CONTACT_FLOW_MODULE_ARN);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(CONTACT_FLOW_MODULE_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(CONTACT_FLOW_MODULE_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(connectClient, times(4)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateDescriptionNull() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .contactFlowModuleArn(CONTACT_FLOW_MODULE_ARN)
                .name(CONTACT_FLOW_MODULE_NAME)
                .content(FLOW_MODULE_CONTENT)
                .description(null)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateContactFlowModuleMetadataRequest> updateContactFlowModuleMetadataRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateContactFlowModuleMetadataRequest.class);
        final ArgumentCaptor<UpdateContactFlowModuleContentRequest> updateContactFlowModuleContentRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateContactFlowModuleContentRequest.class);

        final UpdateContactFlowModuleMetadataResponse updateContactFlowModuleMetadataResponse = UpdateContactFlowModuleMetadataResponse.builder().build();
        when(proxyClient.client().updateContactFlowModuleMetadata(updateContactFlowModuleMetadataRequestArgumentCaptor.capture())).thenReturn(updateContactFlowModuleMetadataResponse);

        final UpdateContactFlowModuleContentResponse updateContactFlowModuleContentResponse = UpdateContactFlowModuleContentResponse.builder().build();
        when(proxyClient.client().updateContactFlowModuleContent(updateContactFlowModuleContentRequestArgumentCaptor.capture())).thenReturn(updateContactFlowModuleContentResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildContactFlowModulePreviousStateResourceModel())
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

        verify(proxyClient.client()).updateContactFlowModuleMetadata(updateContactFlowModuleMetadataRequestArgumentCaptor.capture());
        assertThat(updateContactFlowModuleMetadataRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateContactFlowModuleMetadataRequestArgumentCaptor.getValue().contactFlowModuleId()).isEqualTo(CONTACT_FLOW_MODULE_ARN);

        verify(proxyClient.client()).updateContactFlowModuleContent(updateContactFlowModuleContentRequestArgumentCaptor.capture());
        assertThat(updateContactFlowModuleContentRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateContactFlowModuleContentRequestArgumentCaptor.getValue().contactFlowModuleId()).isEqualTo(CONTACT_FLOW_MODULE_ARN);

        verify(connectClient, times(2)).serviceName();
    }
}
