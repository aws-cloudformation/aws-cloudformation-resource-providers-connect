package software.amazon.connect.trafficdistributiongroup;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupResponse;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroup;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.ID;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.buildReadTrafficDistributionGroupDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private ReadHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        logger = new LoggerProxy();
        handler = new ReadHandler();
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
    public void testHandleRequest_Success_DescribeTrafficDistributionGroup() {
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest>
                describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        final ResourceModel model = buildReadTrafficDistributionGroupDesiredStateResourceModel();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region("us-west-2")
                .build();

        final DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponse =
                DescribeTrafficDistributionGroupResponse.builder()
                        .trafficDistributionGroup(TrafficDistributionGroup.builder()
                                .id(ID.toString())
                                .arn(model.getTrafficDistributionGroupArn())
                                .instanceArn(model.getInstanceArn())
                                .name(model.getName())
                                .description(model.getDescription())
                                .status(model.getStatus())
                                .tags(TagHelper.convertToMap(model.getTags()))
                                .isDefault(model.getIsDefault())
                                .build())
                        .build();

        when(proxyClient.client()
                .describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenReturn(describeTrafficDistributionGroupResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(
                        proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(describeTrafficDistributionGroupRequestArgumentCaptor.getValue().trafficDistributionGroupId())
                .isEqualTo(model.getTrafficDistributionGroupArn());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client())
                .describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture());
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_ResourceNotFoundException_DescribeTrafficDistributionGroup() {
        final ArgumentCaptor<DescribeTrafficDistributionGroupRequest>
                describeTrafficDistributionGroupRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribeTrafficDistributionGroupRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildReadTrafficDistributionGroupDesiredStateResourceModel())
                .region("us-west-2")
                .build();

        when(proxyClient.client()
                .describeTrafficDistributionGroup(describeTrafficDistributionGroupRequestArgumentCaptor.capture()))
                .thenThrow(ResourceNotFoundException.builder()
                        .build());

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(connectClient, times(1)).serviceName();
    }
}
