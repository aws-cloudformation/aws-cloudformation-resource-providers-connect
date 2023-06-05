package software.amazon.connect.tasktemplate;

import java.util.Objects;

public class ArnHelper {

    private static final String TASK_TEMPLATE = "task-template";
    private static final String TASK_TEMPLATE_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/task-template/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final String INSTANCE_ARN_PATTERN = "^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*$";

    static String getInstanceArnFromTaskTemplateArn(final String taskTemplateArn) {
        return taskTemplateArn.substring(0, taskTemplateArn.indexOf(TASK_TEMPLATE) - 1);
    }


    public static String getIdFromArn(String arn){
        int pos = arn.lastIndexOf("/");
        return arn.substring(pos + 1);
    }

    static boolean isValidTaskTemplateArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(TASK_TEMPLATE_ARN_PATTERN);
    }

    static boolean isValidInstanceArn(final String arn) {
        if (Objects.isNull(arn)) {
            return false;
        }
        return arn.matches(INSTANCE_ARN_PATTERN);
    }
}
