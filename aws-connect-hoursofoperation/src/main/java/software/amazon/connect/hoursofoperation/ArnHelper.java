package software.amazon.connect.hoursofoperation;

import java.util.Objects;

public class ArnHelper {

    private static final String HOURS_OF_OPERATION = "operating-hours";
    private static final String HOURS_OF_OPERATION_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/operating-hours/[-a-zA-Z0-9]*$";

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
