package software.amazon.connect.instancestorageconfig;

import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.model.*;
import software.amazon.awssdk.services.connect.model.EncryptionConfig;
import software.amazon.awssdk.services.connect.model.KinesisFirehoseConfig;
import software.amazon.awssdk.services.connect.model.KinesisStreamConfig;
import software.amazon.awssdk.services.connect.model.KinesisVideoStreamConfig;
import software.amazon.awssdk.services.connect.model.S3Config;
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
public class ReadHandlerTest {

    private ReadHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new ReadHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @Test
    public void testHandleRequest_CALL_RECORDINGS_SimpleSuccess() {
        testHandleRequest_Storage_Config_SimpleSuccess(CALL_RECORDINGS, prepareS3InstanceStorageConfig());
    }

    @Test
    public void testHandleRequest_CHAT_TRANSCRIPTS_SimpleSuccess() {
        testHandleRequest_Storage_Config_SimpleSuccess(CHAT_TRANSCRIPTS, prepareS3InstanceStorageConfig());
    }

    @Test
    public void testHandleRequest_SCHEDULED_REPORTS_SimpleSuccess() {
        testHandleRequest_Storage_Config_SimpleSuccess(SCHEDULED_REPORTS, prepareS3InstanceStorageConfig());
    }

    @Test
    public void testHandleRequest_MEDIA_STREAMS_SimpleSuccess() {
        testHandleRequest_Storage_Config_SimpleSuccess(MEDIA_STREAMS, prepareKinesisVideoStreamInstanceStorageConfig());
    }

    @Test
    public void testHandleRequest_CONTACT_TRACE_RECORDS_Kinesis_Stream_SimpleSuccess() {
        testHandleRequest_Storage_Config_SimpleSuccess(CONTACT_TRACE_RECORDS, prepareKinesisStreamInstanceStorageConfig());
    }

    @Test
    public void testHandleRequest_CONTACT_TRACE_RECORDS_Kinesis_Firehose_SimpleSuccess() {
        testHandleRequest_Storage_Config_SimpleSuccess(CONTACT_TRACE_RECORDS, prepareKinesisFirehoseInstanceStorageConfig());
    }

    @Test
    public void testHandleRequest_AGENT_EVENTS_SimpleSuccess() {
        testHandleRequest_Storage_Config_SimpleSuccess(AGENT_EVENTS, prepareKinesisStreamInstanceStorageConfig());
    }

    @Test
    public void testHandleRequest_Exception() {
        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .associationId(ASSOCIATION_ID)
                .resourceType(CALL_RECORDINGS)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ArgumentCaptor<DescribeInstanceStorageConfigRequest> describeInstanceStorageConfigRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeInstanceStorageConfigRequest.class);
        when(proxyClient.client().describeInstanceStorageConfig(describeInstanceStorageConfigRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describeInstanceStorageConfig(describeInstanceStorageConfigRequestArgumentCaptor.capture());
        assertThat(describeInstanceStorageConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeInstanceStorageConfigRequestArgumentCaptor.getValue().associationId()).isEqualTo(ASSOCIATION_ID);
        assertThat(describeInstanceStorageConfigRequestArgumentCaptor.getValue().resourceTypeAsString()).isEqualTo(CALL_RECORDINGS);
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

    public void testHandleRequest_Storage_Config_SimpleSuccess(String resourceType, InstanceStorageConfig instanceStorageConfig) {
        final ReadHandler handler = new ReadHandler();

        final ArgumentCaptor<DescribeInstanceStorageConfigRequest> describeInstanceStorageConfigRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeInstanceStorageConfigRequest.class);

        final DescribeInstanceStorageConfigResponse describeInstanceStorageConfigResponse = DescribeInstanceStorageConfigResponse.builder()
                .storageConfig(instanceStorageConfig)
                .build();
        when(proxyClient.client().describeInstanceStorageConfig(describeInstanceStorageConfigRequestArgumentCaptor.capture())).thenReturn(describeInstanceStorageConfigResponse);

        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .resourceType(resourceType)
                .associationId(ASSOCIATION_ID)
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

    private InstanceStorageConfig prepareS3InstanceStorageConfig() {
        return InstanceStorageConfig.builder()
                .storageType(StorageType.S3)
                .s3Config(S3Config.builder()
                        .bucketPrefix(BUCKET_NAME)
                        .bucketPrefix(BUCKET_PREFIX)
                        .build())
                .build();
    }

    private InstanceStorageConfig prepareKinesisVideoStreamInstanceStorageConfig() {
        return InstanceStorageConfig.builder()
                .storageType(StorageType.KINESIS_VIDEO_STREAM)
                .kinesisVideoStreamConfig(KinesisVideoStreamConfig.builder()
                        .prefix(KINESIS_VIDEO_STREAM_PREFIX)
                        .retentionPeriodHours((int) RETENTION_PERIOD_HOURS)
                        .encryptionConfig(EncryptionConfig.builder()
                                .keyId(KMS_KEY_ID)
                                .encryptionType(KMS)
                                .build())
                        .build())
                .build();
    }

    private InstanceStorageConfig prepareKinesisStreamInstanceStorageConfig() {
        return InstanceStorageConfig.builder()
                .storageType(StorageType.KINESIS_STREAM)
                .kinesisStreamConfig(KinesisStreamConfig.builder()
                        .streamArn(KINESIS_STREAM_ARN)
                        .build())
                .build();
    }

    private InstanceStorageConfig prepareKinesisFirehoseInstanceStorageConfig() {
        return InstanceStorageConfig.builder()
                .storageType(StorageType.KINESIS_FIREHOSE)
                .kinesisFirehoseConfig(KinesisFirehoseConfig.builder()
                        .firehoseArn(KINESIS_FIREHOSE_ARN)
                        .build())
                .build();
    }
}
