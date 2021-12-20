package software.amazon.connect.instancestorageconfig;

import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.model.DisassociateInstanceStorageConfigRequest;
import software.amazon.awssdk.services.connect.model.UpdateInstanceStorageConfigRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.connect.instancestorageconfig.InstanceStorageConfigTestDataProvider.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private UpdateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @Test
    public void testHandleRequest_SimpleSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .associationId(ASSOCIATION_ID)
                .resourceType(CALL_RECORDINGS)
                .storageType(S3)
                .s3Config(prepareS3Config())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testHandleRequest_Exception() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .associationId(ASSOCIATION_ID)
                .resourceType(CALL_RECORDINGS)
                .storageType(S3)
                .s3Config(prepareS3Config())
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ArgumentCaptor<UpdateInstanceStorageConfigRequest> updateInstanceStorageConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateInstanceStorageConfigRequest.class);
        when(proxyClient.client().updateInstanceStorageConfig(updateInstanceStorageConfigRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateInstanceStorageConfig(updateInstanceStorageConfigRequestArgumentCaptor.capture());
        assertThat(updateInstanceStorageConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateInstanceStorageConfigRequestArgumentCaptor.getValue().associationId()).isEqualTo(ASSOCIATION_ID);
        assertThat(updateInstanceStorageConfigRequestArgumentCaptor.getValue().resourceTypeAsString()).isEqualTo(CALL_RECORDINGS);
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidInstanceArn() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INVALID_INSTANCE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    private S3Config prepareS3Config() {
        return S3Config.builder()
                .bucketName(BUCKET_NAME)
                .bucketPrefix(BUCKET_PREFIX)
                .encryptionConfig(null)
                .build();
    }
}
