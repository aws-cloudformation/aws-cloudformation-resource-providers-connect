package software.amazon.connect.instancestorageconfig;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeInstanceStorageConfigRequest;
import software.amazon.awssdk.services.connect.model.InstanceStorageConfig;
import software.amazon.awssdk.services.connect.model.StorageType;
import software.amazon.connect.instancestorageconfig.EncryptionConfig;
import software.amazon.connect.instancestorageconfig.KinesisVideoStreamConfig;
import software.amazon.connect.instancestorageconfig.KinesisFirehoseConfig;
import software.amazon.connect.instancestorageconfig.KinesisStreamConfig;
import software.amazon.connect.instancestorageconfig.S3Config;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        this.logger = logger;

        logger.log(String.format("Invoked ReadHandler with accountId:%s, instanceArn:%s, resourceType:%s, associationId:%s",
                request.getAwsAccountId(), model.getInstanceArn(), model.getResourceType(), model.getAssociationId()));

        if (!ArnHelper.isValidInstanceArn(model.getInstanceArn())) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Instance Arn", model.getInstanceArn())));
        }
        requireNotNull(model.getResourceType(), RESOURCE_TYPE);
        requireNotNull(model.getAssociationId(), ASSOCIATION_ID);

        return proxy.initiate("connect::describeInstanceStorageConfig", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribeInstanceStorageConfigRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describeInstanceStorageConfig, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setInstanceStorageConfigProperties(model, response.storageConfig())));
    }

    private DescribeInstanceStorageConfigRequest translateToDescribeInstanceStorageConfigRequest(final ResourceModel model) {
        return DescribeInstanceStorageConfigRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .resourceType(model.getResourceType())
                .associationId(model.getAssociationId())
                .build();
    }

    private ResourceModel setInstanceStorageConfigProperties(final ResourceModel model, final InstanceStorageConfig storageConfig) {
        String resourceType = model.getResourceType();
        model.setStorageType(storageConfig.storageTypeAsString());
        switch(resourceType) {
            case CALL_RECORDINGS:
            case CHAT_TRANSCRIPTS:
            case SCHEDULED_REPORTS:
                setS3Properties(model, storageConfig.s3Config());
                break;
            case MEDIA_STREAMS:
                setKinesisVideoStreamProperties(model, storageConfig.kinesisVideoStreamConfig());
                break;
            case CONTACT_TRACE_RECORDS:
                if(KINESIS_STREAM.equals(storageConfig.storageTypeAsString())) {
                    setKinesisStreamProperties(model, storageConfig.kinesisStreamConfig());
                } else {
                    setKinesisFirehoseProperties(model, storageConfig.kinesisFirehoseConfig());
                }
                break;
            case AGENT_EVENTS:
                setKinesisStreamProperties(model, storageConfig.kinesisStreamConfig());
                break;
            default:
                throw new CfnInvalidRequestException(String.format(INVALID_RESOURCE_TYPE, resourceType));
        }

        return model;
    }

    private void setS3Properties(final ResourceModel model, final software.amazon.awssdk.services.connect.model.S3Config s3Config) {
        EncryptionConfig encryptionConfig = null;
        if(s3Config.encryptionConfig() != null) {
            encryptionConfig = EncryptionConfig
                    .builder()
                    .encryptionType(s3Config.encryptionConfig().encryptionTypeAsString())
                    .keyId(s3Config.encryptionConfig().keyId())
                    .build();
        }

        S3Config config = S3Config.builder()
                .bucketName(s3Config.bucketName())
                .bucketPrefix(s3Config.bucketPrefix())
                .encryptionConfig(encryptionConfig)
                .build();

        model.setS3Config(config);
    }

    private void setKinesisVideoStreamProperties(final ResourceModel model, final software.amazon.awssdk.services.connect.model.KinesisVideoStreamConfig kinesisVideoStreamConfig) {
        KinesisVideoStreamConfig config = KinesisVideoStreamConfig
                .builder()
                .prefix(kinesisVideoStreamConfig.prefix())
                .retentionPeriodHours(kinesisVideoStreamConfig.retentionPeriodHours().doubleValue())
                .encryptionConfig(EncryptionConfig.builder()
                        .encryptionType(kinesisVideoStreamConfig.encryptionConfig().encryptionTypeAsString())
                        .keyId(kinesisVideoStreamConfig.encryptionConfig().keyId())
                        .build())
                .build();
        model.setKinesisVideoStreamConfig(config);
    }

    private void setKinesisStreamProperties(final ResourceModel model, final software.amazon.awssdk.services.connect.model.KinesisStreamConfig kinesisStreamConfig) {
        KinesisStreamConfig config = KinesisStreamConfig
                .builder()
                .streamArn(kinesisStreamConfig.streamArn())
                .build();

        model.setKinesisStreamConfig(config);
    }

    private void setKinesisFirehoseProperties(final ResourceModel model, final software.amazon.awssdk.services.connect.model.KinesisFirehoseConfig kinesisFirehoseConfig) {
        KinesisFirehoseConfig config = KinesisFirehoseConfig
                .builder().firehoseArn(kinesisFirehoseConfig.firehoseArn())
                .build();
        model.setKinesisFirehoseConfig(config);
    }
}
