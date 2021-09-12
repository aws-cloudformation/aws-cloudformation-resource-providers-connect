package software.amazon.connect.hoursofoperation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ARN;

public class ArnHelperTest {

    @Test
    public void testGetInstanceArnFromHoursOfOperationArn() {
        assertThat(ArnHelper.getInstanceArnFromHoursOfOperationArn(HOURS_OF_OPERATION_ARN)).isEqualTo(INSTANCE_ARN);
    }

    @Test
    public void testIsValidHoursOfOperationArn_ValidArn() {
        assertTrue(ArnHelper.isValidHoursOfOperationArn(HOURS_OF_OPERATION_ARN));
    }

    @Test
    public void testIsValidHoursOfOperationArn_InvalidArn() {
        assertFalse(ArnHelper.isValidHoursOfOperationArn(INSTANCE_ARN));
    }

    @Test
    public void testIsValidHoursOfOperationArn_InvalidNull() {
        assertFalse(ArnHelper.isValidHoursOfOperationArn(null));
    }
}

