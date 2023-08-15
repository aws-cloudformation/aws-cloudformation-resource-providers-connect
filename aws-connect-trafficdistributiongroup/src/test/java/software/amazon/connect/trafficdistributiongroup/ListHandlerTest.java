package software.amazon.connect.trafficdistributiongroup;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ListTrafficDistributionGroupsRequest;
import software.amazon.awssdk.services.connect.model.ListTrafficDistributionGroupsResponse;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroupSummary;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.TDG_IS_DEFAULT;
import static software.amazon.connect.trafficdistributiongroup.TrafficDistributionDataProvider.buildListTrafficDistributionGroupDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    private ListHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        logger = new LoggerProxy();
        handler = new ListHandler();
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
    public void testHandleRequest_Success_ListTrafficDistributionGroups_WithResults() {
        final ResourceModel model = buildListTrafficDistributionGroupDesiredStateResourceModel();
        final UUID tdgSummary1Id = UUID.randomUUID();
        final String tdgArn1 = "arn:aws:connect:us-west-2:012345678910:trafficdistributiongroup/"
                + tdgSummary1Id.toString();
        TrafficDistributionGroupSummary trafficDistributionGroupSummary1 = TrafficDistributionGroupSummary.builder()
                .arn(tdgArn1)
                .id(tdgSummary1Id.toString())
                .name("TDG Summary 1")
                .status("ACTIVE")
                .instanceArn(model.getInstanceArn())
                .isDefault(TDG_IS_DEFAULT)
                .build();

        final UUID tdgSummary2Id = UUID.randomUUID();
        final String tdgArn2 = "arn:aws:connect:us-west-2:012345678910:trafficdistributiongroup/"
                + tdgSummary2Id.toString();
        TrafficDistributionGroupSummary trafficDistributionGroupSummary2 = TrafficDistributionGroupSummary.builder()
                .arn(tdgArn2)
                .id(tdgSummary2Id.toString())
                .name("TDG Summary 2")
                .status("ACTIVE")
                .instanceArn(model.getInstanceArn())
                .isDefault(false)
                .build();

        final ArgumentCaptor<ListTrafficDistributionGroupsRequest> listTrafficDistributionGroupsRequestArgumentCaptor =
                ArgumentCaptor.forClass(ListTrafficDistributionGroupsRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .nextToken(null)
                .region("us-west-2")
                .build();

        final ListTrafficDistributionGroupsResponse listTrafficDistributionGroupsResponse =
                ListTrafficDistributionGroupsResponse.builder()
                        .trafficDistributionGroupSummaryList(
                                Lists.newArrayList(trafficDistributionGroupSummary1, trafficDistributionGroupSummary2))
                        .nextToken(null)
                        .build();

        when(proxyClient.client()
                .listTrafficDistributionGroups(listTrafficDistributionGroupsRequestArgumentCaptor.capture()))
                .thenReturn(listTrafficDistributionGroupsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(
                        proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(listTrafficDistributionGroupsRequestArgumentCaptor.getValue().instanceId())
                .isEqualTo(model.getInstanceArn());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels().size()).isEqualTo(2);

        final List<ResourceModel> results =
                response.getResourceModels()
                        .stream()
                        .sorted(Comparator.comparing(ResourceModel::getName))
                        .collect(Collectors.toList());
        assertThat(results.get(0).getName()).isEqualTo(trafficDistributionGroupSummary1.name());
        assertThat(results.get(0).getStatus()).isEqualTo(trafficDistributionGroupSummary1.statusAsString());
        assertThat(results.get(0).getInstanceArn()).isEqualTo(trafficDistributionGroupSummary1.instanceArn());
        assertThat(results.get(0).getTrafficDistributionGroupArn()).isEqualTo(trafficDistributionGroupSummary1.arn());
        assertThat(results.get(0).getIsDefault()).isEqualTo(trafficDistributionGroupSummary1.isDefault());
        assertThat(results.get(1).getName()).isEqualTo(trafficDistributionGroupSummary2.name());
        assertThat(results.get(1).getStatus()).isEqualTo(trafficDistributionGroupSummary2.statusAsString());
        assertThat(results.get(1).getInstanceArn()).isEqualTo(trafficDistributionGroupSummary2.instanceArn());
        assertThat(results.get(1).getTrafficDistributionGroupArn()).isEqualTo(trafficDistributionGroupSummary2.arn());
        assertThat(results.get(1).getIsDefault()).isEqualTo(trafficDistributionGroupSummary2.isDefault());

        verify(proxyClient.client())
                .listTrafficDistributionGroups(listTrafficDistributionGroupsRequestArgumentCaptor.capture());
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_ListTrafficDistributionGroups_NoResults() {
        final ArgumentCaptor<ListTrafficDistributionGroupsRequest> listTrafficDistributionGroupsRequestArgumentCaptor =
                ArgumentCaptor.forClass(ListTrafficDistributionGroupsRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildListTrafficDistributionGroupDesiredStateResourceModel())
                .nextToken(null)
                .region("us-west-2")
                .build();

        final ListTrafficDistributionGroupsResponse listTrafficDistributionGroupsResponse =
                ListTrafficDistributionGroupsResponse.builder()
                        .trafficDistributionGroupSummaryList(Lists.newArrayList())
                        .nextToken(null)
                        .build();

        when(proxyClient.client()
                .listTrafficDistributionGroups(listTrafficDistributionGroupsRequestArgumentCaptor.capture()))
                .thenReturn(listTrafficDistributionGroupsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(
                        proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getResourceModels().size()).isEqualTo(0);

        verify(proxyClient.client())
                .listTrafficDistributionGroups(listTrafficDistributionGroupsRequestArgumentCaptor.capture());
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_ListTrafficDistributionGroups() {
        final ArgumentCaptor<ListTrafficDistributionGroupsRequest> listTrafficDistributionGroupsRequestArgumentCaptor =
                ArgumentCaptor.forClass(ListTrafficDistributionGroupsRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildListTrafficDistributionGroupDesiredStateResourceModel())
                .nextToken(null)
                .region("us-west-2")
                .build();

        when(proxyClient.client()
                .listTrafficDistributionGroups(listTrafficDistributionGroupsRequestArgumentCaptor.capture()))
                .thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(connectClient, times(1)).serviceName();
    }
}
