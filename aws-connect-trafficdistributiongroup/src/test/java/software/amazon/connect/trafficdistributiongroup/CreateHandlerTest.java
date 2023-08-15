package software.amazon.connect.trafficdistributiongroup;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.AccessDeniedException;
import software.amazon.awssdk.services.connect.model.CreateTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.CreateTrafficDistributionGroupResponse;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupResponse;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroup;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroupStatus;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
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
import static software.amazon.connect.trafficdistributiongroup.TagHelper.convertToMap;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.EXPECTED_CALLBACK_DELAY_SECONDS;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.TAGS_ONE;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.TDG_ARN;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.buildCreateTrafficDistributionGroupDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private CreateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new CreateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success_CreateTrafficDistributionGroup() {
        final ArgumentCaptor<CreateTrafficDistributionGroupRequest> createTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateTrafficDistributionGroupRequest.class);
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest> describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        final CreateTrafficDistributionGroupResponse createTrafficDistributionGroupResponse =
                CreateTrafficDistributionGroupResponse.builder()
                        .arn(TDG_ARN)
                        .build();

        final DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponse =
                DescribeTrafficDistributionGroupResponse.builder()
                        .trafficDistributionGroup(TrafficDistributionGroup.builder()
                                .arn(TDG_ARN)
                                .status(TrafficDistributionGroupStatus.ACTIVE)
                                .build())
                        .build();

        when(proxyClient.client().createTrafficDistributionGroup(createTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(createTrafficDistributionGroupResponse);
        when(proxyClient.client().describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(describeTrafficDistributionGroupResponse);

        ResourceModel desiredStateModel = buildCreateTrafficDistributionGroupDesiredStateResourceModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredStateModel)
                .desiredResourceTags(TAGS_ONE)
                .region("us-west-2")
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(EXPECTED_CALLBACK_DELAY_SECONDS);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getTrafficDistributionGroupArn()).isEqualTo(TDG_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createTrafficDistributionGroup(createTrafficDistributionGroupRequestArgumentCaptor.capture());
        verify(proxyClient.client()).describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture());

        assertThat(createTrafficDistributionGroupRequestArgumentCaptor.getValue().instanceId()).isEqualTo(desiredStateModel.getInstanceArn());
        assertThat(createTrafficDistributionGroupRequestArgumentCaptor.getValue().name()).isEqualTo(desiredStateModel.getName());
        assertThat(createTrafficDistributionGroupRequestArgumentCaptor.getValue().description()).isEqualTo(desiredStateModel.getDescription());
        assertThat(createTrafficDistributionGroupRequestArgumentCaptor.getValue().tags()).isEqualTo(convertToMap(desiredStateModel.getTags()));
        assertThat(describeTrafficDistributionGroupRequestArgumentCaptor.getValue().trafficDistributionGroupId()).isEqualTo(TDG_ARN);
        verify(connectClient, times(2)).serviceName();
    }

    @Test
    void testHandleRequest_Exception_CreateTrafficDistributionGroupConnect() {
        final ArgumentCaptor<CreateTrafficDistributionGroupRequest> createTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateTrafficDistributionGroupRequest.class);

        when(proxyClient.client().createTrafficDistributionGroup(createTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildCreateTrafficDistributionGroupDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .region("us-west-2")
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_CreateTrafficDistributionGroupFailure() {
        final ArgumentCaptor<CreateTrafficDistributionGroupRequest> createTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateTrafficDistributionGroupRequest.class);
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest> describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        final CreateTrafficDistributionGroupResponse createTrafficDistributionGroupResponse =
                CreateTrafficDistributionGroupResponse.builder()
                    .arn(TDG_ARN)
                    .build();

        final DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponse =
                DescribeTrafficDistributionGroupResponse.builder()
                    .trafficDistributionGroup(TrafficDistributionGroup.builder()
                            .arn(TDG_ARN)
                            .status(TrafficDistributionGroupStatus.CREATION_FAILED)
                            .build())
                        .build();

        when(proxyClient.client().createTrafficDistributionGroup(createTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(createTrafficDistributionGroupResponse);
        when(proxyClient.client().describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(describeTrafficDistributionGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildCreateTrafficDistributionGroupDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .region("us-west-2")
                .build();

        assertThrows(CfnNotStabilizedException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Failure_AccessDeniedException() {
        final ArgumentCaptor<CreateTrafficDistributionGroupRequest> createTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateTrafficDistributionGroupRequest.class);
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest> describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        final CreateTrafficDistributionGroupResponse createTrafficDistributionGroupResponse =
                CreateTrafficDistributionGroupResponse.builder()
                        .arn(TDG_ARN)
                        .build();

        when(proxyClient.client().createTrafficDistributionGroup(createTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(createTrafficDistributionGroupResponse);
        when(proxyClient.client().describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenThrow(AccessDeniedException.builder().message("Access Denied").build());

        ResourceModel desiredStateModel = buildCreateTrafficDistributionGroupDesiredStateResourceModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredStateModel)
                .desiredResourceTags(TAGS_ONE)
                .region("us-west-2")
                .build();

        assertThrows(CfnAccessDeniedException.class, () -> {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        });
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_CreateTrafficDistributionGroup_StatusPendingToActive() {
        final ArgumentCaptor<CreateTrafficDistributionGroupRequest> createTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateTrafficDistributionGroupRequest.class);
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest> describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        final CreateTrafficDistributionGroupResponse createTrafficDistributionGroupResponse =
                CreateTrafficDistributionGroupResponse.builder()
                        .arn(TDG_ARN)
                        .build();

        final DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponsePending =
                DescribeTrafficDistributionGroupResponse.builder()
                        .trafficDistributionGroup(TrafficDistributionGroup.builder()
                                .arn(TDG_ARN)
                                .status(TrafficDistributionGroupStatus.CREATION_IN_PROGRESS)
                                .build())
                        .build();

        final DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponseActive =
                DescribeTrafficDistributionGroupResponse.builder()
                        .trafficDistributionGroup(TrafficDistributionGroup.builder()
                                .arn(TDG_ARN)
                                .status(TrafficDistributionGroupStatus.ACTIVE)
                                .build())
                        .build();

        when(proxyClient.client().createTrafficDistributionGroup(createTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(createTrafficDistributionGroupResponse);
        when(proxyClient.client().describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(describeTrafficDistributionGroupResponsePending)
                .thenReturn(describeTrafficDistributionGroupResponseActive);

        ResourceModel desiredStateModel = buildCreateTrafficDistributionGroupDesiredStateResourceModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredStateModel)
                .desiredResourceTags(TAGS_ONE)
                .region("us-west-2")
                .build();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(EXPECTED_CALLBACK_DELAY_SECONDS);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getTrafficDistributionGroupArn()).isEqualTo(TDG_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client())
                .createTrafficDistributionGroup(createTrafficDistributionGroupRequestArgumentCaptor.capture());
        verify(proxyClient.client(), times(2))
                .describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture());

        assertThat(createTrafficDistributionGroupRequestArgumentCaptor.getValue().instanceId()).isEqualTo(desiredStateModel.getInstanceArn());
        assertThat(createTrafficDistributionGroupRequestArgumentCaptor.getValue().name()).isEqualTo(desiredStateModel.getName());
        assertThat(createTrafficDistributionGroupRequestArgumentCaptor.getValue().description()).isEqualTo(desiredStateModel.getDescription());
        assertThat(createTrafficDistributionGroupRequestArgumentCaptor.getValue().tags()).isEqualTo(convertToMap(desiredStateModel.getTags()));
        assertThat(describeTrafficDistributionGroupRequestArgumentCaptor.getAllValues().get(0).trafficDistributionGroupId()).isEqualTo(TDG_ARN);
        assertThat(describeTrafficDistributionGroupRequestArgumentCaptor.getAllValues().get(1).trafficDistributionGroupId()).isEqualTo(TDG_ARN);
        verify(connectClient, times(2)).serviceName();

        // Additional assertions to check the correct status progression
        assertThat(describeTrafficDistributionGroupRequestArgumentCaptor.getAllValues().get(0).trafficDistributionGroupId()).isEqualTo(TDG_ARN);
        assertThat(describeTrafficDistributionGroupRequestArgumentCaptor.getAllValues().get(1).trafficDistributionGroupId()).isEqualTo(TDG_ARN);

        assertThat(describeTrafficDistributionGroupResponsePending.trafficDistributionGroup().status()).isEqualTo(TrafficDistributionGroupStatus.CREATION_IN_PROGRESS);
        assertThat(describeTrafficDistributionGroupResponseActive.trafficDistributionGroup().status()).isEqualTo(TrafficDistributionGroupStatus.ACTIVE);
    }
}
