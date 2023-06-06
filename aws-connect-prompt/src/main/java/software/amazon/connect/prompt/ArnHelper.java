package software.amazon.connect.prompt;

import java.util.Objects;

public class ArnHelper {
    private static final String PROMPT_OPERATION = "prompt";
    private static final String PROMPT_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/prompt/[-a-zA-Z0-9]*$";
    private static final String INSTANCE_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*$";

    static String getInstanceArnFromPromptArn(final String promptArn) {
        return promptArn.substring(0, promptArn.indexOf(PROMPT_OPERATION) - 1);
    }

    static boolean isValidPromptArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(PROMPT_ARN_PATTERN);
    }

    static boolean isValidInstanceArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(INSTANCE_ARN_PATTERN);
    }
}
