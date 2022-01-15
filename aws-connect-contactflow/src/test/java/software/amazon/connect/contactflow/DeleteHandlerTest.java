package software.amazon.connect.contactflow;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteContactFlowRequest;
import software.amazon.awssdk.services.connect.model.DeleteContactFlowResponse;
import software.amazon.awssdk.services.connect.model.DeleteContactFlowRequest;
import software.amazon.awssdk.services.connect.model.DeleteHoursOfOperationRequest;
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
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.*;

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
        final ArgumentCaptor<DeleteContactFlowRequest> deleteContactFlowRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteContactFlowRequest.class);

        final DeleteContactFlowResponse deletContactFlowResponse = DeleteContactFlowResponse.builder().build();

        when(proxyClient.client().deleteContactFlow(deleteContactFlowRequestArgumentCaptor.capture())).thenReturn(deletContactFlowResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowDesiredStateResourceModel())
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

        verify(proxyClient.client()).deleteContactFlow(deleteContactFlowRequestArgumentCaptor.capture());
        assertThat(deleteContactFlowRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteContactFlowRequestArgumentCaptor.getValue().contactFlowId()).isEqualTo(CONTACT_FLOW_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    void testHandleRequest_Exception_DeleteContactFlow() {
        final ArgumentCaptor<DeleteContactFlowRequest> deleteContactFlowRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteContactFlowRequest.class);

        when(proxyClient.client().deleteContactFlow(deleteContactFlowRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).deleteContactFlow(deleteContactFlowRequestArgumentCaptor.capture());
        assertThat(deleteContactFlowRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteContactFlowRequestArgumentCaptor.getValue().contactFlowId()).isEqualTo(CONTACT_FLOW_ARN);

        verify(connectClient, times(1)).serviceName();
    }
}
