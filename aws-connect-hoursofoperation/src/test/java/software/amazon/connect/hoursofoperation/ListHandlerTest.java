package software.amazon.connect.hoursofoperation;

import org.junit.jupiter.api.AfterEach;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.HoursOfOperationSummary;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationsRequest;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationsResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ID;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHoursOfOperationDesiredStateResourceModel;

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
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new ListHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success() {
        final ArgumentCaptor<ListHoursOfOperationsRequest> listHoursOfOperationsRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationsRequest.class);

        final ListHoursOfOperationsResponse listHoursOfOperationsResponse = buildListHoursOfOperationResponse(null);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .build();

        when(proxyClient.client().listHoursOfOperations(listHoursOfOperationsRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationsResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModels().size()).isEqualTo(1);

        verify(proxyClient.client()).listHoursOfOperations(listHoursOfOperationsRequestArgumentCaptor.capture());
    }

    @Test
    public void testHandleRequest_Exception() {
        final ArgumentCaptor<ListHoursOfOperationsRequest> listHoursOfOperationsRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationsRequest.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .build();

        when(proxyClient.client().listHoursOfOperations(listHoursOfOperationsRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).listHoursOfOperations(listHoursOfOperationsRequestArgumentCaptor.capture());
    }

    @Test
    public void testHandleRequest_NullResourceModel() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .build();
        assertThrows(CfnInvalidRequestException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_NullInstanceArn() {
        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        assertThrows(CfnInvalidRequestException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, never()).serviceName();
    }

    protected static ListHoursOfOperationsResponse buildListHoursOfOperationResponse(String nextToken) {
        HoursOfOperationSummary hoursOfOperationSummary = HoursOfOperationSummary.builder()
                .arn(HOURS_OF_OPERATION_ARN)
                .id(HOURS_OF_OPERATION_ID)
                .build();
        ListHoursOfOperationsResponse.Builder builder = ListHoursOfOperationsResponse.builder();
        builder.hoursOfOperationSummaryList(hoursOfOperationSummary);
        if (StringUtils.isNotBlank(nextToken)) {
            builder.nextToken(nextToken);
        }
        return builder.build();
    }
}

