package software.amazon.connect.instancestorageconfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.AssociateInstanceStorageConfigRequest;
import software.amazon.awssdk.services.connect.model.AssociateInstanceStorageConfigResponse;
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
    public void testHandleRequest_CALL_RECORDINGS_Success() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(CALL_RECORDINGS)
                .storageType(S3)
                .s3Config(prepareS3Config())
                .build();
        testHandleRequest_Storage_Config_Success(model);
    }

    @Test
    public void testHandleRequest_CHAT_TRANSCRIPTS_Success() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(CHAT_TRANSCRIPTS)
                .storageType(S3)
                .s3Config(prepareS3Config())
                .build();
        testHandleRequest_Storage_Config_Success(model);
    }

    @Test
    public void testHandleRequest_SCHEDULED_REPORTS_Success() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(SCHEDULED_REPORTS)
                .storageType(S3)
                .s3Config(prepareS3Config())
                .build();
        testHandleRequest_Storage_Config_Success(model);
    }

    @Test
    public void testHandleRequest_MEDIA_STREAMS_Success() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(MEDIA_STREAMS)
                .storageType(KINESIS_VIDEO_STREAM)
                .kinesisVideoStreamConfig(prepareKinesisVideoStreamConfig())
                .build();
        testHandleRequest_Storage_Config_Success(model);
    }

    @Test
    public void testHandleRequest_CONTACT_TRACE_RECORDS_Kinesis_Stream_Success() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(CONTACT_TRACE_RECORDS)
                .storageType(KINESIS_STREAM)
                .kinesisStreamConfig(prepareKinesisStreamConfig())
                .build();
        testHandleRequest_Storage_Config_Success(model);
    }

    @Test
    public void testHandleRequest_CONTACT_TRACE_RECORDS_Kinesis_Firehose_Success() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(CONTACT_TRACE_RECORDS)
                .storageType(KINESIS_FIREHOSE)
                .kinesisFirehoseConfig(prepareKinesisFirehoseConfig())
                .build();
        testHandleRequest_Storage_Config_Success(model);
    }

    @Test
    public void testHandleRequest_AGENT_EVENTS_Success() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(AGENT_EVENTS)
                .storageType(KINESIS_STREAM)
                .kinesisStreamConfig(prepareKinesisStreamConfig())
                .build();
        testHandleRequest_Storage_Config_Success(model);
    }

    @Test
    void testHandleRequest_Exception() {
        final ArgumentCaptor<AssociateInstanceStorageConfigRequest> associateInstanceStorageConfigRequestArgumentCaptor = ArgumentCaptor.forClass(AssociateInstanceStorageConfigRequest.class);
        when(proxyClient.client().associateInstanceStorageConfig(associateInstanceStorageConfigRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(CALL_RECORDINGS)
                .storageType(S3)
                .s3Config(prepareS3Config())
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).associateInstanceStorageConfig(associateInstanceStorageConfigRequestArgumentCaptor.capture());
        assertThat(associateInstanceStorageConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(associateInstanceStorageConfigRequestArgumentCaptor.getValue().resourceTypeAsString()).isEqualTo(CALL_RECORDINGS);
        assertThat(associateInstanceStorageConfigRequestArgumentCaptor.getValue().storageConfig().storageTypeAsString()).isEqualTo(S3);
        assertThat(associateInstanceStorageConfigRequestArgumentCaptor.getValue().storageConfig()).isNotNull();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidInstanceArn() {
        final ResourceModel model = ResourceModel.builder().instanceArn(INVALID_INSTANCE_ARN).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    public void testHandleRequest_Storage_Config_Success(ResourceModel model) {
        final ArgumentCaptor<AssociateInstanceStorageConfigRequest> associateInstanceStorageConfigRequestArgumentCaptor = ArgumentCaptor.forClass(AssociateInstanceStorageConfigRequest.class);

        final AssociateInstanceStorageConfigResponse associateInstanceStorageConfigResponse = AssociateInstanceStorageConfigResponse.builder()
                .associationId(ASSOCIATION_ID)
                .build();
        when(proxyClient.client().associateInstanceStorageConfig(associateInstanceStorageConfigRequestArgumentCaptor.capture()))
                .thenReturn(associateInstanceStorageConfigResponse);

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


        verify(proxyClient.client()).associateInstanceStorageConfig(associateInstanceStorageConfigRequestArgumentCaptor.capture());
        assertThat(associateInstanceStorageConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(associateInstanceStorageConfigRequestArgumentCaptor.getValue().resourceTypeAsString()).isEqualTo(model.getResourceType());
        assertThat(associateInstanceStorageConfigRequestArgumentCaptor.getValue().storageConfig().storageTypeAsString()).isEqualTo(model.getStorageType());
    }

    private S3Config prepareS3Config() {
        return S3Config.builder()
                .bucketName(BUCKET_NAME)
                .bucketPrefix(BUCKET_PREFIX)
                .encryptionConfig(null)
                .build();
    }

    private KinesisStreamConfig prepareKinesisStreamConfig() {
        return KinesisStreamConfig.builder()
                .streamArn(KINESIS_STREAM_ARN)
                .build();
    }

    private KinesisFirehoseConfig prepareKinesisFirehoseConfig() {
        return KinesisFirehoseConfig.builder()
                .firehoseArn(KINESIS_FIREHOSE_ARN)
                .build();
    }

    private KinesisVideoStreamConfig prepareKinesisVideoStreamConfig() {
        return KinesisVideoStreamConfig.builder()
                .prefix(KINESIS_VIDEO_STREAM_PREFIX)
                .retentionPeriodHours(RETENTION_PERIOD_HOURS)
                .encryptionConfig(EncryptionConfig.builder()
                        .keyId(KMS_KEY_ID)
                        .encryptionType(KMS)
                        .build())
                .build();
    }
}
