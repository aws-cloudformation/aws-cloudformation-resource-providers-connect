package software.amazon.connect.phonenumber;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.INVALID_PHONE_NUMBER_ARN;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_ARN_ONE;

public class ArnHelperTest {
    @Test
    public void testIsValidHoursOfOperationArn_ValidArn() {
        assertTrue(ArnHelper.isValidPhoneNumberArn(PHONE_NUMBER_ARN_ONE));
    }

    @Test
    public void testIsValidHoursOfOperationArn_InvalidArn() {
        assertFalse(ArnHelper.isValidPhoneNumberArn(INVALID_PHONE_NUMBER_ARN));
    }

    @Test
    public void testIsValidHoursOfOperationArn_InvalidNull() {
        assertFalse(ArnHelper.isValidPhoneNumberArn(null));
    }
}