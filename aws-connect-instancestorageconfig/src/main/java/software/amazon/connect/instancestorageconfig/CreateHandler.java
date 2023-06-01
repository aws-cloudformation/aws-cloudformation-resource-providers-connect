package software.amazon.connect.instancestorageconfig;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.AssociateInstanceStorageConfigRequest;
import software.amazon.awssdk.services.connect.model.AssociateInstanceStorageConfigResponse;
import software.amazon.awssdk.services.connect.model.EncryptionConfig;
import software.amazon.awssdk.services.connect.model.InstanceStorageConfig;
import software.amazon.awssdk.services.connect.model.S3Config;
import software.amazon.awssdk.services.connect.model.StorageType;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

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

        logger.log(String.format("Invoked CreateHandler with accountId:%s, instanceArn:%s, resourceType:%s, storageType:%s",
                request.getAwsAccountId(), model.getInstanceArn(), model.getResourceType(), model.getStorageType()));

        if (!ArnHelper.isValidInstanceArn(model.getInstanceArn())) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Instance Arn", model.getInstanceArn())));
        }

        return proxy.initiate("connect::associateInstanceStorageConfig", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToAssociateInstanceStorageConfigRequest(resourceModel))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::associateInstanceStorageConfig, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setInstanceStorageConfigIdentifier(model, response)));
    }

    private AssociateInstanceStorageConfigRequest translateToAssociateInstanceStorageConfigRequest(ResourceModel model) {
        String resourceType = model.getResourceType();
        String storageType = model.getStorageType();
        InstanceStorageConfig storageConfig;
        switch(resourceType) {
            case CALL_RECORDINGS:
            case CHAT_TRANSCRIPTS:
            case SCHEDULED_REPORTS:
                validateStorageTypesForResourceType(resourceType, storageType, S3);
                requireNotNull(model.getS3Config(), S3_CONFIG);
                requireNullForStorageType(model.getKinesisVideoStreamConfig(), KINESIS_VIDEO_STREAM_CONFIG, storageType);
                requireNullForStorageType(model.getKinesisStreamConfig(), KINESIS_STREAM_CONFIG, storageType);
                requireNullForStorageType(model.getKinesisFirehoseConfig(), KINESIS_FIREHOSE_CONFIG, storageType);

                storageConfig = translateToS3StorageConfig(model.getS3Config());
                break;
            case MEDIA_STREAMS:
                validateStorageTypesForResourceType(resourceType, storageType, KINESIS_VIDEO_STREAM);
                requireNotNull(model.getKinesisVideoStreamConfig(), KINESIS_VIDEO_STREAM_CONFIG);
                requireNotNull(model.getKinesisVideoStreamConfig().getEncryptionConfig(), ENCRYPTION_CONFIG);
                requireNullForStorageType(model.getS3Config(), S3_CONFIG, storageType);
                requireNullForStorageType(model.getKinesisStreamConfig(), KINESIS_STREAM_CONFIG, storageType);
                requireNullForStorageType(model.getKinesisFirehoseConfig(), KINESIS_FIREHOSE_CONFIG, storageType);

                storageConfig = translateToKinesisVideoStreamStorageConfig(model.getKinesisVideoStreamConfig());
                break;
            case CONTACT_TRACE_RECORDS:
                validateStorageTypesForResourceType(resourceType, storageType, KINESIS_STREAM, KINESIS_FIREHOSE);
                requireNullForStorageType(model.getS3Config(), S3_CONFIG, storageType);
                requireNullForStorageType(model.getKinesisVideoStreamConfig(), KINESIS_VIDEO_STREAM_CONFIG, storageType);

                if(KINESIS_STREAM.equals(storageType)) {
                    storageConfig = translateToKinesisStreamStorageConfig(model.getKinesisStreamConfig());
                } else {
                    storageConfig = translateToKinesisFirehoseStorageConfig(model.getKinesisFirehoseConfig());
                }
                break;
            case AGENT_EVENTS:
                validateStorageTypesForResourceType(resourceType, storageType, KINESIS_STREAM);
                requireNotNull(model.getKinesisStreamConfig(), KINESIS_STREAM_CONFIG);
                requireNotNull(model.getKinesisStreamConfig(), KINESIS_STREAM_CONFIG);
                requireNullForStorageType(model.getS3Config(), S3_CONFIG, storageType);
                requireNullForStorageType(model.getKinesisVideoStreamConfig(), KINESIS_VIDEO_STREAM_CONFIG, storageType);
                requireNullForStorageType(model.getKinesisFirehoseConfig(), KINESIS_FIREHOSE_CONFIG, storageType);

                storageConfig = translateToKinesisStreamStorageConfig(model.getKinesisStreamConfig());
                break;
            default:
                throw new CfnInvalidRequestException(String.format(INVALID_RESOURCE_TYPE, resourceType));
        }

        return AssociateInstanceStorageConfigRequest.builder()
                .instanceId(model.getInstanceArn())
                .resourceType(resourceType)
                .storageConfig(storageConfig)
                .build();
    }

    private InstanceStorageConfig translateToS3StorageConfig(software.amazon.connect.instancestorageconfig.S3Config s3Config) {
        EncryptionConfig encryptionConfig = null;
        if(s3Config.getEncryptionConfig() != null) {
            encryptionConfig = EncryptionConfig
                    .builder()
                    .encryptionType(s3Config.getEncryptionConfig().getEncryptionType())
                    .keyId(s3Config.getEncryptionConfig().getKeyId())
                    .build();
        }

        return InstanceStorageConfig.builder()
                .storageType(StorageType.S3)
                .s3Config(S3Config.builder()
                        .bucketName(s3Config.getBucketName())
                        .bucketPrefix(s3Config.getBucketPrefix())
                        .encryptionConfig(encryptionConfig)
                        .build())
                .build();
    }

    private InstanceStorageConfig translateToKinesisVideoStreamStorageConfig(software.amazon.connect.instancestorageconfig.KinesisVideoStreamConfig kinesisVideoStreamConfig) {
        return InstanceStorageConfig.builder()
                .storageType(StorageType.KINESIS_VIDEO_STREAM)
                .kinesisVideoStreamConfig(software.amazon.awssdk.services.connect.model.KinesisVideoStreamConfig.builder()
                        .prefix(kinesisVideoStreamConfig.getPrefix())
                        .retentionPeriodHours(kinesisVideoStreamConfig.getRetentionPeriodHours().intValue())
                        .encryptionConfig(EncryptionConfig.builder()
                                .encryptionType(kinesisVideoStreamConfig.getEncryptionConfig().getEncryptionType())
                                .keyId(kinesisVideoStreamConfig.getEncryptionConfig().getKeyId())
                                .build())
                        .build())
                .build();
    }

    private InstanceStorageConfig translateToKinesisStreamStorageConfig(KinesisStreamConfig kinesisStreamConfig) {
        return InstanceStorageConfig.builder()
                .storageType(StorageType.KINESIS_STREAM)
                .kinesisStreamConfig(software.amazon.awssdk.services.connect.model.KinesisStreamConfig.builder()
                        .streamArn(kinesisStreamConfig.getStreamArn())
                        .build())
                .build();
    }

    private InstanceStorageConfig translateToKinesisFirehoseStorageConfig(KinesisFirehoseConfig kinesisFirehoseConfig) {
        return InstanceStorageConfig.builder()
                .storageType(StorageType.KINESIS_FIREHOSE)
                .kinesisFirehoseConfig(software.amazon.awssdk.services.connect.model.KinesisFirehoseConfig.builder()
                        .firehoseArn(kinesisFirehoseConfig.getFirehoseArn())
                        .build())
                .build();
    }

    private ResourceModel setInstanceStorageConfigIdentifier(final ResourceModel model, final AssociateInstanceStorageConfigResponse associateInstanceStorageConfigResponse) {
        model.setAssociationId(associateInstanceStorageConfigResponse.associationId());
        return model;
    }
}
