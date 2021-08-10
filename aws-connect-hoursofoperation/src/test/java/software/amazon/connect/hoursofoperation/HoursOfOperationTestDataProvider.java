package software.amazon.connect.hoursofoperation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.connect.model.HoursOfOperation;
import software.amazon.awssdk.services.connect.model.DescribeHoursOfOperationResponse;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

public class HoursOfOperationTestDataProvider {
    protected static final String HOURS_OF_OPERATION_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/operating-hours/hoursOfOperationID";
    protected static final String HOURS_OF_OPERATION_ID = "hoursOfOperationId";
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String HOURS_OF_OPERATION_NAME_ONE = "hoursNameOne";
    protected static final String HOURS_OF_OPERATION_DESCRIPTION_ONE = "hoursDescriptionOne";
    protected static final String HOURS_OF_OPERATION_NAME_TWO = "hoursNameTwo";
    protected static final String HOURS_OF_OPERATION_DESCRIPTION_TWO = "hoursDescriptionTwo";
    protected static final String HOURS_OF_OPERATION_NAME_THREE = "hoursNameThree";
    protected static final String HOURS_OF_OPERATION_DESCRIPTION_THREE = "hoursDescriptionThree";
    protected static final String HOURS_ONE = "12";
    protected static final String MINUTES_ONE = "30";
    protected static final String HOURS_TWO = "10";
    protected static final String MINUTES_TWO = "50";
    protected static final String DAY_ONE = "MONDAY";
    protected static final String DAY_TWO = "TUESDAY";
    protected static final String DAY_THREE = "FRIDAY";
    protected static final String VALID_TAG_KEY_ONE = "TagKeyOne";
    protected static final String TIME_ZONE_ONE = "America/New_York";
    protected static final String TIME_ZONE_TWO = "America/Nome";
    protected static final String VALID_TAG_VALUE_ONE = "A";
    protected static final String VALID_TAG_KEY_TWO = "TagKeyTwo";
    protected static final String VALID_TAG_VALUE_TWO = "B";
    protected static final String VALID_TAG_KEY_THREE = "TagKeyThree";
    protected static final String VALID_TAG_VALUE_THREE = "C";
    protected static final HoursOfOperationConfig HOURS_OF_OPERATION_CONFIG_ONE = HoursOfOperationConfig.builder()
            .day(DAY_ONE)
            .startTime(getHoursOfOperationTimeSLice(HOURS_ONE, MINUTES_ONE))
            .endTime(getHoursOfOperationTimeSLice(HOURS_ONE, MINUTES_ONE))
            .build();
    protected static final HoursOfOperationConfig HOURS_OF_OPERATION_CONFIG_TWO = HoursOfOperationConfig.builder()
            .day(DAY_TWO)
            .startTime(getHoursOfOperationTimeSLice(HOURS_TWO, MINUTES_TWO))
            .endTime(getHoursOfOperationTimeSLice(HOURS_TWO, MINUTES_TWO))
            .build();
    protected static final Map<String, String> TAGS_ONE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE);
    protected static final Map<String, String> TAGS_TWO = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE,
            VALID_TAG_KEY_TWO, VALID_TAG_VALUE_TWO);
    protected static final Map<String, String> TAGS_THREE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE,
            VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);
    protected static final Set<Tag> TAGS_SET_ONE = convertTagMapToSet(TAGS_ONE);
    protected static final Set<Tag> TAGS_SET_TWO = convertTagMapToSet(TAGS_TWO);


    protected static ResourceModel buildHoursOfOperationResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .timeZone(TIME_ZONE_ONE)
                .config(getConfig())
                .build();
    }

    protected static ResourceModel buildHoursOfOperationResourceModelInvalidArn() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn("HOURS_OF_OPERATION_ARN_INVALID")
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .timeZone(TIME_ZONE_ONE)
                .config(getConfig())
                .build();
    }

    protected static HoursOfOperation getDescribeHoursOfOperationResponseObject() {

        return HoursOfOperation.builder()
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .hoursOfOperationId(HOURS_OF_OPERATION_ID)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .config(getHoursOfOperationConfig())
                .timeZone(TIME_ZONE_ONE)
                .tags(TAGS_ONE)
                .build();

    }

    protected static HoursOfOperation getDescribeHoursOfOperationResponseObjectInvalidArn() {

        return HoursOfOperation.builder()
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .hoursOfOperationId(HOURS_OF_OPERATION_ID)
                .hoursOfOperationArn("HOURS_OF_OPERATION_ARN_INVALID")
                .config(getHoursOfOperationConfig())
                .timeZone(TIME_ZONE_ONE)
                .tags(TAGS_ONE)
                .build();

    }


    private static List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> getHoursOfOperationConfig() {
        List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> hoursOfOperationConfigList = new ArrayList<>();
        hoursOfOperationConfigList.add(getHoursConfigs(DAY_ONE));
        return hoursOfOperationConfigList;
    }

    private static software.amazon.awssdk.services.connect.model.HoursOfOperationConfig getHoursConfigs(String day) {
        return software.amazon.awssdk.services.connect.model.HoursOfOperationConfig.builder()
                .day(day)
                .startTime(getHoursOfOperationTimeSlices(HOURS_ONE, MINUTES_ONE))
                .endTime(getHoursOfOperationTimeSlices(HOURS_TWO, MINUTES_TWO))
                .build();
    }

    private static software.amazon.awssdk.services.connect.model.HoursOfOperationTimeSlice getHoursOfOperationTimeSlices(String hours, String minutes) {

        return software.amazon.awssdk.services.connect.model.HoursOfOperationTimeSlice.builder()
                .hours(Integer.parseInt(hours))
                .minutes(Integer.parseInt(minutes))
                .build();
    }

    protected static Set<HoursOfOperationConfig> getConfig() {
        Set<HoursOfOperationConfig> hoursOfOperationConfigSet = new HashSet<>();
        hoursOfOperationConfigSet.add(HOURS_OF_OPERATION_CONFIG_ONE);
        hoursOfOperationConfigSet.add(HOURS_OF_OPERATION_CONFIG_TWO);
        return hoursOfOperationConfigSet;
    }

    private static HoursOfOperationTimeSlice getHoursOfOperationTimeSLice(String hours, String minutes) {
        return HoursOfOperationTimeSlice.builder()
                .hours(hours)
                .minutes(minutes)
                .build();
    }

    private static Set<Tag> convertTagMapToSet(Map<String, String> tagMap) {
        Set<Tag> tags = Sets.newHashSet();
        if (tagMap != null) {
            tagMap.forEach((key, value) -> tags.add(Tag.builder().key(key).value(value).build()));
        }
        return tags;
    }
}
