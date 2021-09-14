package software.amazon.connect.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.connect.user.UserTestDataProvider.HIERARCHY_GROUP_ARN;
import static software.amazon.connect.user.UserTestDataProvider.HIERARCHY_GROUP_ID;
import static software.amazon.connect.user.UserTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.ROUTING_PROFILE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.ROUTING_PROFILE_ID;
import static software.amazon.connect.user.UserTestDataProvider.SECURITY_PROFILE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.SECURITY_PROFILE_ID;
import static software.amazon.connect.user.UserTestDataProvider.USER_ARN;

public class ArnHelperTest {

    @Test
    public void testGetInstanceArnFromUserArn() {
        assertThat(ArnHelper.getInstanceArnFromUserArn(USER_ARN)).isEqualTo(INSTANCE_ARN);
    }

    @Test
    public void testIsValidUserArn_ValidArn() {
        assertTrue(ArnHelper.isValidUserArn(USER_ARN));
    }

    @Test
    public void testIsValidUserArn_InvalidArn() {
        assertFalse(ArnHelper.isValidUserArn(INSTANCE_ARN));
    }

    @Test
    public void testIsValidUserArn_InvalidNull() {
        assertFalse(ArnHelper.isValidUserArn(null));
    }

    @Test
    public void testConstructSecurityProfileArn() {
        assertThat(ArnHelper.constructSecurityProfileArn(INSTANCE_ARN, SECURITY_PROFILE_ID)).isEqualTo(SECURITY_PROFILE_ARN);
    }

    @Test
    public void testConstructRoutingProfileArn() {
        assertThat(ArnHelper.constructRoutingProfileArn(INSTANCE_ARN, ROUTING_PROFILE_ID)).isEqualTo(ROUTING_PROFILE_ARN);
    }

    @Test
    public void testConstructUserHierarchyGroupArn() {
        assertThat(ArnHelper.constructUserHierarchyGroupArn(INSTANCE_ARN, HIERARCHY_GROUP_ID)).isEqualTo(HIERARCHY_GROUP_ARN);
    }
}
