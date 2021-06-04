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
import software.amazon.awssdk.services.connect.model.QuickConnectType;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.CONTACT_FLOW_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.PHONE_NUMBER;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUEUE_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_DESCRIPTION_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_THREE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_NAME_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.USER_ARN;
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
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getQuickConnectArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(response.getResourceModel().getDescription()).isEqualTo(QUICK_CONNECT_DESCRIPTION_ONE);
        assertThat(response.getResourceModel().getName()).isEqualTo(QUICK_CONNECT_NAME_ONE);
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getPhoneConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig()).isNotNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getQuickConnectType()).isEqualTo(QuickConnectType.USER.toString());
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig().getUserArn()).isEqualTo(USER_ARN);
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig().getContactFlowArn()).isEqualTo(CONTACT_FLOW_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture());
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
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
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getQuickConnectArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(response.getResourceModel().getDescription()).isEqualTo(QUICK_CONNECT_DESCRIPTION_TWO);
        assertThat(response.getResourceModel().getName()).isEqualTo(QUICK_CONNECT_NAME_TWO);
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig()).isNotNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getPhoneConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getQuickConnectType()).isEqualTo(QuickConnectType.QUEUE.toString());
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig().getQueueArn()).isEqualTo(QUEUE_ARN);
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig().getContactFlowArn()).isEqualTo(CONTACT_FLOW_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture());
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
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
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getQuickConnectArn()).isEqualTo(QUICK_CONNECT_ARN);
        assertThat(response.getResourceModel().getDescription()).isEqualTo(QUICK_CONNECT_DESCRIPTION_THREE);
        assertThat(response.getResourceModel().getName()).isEqualTo(QUICK_CONNECT_NAME_THREE);
        assertThat(response.getResourceModel().getQuickConnectConfig().getQueueConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getPhoneConfig()).isNotNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getUserConfig()).isNull();
        assertThat(response.getResourceModel().getQuickConnectConfig().getQuickConnectType()).isEqualTo(QuickConnectType.PHONE_NUMBER.toString());
        assertThat(response.getResourceModel().getQuickConnectConfig().getPhoneConfig().getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeQuickConnect(describeQuickConnectRequestArgumentCaptor.capture());
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
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
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeQuickConnectRequestArgumentCaptor.getValue().quickConnectId()).isEqualTo(QUICK_CONNECT_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidQuickConnectArn() {
        final ArgumentCaptor<DescribeQuickConnectRequest> describeQuickConnectRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeQuickConnectRequest.class);
        modelWithQuickConnectTypePhoneNumber.setQuickConnectArn("InvalidQCArn");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(modelWithQuickConnectTypePhoneNumber)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(describeQuickConnectRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);
    }
}
