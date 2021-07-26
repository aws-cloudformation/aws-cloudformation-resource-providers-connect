package software.amazon.connect.user;

import java.util.Objects;

public class ArnHelper {

    private static final String AGENT = "agent";
    private static final String USER_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/agent/[-a-zA-Z0-9]*$";

    static String getInstanceArnFromUserArn(final String userArn) {
        return userArn.substring(0, userArn.indexOf(AGENT) - 1);
    }

    static boolean isValidUserArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(USER_ARN_PATTERN);
    }
}
