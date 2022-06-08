package software.amazon.connect.phonenumber;

import java.util.Objects;

public class ArnHelper {
    private static final String PHONE_NUMBER_ARN_PATTERN = "^arn:aws:connect:[-a-z0-9]*:[0-9]{12}:phone-number/[-a-zA-Z0-9]*$";

    static boolean isValidPhoneNumberArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(PHONE_NUMBER_ARN_PATTERN);
    }
}