package software.amazon.connect.contactflowmodule;

import java.util.Objects;

public class ArnHelper {

    private static final String CONTACT_FLOW_MODULE = "flow-module";
    private static final String CONTACT_FLOW_MODULE_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/flow-module/[-a-zA-Z0-9]*$";

    static String getInstanceArnFromContactFlowModuleArn(final String contactFlowModuleArn) {
        return contactFlowModuleArn.substring(0, contactFlowModuleArn.indexOf(CONTACT_FLOW_MODULE) - 1);
    }

    static boolean isValidContactFlowModuleArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(CONTACT_FLOW_MODULE_ARN_PATTERN);
    }
}
