package software.amazon.connect.quickconnect;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateQuickConnectRequest;
import software.amazon.awssdk.services.connect.model.CreateQuickConnectResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.CONTACT_FLOW_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.INSTANCE_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.PHONE_NUMBER;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUEUE_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.USER_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeQueue;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeUser;


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
    void testHandleRequest_Success_TypeUser() {
        final ArgumentCaptor<CreateQuickConnectRequest> createQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(CreateQuickConnectRequest.class);

        final CreateQuickConnectResponse createQuickConnectResponse = CreateQuickConnectResponse.builder()
                .quickConnectId(QUICK_CONNECT_ID)
                .quickConnectARN(QUICK_CONNECT_ARN)
                .build();
        when(proxyClient.client().createQuickConnect(createQuickConnectRequestArgumentCaptor.capture())).thenReturn(createQuickConnectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeUser())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getQuickConnectId()).isEqualTo(QUICK_CONNECT_ID);
        assertThat(response.getResourceModel().getQuickConnectARN()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createQuickConnect(createQuickConnectRequestArgumentCaptor.capture());
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_ONE);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_ONE);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNotNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().userId()).isEqualTo(USER_ID);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().userConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
    }

    @Test
    void testHandleRequest_Success_TypeQueue() {
        final ArgumentCaptor<CreateQuickConnectRequest> createQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(CreateQuickConnectRequest.class);

        final CreateQuickConnectResponse createQuickConnectResponse = CreateQuickConnectResponse.builder()
                .quickConnectId(QUICK_CONNECT_ID)
                .quickConnectARN(QUICK_CONNECT_ARN)
                .build();
        when(proxyClient.client().createQuickConnect(createQuickConnectRequestArgumentCaptor.capture())).thenReturn(createQuickConnectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypeQueue())
                .desiredResourceTags(TAGS_TWO)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getQuickConnectId()).isEqualTo(QUICK_CONNECT_ID);
        assertThat(response.getResourceModel().getQuickConnectARN()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createQuickConnect(createQuickConnectRequestArgumentCaptor.capture());
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_TWO);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_TWO);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNotNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().queueId()).isEqualTo(QUEUE_ID);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig().contactFlowId()).isEqualTo(CONTACT_FLOW_ID);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_TWO);
    }

    @Test
    void testHandleRequest_Success_TypePhoneNumber() {
        final ArgumentCaptor<CreateQuickConnectRequest> createQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(CreateQuickConnectRequest.class);

        final CreateQuickConnectResponse createQuickConnectResponse = CreateQuickConnectResponse.builder()
                .quickConnectId(QUICK_CONNECT_ID)
                .quickConnectARN(QUICK_CONNECT_ARN)
                .build();
        when(proxyClient.client().createQuickConnect(createQuickConnectRequestArgumentCaptor.capture())).thenReturn(createQuickConnectResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getQuickConnectId()).isEqualTo(QUICK_CONNECT_ID);
        assertThat(response.getResourceModel().getQuickConnectARN()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createQuickConnect(createQuickConnectRequestArgumentCaptor.capture());
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_THREE);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_THREE);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNotNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig().phoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
    }

    @Test
    void testHandleRequest_Exception_CreateQuickConnect() {
        final ArgumentCaptor<CreateQuickConnectRequest> createQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(CreateQuickConnectRequest.class);

        when(proxyClient.client().createQuickConnect(createQuickConnectRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).createQuickConnect(createQuickConnectRequestArgumentCaptor.capture());
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().name()).isEqualTo(QUICK_CONNECT_NAME_THREE);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().description()).isEqualTo(QUICK_CONNECT_DESCRIPTION_THREE);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().userConfig()).isNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().queueConfig()).isNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig()).isNotNull();
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().quickConnectConfig().phoneConfig().phoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(createQuickConnectRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
    }
}
