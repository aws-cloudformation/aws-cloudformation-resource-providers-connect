package software.amazon.connect.trafficdistributiongroup;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.TAGS_ONE;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.TAGS_THREE;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.TAGS_TWO;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.buildUpdateTrafficDistributionGroupDesiredStateResourceModelWithDifferentKeys;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.buildUpdateTrafficDistributionGroupDesiredStateResourceModelWithSameKeys;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.buildUpdateTrafficDistributionGroupPreviousStateResourceModel;

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
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = mock(AmazonWebServicesClientProxy.class);
        final Credentials MOCK_CREDENTIALS =
                new Credentials("accessKey", "secretKey", "token");

        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testhandleRequest_SuccessForSameKeys_TagResource() {
        final ArgumentCaptor<TagResourceRequest>
                tagResourceRequestArgumentCaptor =
                ArgumentCaptor.forClass(TagResourceRequest.class);

        final ResourceModel desiredModel = buildUpdateTrafficDistributionGroupDesiredStateResourceModelWithSameKeys();
        final ResourceModel previousModel = buildUpdateTrafficDistributionGroupPreviousStateResourceModel();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .previousResourceTags(TAGS_ONE)
                .desiredResourceTags(TAGS_TWO)
                .region("us-west-2")
            .build();

        when(proxyClient.client()
                .tagResource(tagResourceRequestArgumentCaptor.capture()))
                .thenReturn(TagResourceResponse.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(
                        proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(tagResourceRequestArgumentCaptor.getValue().tags())
                .isEqualTo(TagHelper.convertToMap(desiredModel.getTags()));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client())
                .tagResource(tagResourceRequestArgumentCaptor.capture());
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testhandleRequest_SuccessForDifferentKeys_TagResource() {
        final ArgumentCaptor<UntagResourceRequest>
                untagResourceRequestArgumentCaptor =
                ArgumentCaptor.forClass(UntagResourceRequest.class);
        final ArgumentCaptor<TagResourceRequest>
                tagResourceRequestArgumentCaptor =
                ArgumentCaptor.forClass(TagResourceRequest.class);

        final ResourceModel desiredModel =
                buildUpdateTrafficDistributionGroupDesiredStateResourceModelWithDifferentKeys();
        final ResourceModel previousModel = buildUpdateTrafficDistributionGroupPreviousStateResourceModel();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .previousResourceTags(TAGS_ONE)
                .desiredResourceTags(TAGS_THREE)
                .region("us-west-2")
                .build();

        when(proxyClient.client()
                .untagResource(untagResourceRequestArgumentCaptor.capture()))
                .thenReturn(UntagResourceResponse.builder().build());
        when(proxyClient.client()
                .tagResource(tagResourceRequestArgumentCaptor.capture()))
                .thenReturn(TagResourceResponse.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(
                        proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys().size()).isEqualTo(2);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys())
                .containsAll(TagHelper.convertToMap(previousModel.getTags()).keySet());
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags())
                .isEqualTo(TagHelper.convertToMap(desiredModel.getTags()));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client())
                .tagResource(tagResourceRequestArgumentCaptor.capture());
        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_TagResource() {
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor =
                ArgumentCaptor.forClass(TagResourceRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUpdateTrafficDistributionGroupDesiredStateResourceModelWithSameKeys())
                .previousResourceState(buildUpdateTrafficDistributionGroupPreviousStateResourceModel())
                .previousResourceTags(TAGS_ONE)
                .desiredResourceTags(TAGS_TWO)
                .nextToken(null)
                .region("us-west-2")
                .build();

        when(proxyClient.client()
                .tagResource(tagResourceRequestArgumentCaptor.capture()))
                .thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(connectClient, times(1)).serviceName();
    }
}
