package software.amazon.connect.instancestorageconfig;

public class InstanceStorageConfigTestDataProvider {
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:123456789012:instance/451dccf2-cd10-4843-bdd9-7f073a403e0d";
    protected static final String INSTANCE_ID = "451dccf2-cd10-4843-bdd9-7f073a403e0d";
    protected static final String INVALID_INSTANCE_ARN = "invalid-instance-arn";
    protected static final String ASSOCIATION_ID = "451dccf2-cd10-4843-bdd9-7f073a403e0d";

    public static final String CHAT_TRANSCRIPTS = "CHAT_TRANSCRIPTS";
    public static final String CALL_RECORDINGS = "CALL_RECORDINGS";
    public static final String SCHEDULED_REPORTS = "SCHEDULED_REPORTS";
    public static final String MEDIA_STREAMS = "MEDIA_STREAMS";
    public static final String CONTACT_TRACE_RECORDS = "CONTACT_TRACE_RECORDS";
    public static final String AGENT_EVENTS = "AGENT_EVENTS";

    public static final String S3 = "S3";
    public static final String KINESIS_VIDEO_STREAM = "KINESIS_VIDEO_STREAM";
    public static final String KINESIS_STREAM = "KINESIS_STREAM";
    public static final String KINESIS_FIREHOSE = "KINESIS_FIREHOSE";
    public static final String KMS = "KMS";


    public static final String S3_CONFIG = "S3Config";
    public static final String KINESIS_VIDEO_STREAM_CONFIG = "KinesisVideoStreamConfig";
    public static final String ENCRYPTION_CONFIG = "EncryptionConfig";
    public static final String KINESIS_STREAM_CONFIG = "KinesisStreamConfig";
    public static final String KINESIS_FIREHOSE_CONFIG = "KinesisFirehoseConfig";

    public static final String BUCKET_NAME = "bucket-name";
    public static final String BUCKET_PREFIX = "bucket-prefix";
    public static final String KINESIS_VIDEO_STREAM_PREFIX = "kvs-prefix";
    public static final double RETENTION_PERIOD_HOURS = 100;
    public static final String KMS_KEY_ID = "kms-key-arn";
    public static final String KINESIS_STREAM_ARN = "kinesis-stream-arn";
    public static final String KINESIS_FIREHOSE_ARN = "kinesis-firehose-arn";


}
