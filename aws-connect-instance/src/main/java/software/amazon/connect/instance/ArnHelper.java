package software.amazon.connect.instance;

import java.util.Objects;

public class ArnHelper {

    private static final String INSTANCE_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*$";

    static boolean isValidInstanceArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(INSTANCE_ARN_PATTERN);
    }
}
