package software.amazon.connect.quickconnect;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteQuickConnectRequest;
import software.amazon.awssdk.services.connect.model.DeleteQuickConnectResponse;
import software.amazon.awssdk.services.connect.model.DescribeQuickConnectRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeQueue;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeUser;

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
    void testHandleRequest_Success_TypeUser() {
        final ArgumentCaptor<DeleteQuickConnectRequest> deleteQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteQuickConnectRequest.class);

        final DeleteQuickConnectResponse deleteQuickConnectResponse = DeleteQuickConnectResponse.builder().build();

        when(proxyClient.client().deleteQuickConnect(deleteQuickConnectRequestArgumentCaptor.capture())).thenReturn(deleteQuickConnectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
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

        verify(proxyClient.client()).deleteQuickConnect(deleteQuickConnectRequestArgumentCaptor.capture());
        assertThat(deleteQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    void testHandleRequest_Exception_DeleteQuickConnect() {
        final ArgumentCaptor<DeleteQuickConnectRequest> deleteQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteQuickConnectRequest.class);

        when(proxyClient.client().deleteQuickConnect(deleteQuickConnectRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).deleteQuickConnect(deleteQuickConnectRequestArgumentCaptor.capture());
        assertThat(deleteQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidQuickConnectArn() {
        final ArgumentCaptor<DescribeQuickConnectRequest> describeQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeQuickConnectRequest.class);
        final ResourceModel model = buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber();
        model.setQuickConnectArn("InvalidQCArn");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(describeQuickConnectRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);
    }
}
