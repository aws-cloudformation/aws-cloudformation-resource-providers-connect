package software.amazon.connect.hoursofoperation;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.DeleteHoursOfOperationResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INVALID_HOURS_OF_OPERATION_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHoursOfOperationDesiredStateResourceModel;

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
    void testHandleRequest_Success() {
        final ArgumentCaptor<DeleteHoursOfOperationRequest> deleteHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationRequest.class);

        final DeleteHoursOfOperationResponse deleteHoursOfOperationResponse = DeleteHoursOfOperationResponse.builder().build();

        when(proxyClient.client().deleteHoursOfOperation(deleteHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(deleteHoursOfOperationResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).deleteHoursOfOperation(deleteHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(deleteHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    void testHandleRequest_Exception_DeleteHoursOfOperation() {
        final ArgumentCaptor<DeleteHoursOfOperationRequest> deleteHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationRequest.class);

        when(proxyClient.client().deleteHoursOfOperation(deleteHoursOfOperationRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).deleteHoursOfOperation(deleteHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(deleteHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidHoursOfOperationArn() {
        final ArgumentCaptor<DeleteHoursOfOperationRequest> deleteHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationRequest.class);
        final ResourceModel model = buildHoursOfOperationDesiredStateResourceModel();
        model.setHoursOfOperationArn(INVALID_HOURS_OF_OPERATION_ARN);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(deleteHoursOfOperationRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }
}
