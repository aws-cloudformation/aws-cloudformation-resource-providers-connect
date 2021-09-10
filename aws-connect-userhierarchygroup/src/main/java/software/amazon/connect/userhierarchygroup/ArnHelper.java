package software.amazon.connect.userhierarchygroup;

import java.util.Objects;

public class ArnHelper {

    private static final String AGENT_GROUP = "agent-group";
    private static final String AGENT_GROUP_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/agent-group/[-a-zA-Z0-9]*$";

    static String getInstanceArnFromUserHierarchyGroupArn(final String userHierarchyGroupArn) {
        return userHierarchyGroupArn.substring(0, userHierarchyGroupArn.indexOf(AGENT_GROUP) - 1);
    }

    static boolean isValidUserHierarchyGroupArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(AGENT_GROUP_ARN_PATTERN);
    }

}
