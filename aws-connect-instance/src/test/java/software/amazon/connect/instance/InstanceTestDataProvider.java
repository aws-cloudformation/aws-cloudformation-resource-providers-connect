package software.amazon.connect.instance;

public class InstanceTestDataProvider {
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:123456789012:instance/451dccf2-cd10-4843-bdd9-7f073a403e0d";
    protected static final String INSTANCE_ID = "451dccf2-cd10-4843-bdd9-7f073a403e0d";
    protected static final String INVALID_INSTANCE_ARN = "invalid-instance-arn";
    protected static final String INSTANCE_ALIAS = "test-instance-alias";
    protected static final String DIRECTORY_ID = "d-1234567890";

    protected static final String SAML = "SAML";
    protected static final String CONNECT_MANAGED = "CONNECT_MANAGED";
    protected static final String EXISTING_DIRECTORY = "EXISTING_DIRECTORY";

    public static final String INSTANCE_STATUS_CREATION_IN_PROGRESS = "CREATION_IN_PROGRESS";
    public static final String INSTANCE_STATUS_ACTIVE = "ACTIVE";
    public static final String INSTANCE_STATUS_CREATION_FAILED = "CREATION_FAILED";
    public static final String INSTANCE_STATUS_FAILURE_REASON = "internal server error";
}
