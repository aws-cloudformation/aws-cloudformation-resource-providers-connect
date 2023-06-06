package software.amazon.connect.prompt;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.awssdk.services.connect.model.UpdatePromptRequest;
import software.amazon.awssdk.services.connect.model.UpdatePromptResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.prompt.PromptTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.INSTANCE_ARN_TWO;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_DESCRIPTION_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_DESCRIPTION_TWO;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_NAME_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_NAME_TWO;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_S3URI_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_S3URI_TWO;
import static software.amazon.connect.prompt.PromptTestDataProvider.RES_STATE_TAGS_SET_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.SYSTEM_TAGS_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.TAGS_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.TAGS_SET_TWO;
import static software.amazon.connect.prompt.PromptTestDataProvider.TAGS_THREE;
import static software.amazon.connect.prompt.PromptTestDataProvider.TAGS_TWO;
import static software.amazon.connect.prompt.PromptTestDataProvider.VALID_TAG_KEY_THREE;
import static software.amazon.connect.prompt.PromptTestDataProvider.VALID_TAG_KEY_TWO;
import static software.amazon.connect.prompt.PromptTestDataProvider.VALID_TAG_VALUE_THREE;
import static software.amazon.connect.prompt.PromptTestDataProvider.buildPromptDesiredStateResourceModel;
import static software.amazon.connect.prompt.PromptTestDataProvider.buildPromptPreviousStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    private UpdateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void tear_down() {
        verifyNoMoreInteractions(connectClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ArgumentCaptor<UpdatePromptRequest> updatePromptRequestArgumentCaptor = ArgumentCaptor.forClass(UpdatePromptRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdatePromptResponse updatePromptResponse = UpdatePromptResponse.builder().build();
        when(proxyClient.client().updatePrompt(updatePromptRequestArgumentCaptor.capture())).thenReturn(updatePromptResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPromptDesiredStateResourceModel())
                .previousResourceState(buildPromptPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .previousSystemTags(SYSTEM_TAGS_ONE)
                .systemTags(SYSTEM_TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        responseAssertionHelper(response, request);

        verify(proxyClient.client()).updatePrompt(updatePromptRequestArgumentCaptor.capture());
        updatePromptValidationHelper(updatePromptRequestArgumentCaptor);

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(PROMPT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(PROMPT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(3)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateDescriptionNull() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .promptArn(PROMPT_ARN)
                .instanceArn(INSTANCE_ARN)
                .name(PROMPT_NAME_TWO)
                .description(null)
                .s3Uri(PROMPT_S3URI_ONE)
                .tags(RES_STATE_TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdatePromptRequest> updatePromptRequestArgumentCaptor = ArgumentCaptor.forClass(UpdatePromptRequest.class);

        final UpdatePromptResponse updatePromptResponseResponse = UpdatePromptResponse.builder().build();
        when(proxyClient.client().updatePrompt(updatePromptRequestArgumentCaptor.capture())).thenReturn(updatePromptResponseResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildPromptPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        responseAssertionHelper(response, request);

        verify(proxyClient.client()).updatePrompt(updatePromptRequestArgumentCaptor.capture());
        assertThat(updatePromptRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updatePromptRequestArgumentCaptor.getValue().promptId()).isEqualTo(PROMPT_ARN);
        assertThat(updatePromptRequestArgumentCaptor.getValue().name()).isEqualTo(PROMPT_NAME_TWO);
        assertThat(updatePromptRequestArgumentCaptor.getValue().description()).isEqualTo("");
        assertThat(updatePromptRequestArgumentCaptor.getValue().s3Uri()).isEqualTo(PROMPT_S3URI_ONE);

        verify(connectClient, atMostOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateInstanceArn() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN_TWO)
                .promptArn(PROMPT_ARN)
                .name(PROMPT_NAME_TWO)
                .description(PROMPT_DESCRIPTION_TWO)
                .s3Uri(PROMPT_S3URI_TWO)
                .tags(TAGS_SET_TWO)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildPromptPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdatePrompt() {
        final ArgumentCaptor<UpdatePromptRequest> updatePromptRequestArgumentCaptor = ArgumentCaptor.forClass(UpdatePromptRequest.class);

        when(proxyClient.client().updatePrompt(updatePromptRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPromptDesiredStateResourceModel())
                .previousResourceState(buildPromptPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updatePrompt(updatePromptRequestArgumentCaptor.capture());
        updatePromptValidationHelper(updatePromptRequestArgumentCaptor);

        verify(connectClient, atMostOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_TagResource() {
        final ArgumentCaptor<UpdatePromptRequest> updatePromptRequestArgumentCaptor = ArgumentCaptor.forClass(UpdatePromptRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);

        final Map<String, String> tagsAdded = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);

        final UpdatePromptResponse updatePromptResponse = UpdatePromptResponse.builder().build();
        when(proxyClient.client().updatePrompt(updatePromptRequestArgumentCaptor.capture())).thenReturn(updatePromptResponse);

        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPromptDesiredStateResourceModel())
                .previousResourceState(buildPromptPreviousStateResourceModel())
                .desiredResourceTags(TAGS_THREE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updatePrompt(updatePromptRequestArgumentCaptor.capture());
        updatePromptValidationHelper(updatePromptRequestArgumentCaptor);

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(PROMPT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(tagsAdded);

        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UnTagResource() {
        final ArgumentCaptor<UpdatePromptRequest> updatePromptRequestArgumentCaptor = ArgumentCaptor.forClass(UpdatePromptRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdatePromptResponse updatePromptResponse = UpdatePromptResponse.builder().build();
        when(proxyClient.client().updatePrompt(updatePromptRequestArgumentCaptor.capture())).thenReturn(updatePromptResponse);

        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPromptDesiredStateResourceModel())
                .previousResourceState(buildPromptPreviousStateResourceModel())
                .desiredResourceTags(ImmutableMap.of())
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updatePrompt(updatePromptRequestArgumentCaptor.capture());
        updatePromptValidationHelper(updatePromptRequestArgumentCaptor);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(PROMPT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(2)).serviceName();
    }

    private void responseAssertionHelper(ProgressEvent<ResourceModel, CallbackContext> updatePromptResponse, ResourceHandlerRequest<ResourceModel> request) {
        assertThat(updatePromptResponse).isNotNull();
        assertThat(updatePromptResponse.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(updatePromptResponse.getCallbackContext()).isNull();
        assertThat(updatePromptResponse.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(updatePromptResponse.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(updatePromptResponse.getResourceModels()).isNull();
        assertThat(updatePromptResponse.getMessage()).isNull();
        assertThat(updatePromptResponse.getErrorCode()).isNull();
    }

    private void updatePromptValidationHelper(ArgumentCaptor<UpdatePromptRequest> updatePromptCaptor) {
        assertThat(updatePromptCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updatePromptCaptor.getValue().promptId()).isEqualTo(PROMPT_ARN);
        assertThat(updatePromptCaptor.getValue().name()).isEqualTo(PROMPT_NAME_ONE);
        assertThat(updatePromptCaptor.getValue().description()).isEqualTo(PROMPT_DESCRIPTION_ONE);
        assertThat(updatePromptCaptor.getValue().s3Uri()).isEqualTo(PROMPT_S3URI_ONE);
    }
}
