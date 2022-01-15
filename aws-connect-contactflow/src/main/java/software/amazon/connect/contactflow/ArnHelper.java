package software.amazon.connect.contactflow;

import java.util.Objects;

public class ArnHelper {

    private static final String CONTACT_FLOW = "contact-flow";
    private static final String CONTACT_FLOW_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/contact-flow/[-a-zA-Z0-9]*$";

    static String getInstanceArnFromContactFlowArn(final String contactFlowArn) {
        return contactFlowArn.substring(0, contactFlowArn.indexOf(CONTACT_FLOW) - 1);
    }

    static boolean isValidContactFlowArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(CONTACT_FLOW_ARN_PATTERN);
    }
}
