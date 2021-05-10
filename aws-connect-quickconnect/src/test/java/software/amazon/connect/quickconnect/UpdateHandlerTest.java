package software.amazon.connect.quickconnect;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.model.QuickConnectType;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.awssdk.services.connect.model.UpdateQuickConnectConfigRequest;
import software.amazon.awssdk.services.connect.model.UpdateQuickConnectConfigResponse;
import software.amazon.awssdk.services.connect.model.UpdateQuickConnectNameRequest;
import software.amazon.awssdk.services.connect.model.UpdateQuickConnectNameResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.CONTACT_FLOW_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.CONTACT_FLOW_ID_2;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.INSTANCE_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.PHONE_NUMBER;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.PHONE_NUMBER_2;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUEUE_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUEUE_ID_2;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_SET_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_SET_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.USER_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.USER_ID_2;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.VALID_TAG_KEY_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.VALID_TAG_KEY_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.VALID_TAG_VALUE_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeQueue;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeUser;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.getPhoneQuickConnectConfig;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private UpdateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;
    private ResourceModel desiredQueueModel;
    private ResourceModel currentUserModel;
    private ResourceModel currentPhoneNumberModel;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
        desiredQueueModel = buildQuickConnectResourceModelWithQuickConnectTypeQueue();
        currentUserModel = buildQuickConnectResourceModelWithQuickConnectTypeUser();
        currentPhoneNumberModel = buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber();
    }

    @AfterEach
    public void post_execute() {
        verify(connectClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success_ToTypeUserFromTypeQueue() {
        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
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

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_ONE);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_ONE);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().userId()).isEqualTo(USER_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);
    }

    @Test
    public void testHandleRequest_Success_ToTypePhoneNumberFromTypeQueue() {

        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
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

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_THREE);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_THREE);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig().phoneNumber()).isEqualTo(PHONE_NUMBER);

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);
    }

    @Test
    public void testHandleRequest_Success_ToTypeQueueFromTypeUser() {

        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
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

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_TWO);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_TWO);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().queueId()).isEqualTo(QUEUE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);
    }

    @Test
    public void testHandleRequest_Success_ToTypePhoneNumberFromTypeUser() {

        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
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

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_THREE);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_THREE);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig().phoneNumber()).isEqualTo(PHONE_NUMBER);

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);
    }

    @Test
    public void testHandleRequest_Success_ToTypeQueueFromTypePhoneNumber() {

        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
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

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_TWO);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_TWO);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().queueId()).isEqualTo(QUEUE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);
    }

    @Test
    public void testHandleRequest_Success_ToTypeUserFromTypePhoneNumber() {

        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
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

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_ONE);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_ONE);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().userId()).isEqualTo(USER_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);
    }

    @Test
    public void testHandleRequest_Success_UpdateNameOnly() {

        final QuickConnectConfig quickConnectConfigTypePhoneNumber = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.PHONE_NUMBER.toString())
                .phoneConfig(getPhoneQuickConnectConfig())
                .build();

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_TWO)
                .description(QUICK_CONNECT_DESCRIPTION_THREE)
                .quickConnectConfig(quickConnectConfigTypePhoneNumber)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
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

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_TWO);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_THREE);
    }

    @Test
    public void testHandleRequest_Success_UpdateDescriptionOnly() {

        final QuickConnectConfig quickConnectConfigTypePhoneNumber = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.PHONE_NUMBER.toString())
                .phoneConfig(getPhoneQuickConnectConfig())
                .build();

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_THREE)
                .description(QUICK_CONNECT_DESCRIPTION_TWO)
                .quickConnectConfig(quickConnectConfigTypePhoneNumber)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
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

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_THREE);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_TWO);
    }

    @Test
    public void testHandleRequest_Success_UpdateUserQuickConnect_UserID() {

        final QuickConnectConfig quickConnectConfigTypeUser = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.USER.toString())
                .userConfig(UserQuickConnectConfig
                        .builder()
                        .userId(USER_ID_2)
                        .contactFlowId(CONTACT_FLOW_ID)
                        .build())
                .build();

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_ONE)
                .description(QUICK_CONNECT_DESCRIPTION_ONE)
                .quickConnectConfig(quickConnectConfigTypeUser)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
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

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().userId()).isEqualTo(USER_ID_2);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();

    }

    @Test
    public void testHandleRequest_Success_UpdateUserQuickConnect_ContactFlowID() {
        final QuickConnectConfig quickConnectConfigTypeUser = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.USER.toString())
                .userConfig(UserQuickConnectConfig
                        .builder()
                        .userId(USER_ID)
                        .contactFlowId(CONTACT_FLOW_ID_2)
                        .build())
                .build();

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_ONE)
                .description(QUICK_CONNECT_DESCRIPTION_ONE)
                .quickConnectConfig(quickConnectConfigTypeUser)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
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

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID_2);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().userId()).isEqualTo(USER_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();
    }

    @Test
    public void testHandleRequest_Success_UpdateQueueQuickConnect_QueueID() {
        final QuickConnectConfig quickConnectConfigTypeQueue = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.QUEUE.toString())
                .queueConfig(QueueQuickConnectConfig
                        .builder()
                        .queueId(QUEUE_ID_2)
                        .contactFlowId(CONTACT_FLOW_ID)
                        .build())
                .build();

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_TWO)
                .description(QUICK_CONNECT_DESCRIPTION_TWO)
                .quickConnectConfig(quickConnectConfigTypeQueue)
                .tags(TAGS_SET_TWO)
                .build();

        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .desiredResourceTags(TAGS_TWO)
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

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().queueId()).isEqualTo(QUEUE_ID_2);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();
    }

    @Test
    public void testHandleRequest_Success_UpdateQueueQuickConnect_ContactFlowID() {
        final QuickConnectConfig quickConnectConfigTypeQueue = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.QUEUE.toString())
                .queueConfig(QueueQuickConnectConfig
                        .builder()
                        .queueId(QUEUE_ID)
                        .contactFlowId(CONTACT_FLOW_ID_2)
                        .build())
                .build();

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_TWO)
                .description(QUICK_CONNECT_DESCRIPTION_TWO)
                .quickConnectConfig(quickConnectConfigTypeQueue)
                .tags(TAGS_SET_TWO)
                .build();

        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .desiredResourceTags(TAGS_TWO)
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

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID_2);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().queueId()).isEqualTo(QUEUE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();
    }

    @Test
    public void testHandleRequest_Success_UpdatePhoneNumberQuickConnect_PhoneNumber() {
        final QuickConnectConfig quickConnectConfigTypePhoneNumber = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.PHONE_NUMBER.toString())
                .phoneConfig(PhoneNumberQuickConnectConfig
                        .builder()
                        .phoneNumber(PHONE_NUMBER_2)
                        .build())
                .build();

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_THREE)
                .description(QUICK_CONNECT_DESCRIPTION_THREE)
                .quickConnectConfig(quickConnectConfigTypePhoneNumber)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
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

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig().phoneNumber()).isEqualTo(PHONE_NUMBER_2);

    }

    @Test
    public void testHandleRequest_Exception_UpdateQuickConnectName() {
        final QuickConnectConfig quickConnectConfigTypePhoneNumber = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.PHONE_NUMBER.toString())
                .phoneConfig(getPhoneQuickConnectConfig())
                .build();

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_TWO)
                .description(QUICK_CONNECT_DESCRIPTION_THREE)
                .quickConnectConfig(quickConnectConfigTypePhoneNumber)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);

        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_TWO);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_THREE);
    }

    @Test
    public void testHandleRequest_Exception_UpdateQuickConnectConfig() {
        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .desiredResourceTags(TAGS_TWO)
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_ONE);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_ONE);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().userId()).isEqualTo(USER_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();
    }

    @Test
    public void testHandleRequest_Exception_TagResource() {
        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);

        final Map<String, String> tagsAdded = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .desiredResourceTags(TAGS_THREE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_ONE);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_ONE);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().userId()).isEqualTo(USER_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(tagsAdded);
    }

    @Test
    public void testHandleRequest_Exception_UnTagResource() {
        final ArgumentCaptor<UpdateQuickConnectNameRequest> updateQuickConnectNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectNameRequest.class);
        final ArgumentCaptor<UpdateQuickConnectConfigRequest> updateQuickConnectConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateQuickConnectConfigRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateQuickConnectNameResponse updateQuickConnectNameResponse = UpdateQuickConnectNameResponse.builder().build();
        when(proxyClient.client().updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectNameResponse);

        final UpdateQuickConnectConfigResponse updateQuickConnectConfigResponse = UpdateQuickConnectConfigResponse.builder().build();
        when(proxyClient.client().updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture())).thenReturn(updateQuickConnectConfigResponse);

        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
                .previousResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .desiredResourceTags(ImmutableMap.of())
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateQuickConnectName(updateQuickConnectNameRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_ONE);
        assertThat(updateQuickConnectNameRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_ONE);

        verify(proxyClient.client()).updateQuickConnectConfig(updateQuickConnectConfigRequestArgumentCaptor.capture());
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNotNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().userId()).isEqualTo(USER_ID);
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(updateQuickConnectConfigRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);
    }
}
