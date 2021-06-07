package software.amazon.connect.quickconnect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.CONTACT_FLOW_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.CONTACT_FLOW_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUEUE_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUEUE_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUICK_CONNECT_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.USER_ARN;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.USER_ID;

public class ArnHelperTest {

    @Test
    public void testGetInstanceArnFromQuickConnectArn() {
        assertThat(ArnHelper.getInstanceArnFromQuickConnectArn(QUICK_CONNECT_ARN)).isEqualTo(INSTANCE_ARN);
    }

    @Test
    public void testIsValidQuickConnectArn_ValidArn() {
        assertTrue(ArnHelper.isValidQuickConnectArn(QUICK_CONNECT_ARN));
    }

    @Test
    public void testIsValidQuickConnectArn_InvalidArn() {
        assertFalse(ArnHelper.isValidQuickConnectArn(INSTANCE_ARN));
    }

    @Test
    public void testIsValidQuickConnectArn_InvalidNull() {
        assertFalse(ArnHelper.isValidQuickConnectArn(null));
    }

    @Test
    public void testConstructUserArn() {
        assertThat(ArnHelper.constructUserArn(INSTANCE_ARN, USER_ID)).isEqualTo(USER_ARN);
    }

    @Test
    public void testConstructQueueArn() {
        assertThat(ArnHelper.constructQueueArn(INSTANCE_ARN, QUEUE_ID)).isEqualTo(QUEUE_ARN);
    }

    @Test
    public void testConstructContactFlowArn() {
        assertThat(ArnHelper.constructContactFlowArn(INSTANCE_ARN, CONTACT_FLOW_ID)).isEqualTo(CONTACT_FLOW_ARN);
    }
}
