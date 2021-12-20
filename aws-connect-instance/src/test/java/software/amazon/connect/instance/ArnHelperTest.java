package software.amazon.connect.instance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.connect.instance.InstanceTestDataProvider.*;

public class ArnHelperTest {


    @Test
    public void testIsValidInstanceArn_ValidArn() {
        assertTrue(ArnHelper.isValidInstanceArn(INSTANCE_ARN));
    }

    @Test
    public void testIsValidInstanceArn_InvalidArn() {
        assertFalse(ArnHelper.isValidInstanceArn(INVALID_INSTANCE_ARN));
    }

    @Test
    public void testIsValidInstanceArn_InvalidNull() {
        assertFalse(ArnHelper.isValidInstanceArn(null));
    }

}
