package software.amazon.connect.contactflow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.CONTACT_FLOW_ARN;

public class ArnHelperTest {

    @Test
    public void testGetInstanceArnFromContactFlowArn() {
        assertThat(ArnHelper.getInstanceArnFromContactFlowArn(CONTACT_FLOW_ARN)).isEqualTo(INSTANCE_ARN);
    }

    @Test
    public void testIsValidContactFlowArn_ValidArn() {
        assertTrue(ArnHelper.isValidContactFlowArn(CONTACT_FLOW_ARN));
    }


    @Test
    public void testIsValidContactFlowArn_InvalidNull() {
        assertFalse(ArnHelper.isValidContactFlowArn(null));
    }
}
