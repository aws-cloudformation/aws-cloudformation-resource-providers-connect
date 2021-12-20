package software.amazon.connect.instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.*;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.connect.instance.InstanceTestDataProvider.*;

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

    @Test
    public void testHandleRequest_CONNECT_MANAGED_InProgress() {
        mockCreateInstanceRequest_Success();
        mockDescribeInstanceRequest_InProgress();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(buildInstance_withType(CONNECT_MANAGED))
            .build();

        final CallbackContext callbackContext = CallbackContext.builder().stabilizationRetriesRemaining(5).build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testHandleRequest_SAML_InProgress() {
        mockCreateInstanceRequest_Success();
        mockDescribeInstanceRequest_InProgress();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(buildInstance_withType(SAML))
            .build();

        final CallbackContext callbackContext = CallbackContext.builder().stabilizationRetriesRemaining(5).build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testHandleRequest_EXISTING_DIRECTORY_InProgress() {
        mockCreateInstanceRequest_Success();
        mockDescribeInstanceRequest_InProgress();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(buildInstance_withType(EXISTING_DIRECTORY))
            .build();

        final CallbackContext callbackContext = CallbackContext.builder().stabilizationRetriesRemaining(5).build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testHandleRequest_CONNECT_MANAGED_Creation_Failed() {
        mockCreateInstanceRequest_Success();
        mockDescribeInstanceRequest_Failed();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(buildInstance_withType(CONNECT_MANAGED))
            .build();

        final CallbackContext callbackContext = CallbackContext.builder().stabilizationRetriesRemaining(5).build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testHandleRequest_WithCallbackContext_Success() {
        CallbackContext callbackContext = CallbackContext.builder()
                .instance(Instance.builder()
                        .arn(INSTANCE_ARN)
                        .id(INSTANCE_ID)
                        .instanceAlias(INSTANCE_ALIAS)
                        .identityManagementType(CONNECT_MANAGED)
                        .instanceStatus(INSTANCE_STATUS_ACTIVE)
                        .inboundCallsEnabled(true)
                        .outboundCallsEnabled(true)
                        .createdTime(Instant.now())
                        .build())
                .stabilizationRetriesRemaining(100)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(buildInstance_withType(CONNECT_MANAGED))
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void testHandleRequest_Exception() {
        final ArgumentCaptor<CreateInstanceRequest> createInstanceRequestArgumentCaptor = ArgumentCaptor.forClass(CreateInstanceRequest.class);
        when(proxyClient.client().createInstance(createInstanceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildInstance_withType(CONNECT_MANAGED))
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).createInstance(createInstanceRequestArgumentCaptor.capture());
        assertThat(createInstanceRequestArgumentCaptor.getValue().instanceAlias()).isEqualTo(INSTANCE_ALIAS);
        assertThat(createInstanceRequestArgumentCaptor.getValue().identityManagementTypeAsString()).isEqualTo(CONNECT_MANAGED);
        assertTrue(createInstanceRequestArgumentCaptor.getValue().inboundCallsEnabled());
        assertTrue(createInstanceRequestArgumentCaptor.getValue().outboundCallsEnabled());
    }

    private ResourceModel buildInstance_withType(String identityManagementType) {
        final Attributes attributes = Attributes.builder()
                .inboundCalls(true)
                .outboundCalls(true)
                .contactflowLogs(true)
                .build();

        if(identityManagementType == EXISTING_DIRECTORY) {
            return ResourceModel.builder()
                    .directoryId(DIRECTORY_ID)
                    .identityManagementType(identityManagementType)
                    .attributes(attributes)
                    .build();
        }
        return ResourceModel.builder()
                .instanceAlias(INSTANCE_ALIAS)
                .identityManagementType(identityManagementType)
                .attributes(attributes)
                .build();
    }

    private void mockCreateInstanceRequest_Success() {
        final ArgumentCaptor<CreateInstanceRequest> createInstanceRequestArgumentCaptor = ArgumentCaptor.forClass(CreateInstanceRequest.class);

        final CreateInstanceResponse createInstanceResponse = CreateInstanceResponse.builder()
                .arn(INSTANCE_ARN)
                .id(INSTANCE_ID)
                .build();
        when(proxyClient.client().createInstance(createInstanceRequestArgumentCaptor.capture())).thenReturn(createInstanceResponse);
    }

    private void mockDescribeInstanceRequest_InProgress() {
        final ArgumentCaptor<DescribeInstanceRequest> describeInstanceRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeInstanceRequest.class);

        final Instance instance = Instance.builder()
                .id(INSTANCE_ID)
                .arn(INSTANCE_ARN)
                .instanceStatus(INSTANCE_STATUS_CREATION_IN_PROGRESS)
                .build();

        final DescribeInstanceResponse describeInstanceResponse = DescribeInstanceResponse.builder()
                .instance(instance)
                .build();
        when(proxyClient.client().describeInstance(describeInstanceRequestArgumentCaptor.capture())).thenReturn(describeInstanceResponse);
    }

    private void mockDescribeInstanceRequest_Failed() {
        final ArgumentCaptor<DescribeInstanceRequest> describeInstanceRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeInstanceRequest.class);

        final Instance instance = Instance.builder()
                .id(INSTANCE_ID)
                .arn(INSTANCE_ARN)
                .instanceStatus(INSTANCE_STATUS_CREATION_FAILED)
                .statusReason(InstanceStatusReason.builder()
                        .message(INSTANCE_STATUS_FAILURE_REASON)
                        .build())
                .build();

        final DescribeInstanceResponse describeInstanceResponse = DescribeInstanceResponse.builder()
                .instance(instance)
                .build();
        when(proxyClient.client().describeInstance(describeInstanceRequestArgumentCaptor.capture())).thenReturn(describeInstanceResponse);
    }
}
