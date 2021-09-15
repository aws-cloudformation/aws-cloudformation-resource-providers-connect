package software.amazon.connect.user;

import java.util.Objects;

public class ArnHelper {

    private static final String AGENT = "agent";
    private static final String USER_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/agent/[-a-zA-Z0-9]*$";
    private static final String SECURITY_PROFILE_ARN_FORMAT = "%s/security-profile/%s";
    private static final String ROUTING_PROFILE_ARN_FORMAT = "%s/routing-profile/%s";
    private static final String USER_HIERARCHY_GROUP_ARN_FORMAT = "%s/agent-group/%s";

    static String getInstanceArnFromUserArn(final String userArn) {
        return userArn.substring(0, userArn.indexOf(AGENT) - 1);
    }

    static boolean isValidUserArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(USER_ARN_PATTERN);
    }

    static String constructSecurityProfileArn(final String instanceArn, final String securityProfileId) {
        return String.format(SECURITY_PROFILE_ARN_FORMAT, instanceArn, securityProfileId);
    }

    static String constructRoutingProfileArn(final String instanceArn, final String routingProfileId) {
        return String.format(ROUTING_PROFILE_ARN_FORMAT, instanceArn, routingProfileId);
    }

    static String constructUserHierarchyGroupArn(final String instanceArn, final String hierarchyGroupId) {
        return String.format(USER_HIERARCHY_GROUP_ARN_FORMAT, instanceArn, hierarchyGroupId);
    }
}
