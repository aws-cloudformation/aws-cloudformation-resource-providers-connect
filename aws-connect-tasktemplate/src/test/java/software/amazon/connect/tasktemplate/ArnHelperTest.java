package software.amazon.connect.tasktemplate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArnHelperTest{


    @Test
    public void testGetInstanceArnFromTaskTemplateArn() {
        assertThat(ArnHelper.getInstanceArnFromTaskTemplateArn(BaseTest.TASK_TEMPLATE_ARN)).isEqualTo(BaseTest.INSTANCE_ARN);
    }

    @Test
    public void testIsValidTaskTemplateArn_ValidArn() {
        assertTrue(ArnHelper.isValidTaskTemplateArn(BaseTest.TASK_TEMPLATE_ARN));
    }

    @Test
    public void testIsValidTaskTemplateArn_InvalidFormatArn() {
        assertFalse(ArnHelper.isValidTaskTemplateArn(BaseTest.INVALID_FORMAT_TASK_TEMPLATE_ARN));
        assertFalse(ArnHelper.isValidTaskTemplateArn(BaseTest.INVALID_FORMAT_TASK_TEMPLATE_ARN_2));
    }


    @Test
    public void testIsValidTaskTemplateArn_InvalidNull() {
        assertFalse(ArnHelper.isValidTaskTemplateArn(null));
    }

    @Test
    public static void testGetTaskTemplateIdFromArn(){
        assertEquals(ArnHelper.getIdFromArn(BaseTest.TASK_TEMPLATE_ARN), BaseTest.TASK_TEMPLATE_ID);
    }
}