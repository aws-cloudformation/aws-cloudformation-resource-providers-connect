package software.amazon.connect.trafficdistributiongroup;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.DeleteTrafficDistributionGroupResponse;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupResponse;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroup;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroupStatus;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.EXPECTED_CALLBACK_DELAY_SECONDS;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.TDG_ARN;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.buildDeleteTrafficDistributionGroupDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {
    private DeleteHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new DeleteHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    void testHandleRequest_Success_DeleteTrafficDistributionGroup() {
        final ArgumentCaptor<DeleteTrafficDistributionGroupRequest> deleteTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DeleteTrafficDistributionGroupRequest.class);
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest> describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        ResourceModel desiredResourceState = buildDeleteTrafficDistributionGroupDesiredStateResourceModel();

        final DeleteTrafficDistributionGroupResponse deleteTrafficDistributionGroupResponse = DeleteTrafficDistributionGroupResponse.builder().build();

        final DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponse = DescribeTrafficDistributionGroupResponse.builder()
                .trafficDistributionGroup(TrafficDistributionGroup.builder()
                        .arn(desiredResourceState.getTrafficDistributionGroupArn())
                        .status(TrafficDistributionGroupStatus.ACTIVE)
                        .build())
                .build();

        when(proxyClient.client().deleteTrafficDistributionGroup(deleteTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(deleteTrafficDistributionGroupResponse);

        when(proxyClient.client().describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(describeTrafficDistributionGroupResponse)
                .thenThrow(ResourceNotFoundException.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceState)
                .region("us-west-2")
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(EXPECTED_CALLBACK_DELAY_SECONDS);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).deleteTrafficDistributionGroup(deleteTrafficDistributionGroupRequestArgumentCaptor.capture());
        assertThat(deleteTrafficDistributionGroupRequestArgumentCaptor.getValue().trafficDistributionGroupId()).isEqualTo(desiredResourceState.getTrafficDistributionGroupArn());

        verify(connectClient, times(4)).serviceName();
    }

    @Test
    void testHandleRequest_Exception_DeleteTrafficDistributionGroup() {
        final ArgumentCaptor<DeleteTrafficDistributionGroupRequest> deleteTrafficDistributionGroupRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteTrafficDistributionGroupRequest.class);
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest> describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        ResourceModel desiredResourceState = buildDeleteTrafficDistributionGroupDesiredStateResourceModel();

        final DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponse = DescribeTrafficDistributionGroupResponse.builder()
                .trafficDistributionGroup(TrafficDistributionGroup.builder()
                        .arn(desiredResourceState.getTrafficDistributionGroupArn())
                        .status(TrafficDistributionGroupStatus.ACTIVE)
                        .build())
                .build();

        when(proxyClient.client().describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(describeTrafficDistributionGroupResponse);

        when(proxyClient.client().deleteTrafficDistributionGroup(deleteTrafficDistributionGroupRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceState)
                .region("us-west-2")
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, times(2)).serviceName();
    }

    @Test
    void testHandleRequest_Exception_DeleteTrafficDistributionGroupFailure() {
        final ArgumentCaptor<DeleteTrafficDistributionGroupRequest> deleteTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DeleteTrafficDistributionGroupRequest.class);
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest> describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        final DeleteTrafficDistributionGroupResponse deleteTrafficDistributionGroupResponse = DeleteTrafficDistributionGroupResponse.builder().build();

        ResourceModel desiredResourceState = buildDeleteTrafficDistributionGroupDesiredStateResourceModel();

        final DescribeTrafficDistributionGroupResponse activeStateDescribeTrafficDistributionGroupResponse = DescribeTrafficDistributionGroupResponse.builder()
                .trafficDistributionGroup(TrafficDistributionGroup.builder()
                        .arn(desiredResourceState.getTrafficDistributionGroupArn())
                        .status(TrafficDistributionGroupStatus.ACTIVE)
                        .build())
                .build();
        final DescribeTrafficDistributionGroupResponse failedStateDescribeTrafficDistributionGroupResponse = DescribeTrafficDistributionGroupResponse.builder()
                .trafficDistributionGroup(TrafficDistributionGroup.builder()
                        .arn(desiredResourceState.getTrafficDistributionGroupArn())
                        .status(TrafficDistributionGroupStatus.DELETION_FAILED)
                        .build())
                .build();

        when(proxyClient.client().deleteTrafficDistributionGroup(deleteTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(deleteTrafficDistributionGroupResponse);

        when(proxyClient.client().describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(activeStateDescribeTrafficDistributionGroupResponse)
                .thenReturn(failedStateDescribeTrafficDistributionGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceState)
                .region("us-west-2")
                .build();

        assertThrows(CfnNotStabilizedException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, times(2)).serviceName();
    }

    @Test
    void testHandleRequest_Success_PendingDeletionToResourceNotFoundException() {
        final ArgumentCaptor<DeleteTrafficDistributionGroupRequest> deleteTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DeleteTrafficDistributionGroupRequest.class);
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest> describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        final DeleteTrafficDistributionGroupResponse deleteTrafficDistributionGroupResponse = DeleteTrafficDistributionGroupResponse.builder().build();

        ResourceModel desiredResourceState = buildDeleteTrafficDistributionGroupDesiredStateResourceModel();

        final DescribeTrafficDistributionGroupResponse activeStateDescribeTrafficDistributionGroupResponse = DescribeTrafficDistributionGroupResponse.builder()
                .trafficDistributionGroup(TrafficDistributionGroup.builder()
                        .arn(desiredResourceState.getTrafficDistributionGroupArn())
                        .status(TrafficDistributionGroupStatus.ACTIVE)
                        .build())
                .build();
        final DescribeTrafficDistributionGroupResponse pendingDeletionResponse =
                DescribeTrafficDistributionGroupResponse.builder()
                        .trafficDistributionGroup(TrafficDistributionGroup.builder()
                                .status(TrafficDistributionGroupStatus.PENDING_DELETION)
                                .build())
                        .build();

        when(proxyClient.client().deleteTrafficDistributionGroup(deleteTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(deleteTrafficDistributionGroupResponse);

        when(proxyClient.client().describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(activeStateDescribeTrafficDistributionGroupResponse)
                .thenReturn(pendingDeletionResponse)
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceState)
                .region("us-west-2")
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(EXPECTED_CALLBACK_DELAY_SECONDS);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).deleteTrafficDistributionGroup(deleteTrafficDistributionGroupRequestArgumentCaptor.capture());
        assertThat(deleteTrafficDistributionGroupRequestArgumentCaptor.getValue().trafficDistributionGroupId())
                .isEqualTo(desiredResourceState.getTrafficDistributionGroupArn());

        verify(connectClient, times(4)).serviceName();
    }
}
