package software.amazon.connect.hoursofoperation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationResponse;
import software.amazon.awssdk.services.connect.model.HoursOfOperationConfig;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_NAME_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_DESCRIPTION_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ID;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TIME_ZONE_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHoursOfOperationResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getConfig;

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
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new CreateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verify(connectClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    void testHandleRequest_Success() {
        final ArgumentCaptor<CreateHoursOfOperationRequest> createHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(CreateHoursOfOperationRequest.class);
        final CreateHoursOfOperationResponse createHoursOfOperationResponse = CreateHoursOfOperationResponse.builder()
                .hoursOfOperationId(HOURS_OF_OPERATION_ID)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .build();

        when(proxyClient.client().createHoursOfOperation(createHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(createHoursOfOperationResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getHoursOfOperationArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createHoursOfOperation(createHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().timeZone()).isEqualTo(TIME_ZONE_ONE);
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().config().size()).isEqualTo(getConfig().size());
        validateConfig(createHoursOfOperationRequestArgumentCaptor.getValue().config());
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
    }

    @Test
    void testHandleRequest_Exception_CreateHoursOfOperationConnect() {
        final ArgumentCaptor<CreateHoursOfOperationRequest> createHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(CreateHoursOfOperationRequest.class);

        when(proxyClient.client().createHoursOfOperation(createHoursOfOperationRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).createHoursOfOperation(createHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().timeZone()).isEqualTo(TIME_ZONE_ONE);
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().config().size()).isEqualTo(getConfig().size());
        validateConfig(createHoursOfOperationRequestArgumentCaptor.getValue().config());
        assertThat(createHoursOfOperationRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
    }

    private void validateConfig(List<HoursOfOperationConfig> hoursOfOperationConfig) {
        int index = 0;
        for (software.amazon.connect.hoursofoperation.HoursOfOperationConfig config : getConfig()) {
            assertThat(hoursOfOperationConfig.get(index).day().toString()).isEqualTo(config.getDay());
            assertThat(hoursOfOperationConfig.get(index).startTime().hours()).isEqualTo(config.getStartTime().getHours());
            assertThat(hoursOfOperationConfig.get(index).startTime().minutes()).isEqualTo(config.getStartTime().getMinutes());
            assertThat(hoursOfOperationConfig.get(index).endTime().hours()).isEqualTo(config.getEndTime().getHours());
            assertThat(hoursOfOperationConfig.get(index).endTime().minutes()).isEqualTo(config.getEndTime().getMinutes());
            index += 1;
        }
    }
}
