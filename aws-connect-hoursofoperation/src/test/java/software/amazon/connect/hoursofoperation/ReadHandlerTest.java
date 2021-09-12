package software.amazon.connect.hoursofoperation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.DescribeHoursOfOperationResponse;
import software.amazon.awssdk.services.connect.model.HoursOfOperation;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.DAY_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_CONFIG_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_CONFIG_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_DESCRIPTION_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ID;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_NAME_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INVALID_HOURS_OF_OPERATION_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.MINUTES_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TIME_ZONE_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHoursOfOperationDesiredStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getConfig;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getHoursOfOperationConfig;

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
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new ReadHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success() {
        final ArgumentCaptor<DescribeHoursOfOperationRequest> describeHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeHoursOfOperationRequest.class);

        final DescribeHoursOfOperationResponse describeHoursOfOperationResponse = DescribeHoursOfOperationResponse.builder()
                .hoursOfOperation(getDescribeHoursOfOperationResponseObject())
                .build();

        when(proxyClient.client().describeHoursOfOperation(describeHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(describeHoursOfOperationResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getHoursOfOperationArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(response.getResourceModel().getDescription()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(response.getResourceModel().getName()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(response.getResourceModel().getConfig().size()).isEqualTo(1);
        validateConfig(response.getResourceModel().getConfig());
        assertThat(response.getResourceModel().getTimeZone()).isEqualTo(TIME_ZONE_ONE);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeHoursOfOperation(describeHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(describeHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidArn() {
        final ArgumentCaptor<DescribeHoursOfOperationRequest> describeHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeHoursOfOperationRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationResourceModelInvalidArn())
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(describeHoursOfOperationRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception() {
        final ArgumentCaptor<DescribeHoursOfOperationRequest> describeHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeHoursOfOperationRequest.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describeHoursOfOperation(describeHoursOfOperationRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describeHoursOfOperation(describeHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(describeHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    private void validateConfig(Set<HoursOfOperationConfig> hoursOfOperationConfig) {
        for (HoursOfOperationConfig config : hoursOfOperationConfig) {
            assertThat(config.getDay()).isEqualTo(DAY_ONE);
            assertThat(config.getStartTime().getHours()).isEqualTo(HOURS_ONE);
            assertThat(config.getStartTime().getMinutes()).isEqualTo(MINUTES_ONE);
        }
    }

    private ResourceModel buildHoursOfOperationResourceModelInvalidArn() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(INVALID_HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .timeZone(TIME_ZONE_ONE)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .build();
    }

    private HoursOfOperation getDescribeHoursOfOperationResponseObject() {
        return HoursOfOperation.builder()
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .hoursOfOperationId(HOURS_OF_OPERATION_ID)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .config(getHoursOfOperationConfig())
                .timeZone(TIME_ZONE_ONE)
                .tags(TAGS_ONE)
                .build();
    }
}
