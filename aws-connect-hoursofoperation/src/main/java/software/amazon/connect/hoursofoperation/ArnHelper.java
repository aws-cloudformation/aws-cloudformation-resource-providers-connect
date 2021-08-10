package software.amazon.connect.hoursofoperation;

import java.util.Objects;

public class ArnHelper {

        private static final String HOURS_OF_OPERATION = "operating-hours";
        private static final String HOURS_OF_OPERATION_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/operating-hours/[-a-zA-Z0-9]*$";
        private static final String USER_ARN_FORMAT = "%s/agent/%s";
        private static final String CONTACT_FLOW_ARN_FORMAT = "%s/contact-flow/%s";
        private static final String QUEUE_ARN_FORMAT = "%s/queue/%s";

        static String getInstanceArnFromHoursOfOperationArn(final String hoursOfOperationArn) {
        return hoursOfOperationArn.substring(0, hoursOfOperationArn.indexOf(HOURS_OF_OPERATION) - 1);
    }

        static boolean isValidHoursOfOperationArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(HOURS_OF_OPERATION_ARN_PATTERN);
    }

}
