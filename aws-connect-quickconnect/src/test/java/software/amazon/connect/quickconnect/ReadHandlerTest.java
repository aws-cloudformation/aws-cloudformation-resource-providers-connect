package software.amazon.connect.quickconnect;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.model.DescribeQuickConnectRequest;
import software.amazon.awssdk.services.connect.model.DescribeQuickConnectResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.INSTANCE_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeQueue;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeUser;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.getDescribeQuickConnectResponseObjectWithQuickConnectTypePhoneNumber;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.getDescribeQuickConnectResponseObjectWithQuickConnectTypeQueue;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.getDescribeQuickConnectResponseObjectWithQuickConnectTypeUser;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private ReadHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;
    private ResourceModel modelWithQuickConnectTypeUser;
    private ResourceModel modelWithQuickConnectTypeQueue;
    private ResourceModel modelWithQuickConnectTypePhoneNumber;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new ReadHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
        modelWithQuickConnectTypeUser = buildQuickConnectResourceModelWithQuickConnectTypeUser();
        modelWithQuickConnectTypeQueue = buildQuickConnectResourceModelWithQuickConnectTypeQueue();
        modelWithQuickConnectTypePhoneNumber = buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber();
    }

    @AfterEach
    public void post_execute() {
        verify(connectClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success_TypeUser() {
        final ArgumentCaptor<DescribeQuickConnectRequest> describeQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeQuickConnectRequest.class);

        final DescribeQuickConnectResponse describeQuickConnectResponse = DescribeQuickConnectResponse.builder()
                .quickConnect(getDescribeQuickConnectResponseObjectWithQuickConnectTypeUser())
                .build();

        when(proxyClient.client().describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture())).thenReturn(describeQuickConnectResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelWithQuickConnectTypeUser)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceId()).isEqualTo(modelWithQuickConnectTypeUser.getInstanceId());
        assertThat(response.getResourceModel().getQuickConnectId()).isEqualTo(modelWithQuickConnectTypeUser.getQuickConnectId());
        assertThat(response.getResourceModel().getDescription()).isEqualTo(modelWithQuickConnectTypeUser.getDescription());
        assertThat(response.getResourceModel().getName()).isEqualTo(modelWithQuickConnectTypeUser.getName());
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getPhoneConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig()).isNotNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getQuickConnectType()).isEqualTo(modelWithQuickConnectTypeUser.getQuickConnectConfig().getQuickConnectType());
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig().getUserId()).isEqualTo(modelWithQuickConnectTypeUser.getQuickConnectConfig().getUserConfig().getUserId());
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig().getContactFlowId()).isEqualTo(modelWithQuickConnectTypeUser.getQuickConnectConfig().getUserConfig().getContactFlowId());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture());
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
    }

    @Test
    public void testHandleRequest_Success_TypeQueue() {
        final ArgumentCaptor<DescribeQuickConnectRequest> describeQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeQuickConnectRequest.class);
        final DescribeQuickConnectResponse describeQuickConnectResponse = DescribeQuickConnectResponse.builder()
                .quickConnect(getDescribeQuickConnectResponseObjectWithQuickConnectTypeQueue())
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelWithQuickConnectTypeQueue)
                .build();

        when(proxyClient.client().describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture())).thenReturn(describeQuickConnectResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceId()).isEqualTo(modelWithQuickConnectTypeQueue.getInstanceId());
        assertThat(response.getResourceModel().getQuickConnectId()).isEqualTo(modelWithQuickConnectTypeQueue.getQuickConnectId());
        assertThat(response.getResourceModel().getDescription()).isEqualTo(modelWithQuickConnectTypeQueue.getDescription());
        assertThat(response.getResourceModel().getName()).isEqualTo(modelWithQuickConnectTypeQueue.getName());
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig()).isNotNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getPhoneConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getQuickConnectType()).isEqualTo(modelWithQuickConnectTypeQueue.getQuickConnectConfig().getQuickConnectType());
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig().getQueueId()).isEqualTo(modelWithQuickConnectTypeQueue.getQuickConnectConfig().getQueueConfig().getQueueId());
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig().getContactFlowId()).isEqualTo(modelWithQuickConnectTypeQueue.getQuickConnectConfig().getQueueConfig().getContactFlowId());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture());
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
    }

    @Test
    public void testHandleRequest_Success_TypePhoneNumber() {
        final ArgumentCaptor<DescribeQuickConnectRequest> describeQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeQuickConnectRequest.class);
        final DescribeQuickConnectResponse describeQuickConnectResponse = DescribeQuickConnectResponse.builder()
                .quickConnect(getDescribeQuickConnectResponseObjectWithQuickConnectTypePhoneNumber())
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelWithQuickConnectTypePhoneNumber)
                .build();

        when(proxyClient.client().describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture())).thenReturn(describeQuickConnectResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceId()).isEqualTo(modelWithQuickConnectTypePhoneNumber.getInstanceId());
        assertThat(response.getResourceModel().getQuickConnectId()).isEqualTo(modelWithQuickConnectTypePhoneNumber.getQuickConnectId());
        assertThat(response.getResourceModel().getDescription()).isEqualTo(modelWithQuickConnectTypePhoneNumber.getDescription());
        assertThat(response.getResourceModel().getName()).isEqualTo(modelWithQuickConnectTypePhoneNumber.getName());
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getPhoneConfig()).isNotNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getQuickConnectType()).isEqualTo(modelWithQuickConnectTypePhoneNumber.getQuickConnectConfig().getQuickConnectType());
        assertThat(response.getResourceModel().getQuickConnectConfig().getPhoneConfig().getPhoneNumber()).isEqualTo(modelWithQuickConnectTypePhoneNumber.getQuickConnectConfig().getPhoneConfig().getPhoneNumber());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture());
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
    }

    @Test
    public void testHandleRequest_Exception_DescribeQuickConnect() {
        final ArgumentCaptor<DescribeQuickConnectRequest> describeQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeQuickConnectRequest.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelWithQuickConnectTypePhoneNumber)
                .build();

        when(proxyClient.client().describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture());
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);
    }
}