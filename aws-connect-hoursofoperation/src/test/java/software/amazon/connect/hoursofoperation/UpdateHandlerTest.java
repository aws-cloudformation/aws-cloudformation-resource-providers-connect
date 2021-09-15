package software.amazon.connect.hoursofoperation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.awssdk.services.connect.model.UpdateHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.UpdateHoursOfOperationResponse;
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
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_CONFIG_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_CONFIG_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_DESCRIPTION_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_DESCRIPTION_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_NAME_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_NAME_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INSTANCE_ARN_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_SET_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_THREE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.VALID_TAG_KEY_THREE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.VALID_TAG_KEY_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.VALID_TAG_VALUE_THREE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHoursOfOperationDesiredStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHoursOfOperationPreviousStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getConfig;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.validateConfig;

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
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
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

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(3)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateDescriptionNull() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .instanceArn(INSTANCE_ARN)
                .name(HOURS_OF_OPERATION_NAME_TWO)
                .description(null)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
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

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_TWO);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo("");
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(connectClient, atMostOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateInstanceArn() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .instanceArn(INSTANCE_ARN_TWO)
                .name(HOURS_OF_OPERATION_NAME_TWO)
                .description(HOURS_OF_OPERATION_DESCRIPTION_TWO)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .tags(TAGS_SET_ONE)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateHoursOfOperation() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(connectClient, atMostOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_TagResource() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);

        final Map<String, String> tagsAdded = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(TAGS_THREE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(tagsAdded);

        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UnTagResource() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(ImmutableMap.of())
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(2)).serviceName();
    }
}
