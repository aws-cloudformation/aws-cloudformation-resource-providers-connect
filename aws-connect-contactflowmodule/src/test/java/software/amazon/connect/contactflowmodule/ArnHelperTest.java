package software.amazon.connect.contactflowmodule;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.CONTACT_FLOW_MODULE_ARN;

public class ArnHelperTest {

    @Test
    public void testGetInstanceArnFromContactFlowArn() {
        assertThat(ArnHelper.getInstanceArnFromContactFlowModuleArn(CONTACT_FLOW_MODULE_ARN)).isEqualTo(INSTANCE_ARN);
    }

    @Test
    public void testIsValidContactFlowArn_ValidArn() {
        assertTrue(ArnHelper.isValidContactFlowModuleArn(CONTACT_FLOW_MODULE_ARN));
    }


    @Test
    public void testIsValidContactFlowArn_InvalidNull() {
        assertFalse(ArnHelper.isValidContactFlowModuleArn(null));
    }
}
