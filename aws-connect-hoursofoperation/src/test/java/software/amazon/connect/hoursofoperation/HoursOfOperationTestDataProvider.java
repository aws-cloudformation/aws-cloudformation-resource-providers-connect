package software.amazon.connect.hoursofoperation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.connect.model.HoursOfOperation;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class HoursOfOperationTestDataProvider {
    protected static final String HOURS_OF_OPERATION_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/operating-hours/hoursOfOperationID";
    protected static final String INVALID_HOURS_OF_OPERATION_ARN = "invalidHoursOfOperationArn";
    protected static final String HOURS_OF_OPERATION_ID = "hoursOfOperationId";
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String INSTANCE_ARN_TWO = "arn:aws:connect:us-west-2:111111111111:instance/instanceIdTwo";
    protected static final String HOURS_OF_OPERATION_NAME_ONE = "hoursNameOne";
    protected static final String HOURS_OF_OPERATION_DESCRIPTION_ONE = "hoursDescriptionOne";
    protected static final String HOURS_OF_OPERATION_NAME_TWO = "hoursNameTwo";
    protected static final String HOURS_OF_OPERATION_DESCRIPTION_TWO = "hoursDescriptionTwo";
    protected static final Integer HOURS_ONE = 12;
    protected static final Integer MINUTES_ONE = 30;
    protected static final Integer HOURS_TWO = 10;
    protected static final Integer MINUTES_TWO = 50;
    protected static final Integer HOURS_THREE = 20;
    protected static final Integer MINUTES_THREE = 40;
    protected static final String DAY_ONE = "MONDAY";
    protected static final String DAY_TWO = "TUESDAY";
    protected static final String DAY_THREE = "WEDNESDAY";
    protected static final String VALID_TAG_KEY_ONE = "TagKeyOne";
    protected static final String TIME_ZONE_ONE = "America/New_York";
    protected static final String TIME_ZONE_TWO = "Pacific/Midway";
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
    protected static final HoursOfOperationConfig HOURS_OF_OPERATION_CONFIG_THREE = HoursOfOperationConfig.builder()
            .day(DAY_THREE)
            .startTime(getHoursOfOperationTimeSLice(HOURS_THREE, MINUTES_THREE))
            .endTime(getHoursOfOperationTimeSLice(HOURS_THREE, MINUTES_THREE))
            .build();
    protected static final Map<String, String> TAGS_ONE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE);
    protected static final Map<String, String> TAGS_TWO = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE,
            VALID_TAG_KEY_TWO, VALID_TAG_VALUE_TWO);
    protected static final Map<String, String> TAGS_THREE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE,
            VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);
    protected static final Set<Tag> TAGS_SET_ONE = convertTagMapToSet();

    protected static ResourceModel buildHoursOfOperationDesiredStateResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .timeZone(TIME_ZONE_ONE)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .build();
    }

    protected static ResourceModel buildHoursOfOperationPreviousStateResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_TWO)
                .description(HOURS_OF_OPERATION_DESCRIPTION_TWO)
                .timeZone(TIME_ZONE_TWO)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_TWO, HOURS_OF_OPERATION_CONFIG_THREE))
                .build();
    }

    protected static List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> getHoursOfOperationConfig() {
        List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> hoursOfOperationConfigList = new ArrayList<>();
        hoursOfOperationConfigList.add(getHoursConfigs());
        return hoursOfOperationConfigList;
    }

    private static software.amazon.awssdk.services.connect.model.HoursOfOperationConfig getHoursConfigs() {
        return software.amazon.awssdk.services.connect.model.HoursOfOperationConfig.builder()
                .day(DAY_ONE)
                .startTime(getHoursOfOperationTimeSlices(HOURS_ONE, MINUTES_ONE))
                .endTime(getHoursOfOperationTimeSlices(HOURS_TWO, MINUTES_TWO))
                .build();
    }

    private static software.amazon.awssdk.services.connect.model.HoursOfOperationTimeSlice getHoursOfOperationTimeSlices(final int hours, final int minutes) {
        return software.amazon.awssdk.services.connect.model.HoursOfOperationTimeSlice.builder()
                .hours(hours)
                .minutes(minutes)
                .build();
    }

    protected static Set<HoursOfOperationConfig> getConfig(final HoursOfOperationConfig hoursOfOperationConfig1, final HoursOfOperationConfig hoursOfOperationConfig2) {
        Set<HoursOfOperationConfig> hoursOfOperationConfigSet = new HashSet<>();
        hoursOfOperationConfigSet.add(hoursOfOperationConfig1);
        hoursOfOperationConfigSet.add(hoursOfOperationConfig2);
        return hoursOfOperationConfigSet;
    }

    protected static void validateConfig(final List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> hoursOfOperationConfig) {
        int index = 0;
        for (software.amazon.connect.hoursofoperation.HoursOfOperationConfig config : getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO)) {
            assertThat(hoursOfOperationConfig.get(index).day().toString()).isEqualTo(config.getDay());
            assertThat(hoursOfOperationConfig.get(index).startTime().hours()).isEqualTo(config.getStartTime().getHours());
            assertThat(hoursOfOperationConfig.get(index).startTime().minutes()).isEqualTo(config.getStartTime().getMinutes());
            assertThat(hoursOfOperationConfig.get(index).endTime().hours()).isEqualTo(config.getEndTime().getHours());
            assertThat(hoursOfOperationConfig.get(index).endTime().minutes()).isEqualTo(config.getEndTime().getMinutes());
            index += 1;
        }
    }

    private static HoursOfOperationTimeSlice getHoursOfOperationTimeSLice(final int hours, final int minutes) {
        return HoursOfOperationTimeSlice.builder()
                .hours(hours)
                .minutes(minutes)
                .build();
    }

    private static Set<Tag> convertTagMapToSet() {
        Set<Tag> tags = Sets.newHashSet();
        TAGS_ONE.forEach((key, value) -> tags.add(Tag.builder().key(key).value(value).build()));
        return tags;
    }
}
