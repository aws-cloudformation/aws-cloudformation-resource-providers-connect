package software.amazon.connect.quickconnect;

import java.util.Objects;

public class ArnHelper {

    private static final String TRANSFER_DESTINATION = "transfer-destination";
    private static final String QUICK_CONNECT_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/transfer-destination/[-a-zA-Z0-9]*$";
    private static final String USER_ARN_FORMAT = "%s/agent/%s";
    private static final String CONTACT_FLOW_ARN_FORMAT = "%s/contact-flow/%s";
    private static final String QUEUE_ARN_FORMAT = "%s/queue/%s";

    protected static String getInstanceArnFromQuickConnectArn(final String quickConnectArn) {
        return quickConnectArn.substring(0, quickConnectArn.indexOf(TRANSFER_DESTINATION) - 1);
    }

    protected static boolean isValidQuickConnectArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(QUICK_CONNECT_ARN_PATTERN);
    }

    protected static String constructUserArn(final String instanceArn, final String userId) {
        return String.format(USER_ARN_FORMAT, instanceArn, userId);
    }

    protected static String constructQueueArn(final String instanceArn, final String queueId) {
        return String.format(QUEUE_ARN_FORMAT, instanceArn, queueId);
    }

    protected static String constructContactFlowArn(final String instanceArn, final String contactFlowId) {
        return String.format(CONTACT_FLOW_ARN_FORMAT, instanceArn, contactFlowId);
    }
}
