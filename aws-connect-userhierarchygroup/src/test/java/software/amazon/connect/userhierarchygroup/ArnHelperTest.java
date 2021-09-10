package software.amazon.connect.userhierarchygroup;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.USER_HIERARCHY_GROUP_ARN;

public class ArnHelperTest {
    @Test
    public void testGetInstanceArnFromUserHierarchyGroupArn_validArn() {
        assertThat(ArnHelper.getInstanceArnFromUserHierarchyGroupArn(USER_HIERARCHY_GROUP_ARN));
    }

    @Test
    public void testIsValidUserHierarchyGroupArn_validArn() {
        assertThat(ArnHelper.isValidUserHierarchyGroupArn(USER_HIERARCHY_GROUP_ARN));
    }

    @Test
    public void testIsValidUserHierarchyGroupArn_inValidArn() {
        assertThat(ArnHelper.isValidUserHierarchyGroupArn(INSTANCE_ARN));
    }
}
