package software.amazon.connect.hoursofoperation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.platform.commons.util.StringUtils;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationOverridesResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.connect.hoursofoperation.BaseHandlerStd.translateToHOOPOverrideConfigModel;

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
    protected static final String HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION = "Test description for override";
    protected static final String HOURS_OF_OPERATION_OVERRIDE_ID_ONE = "hoursOfOperationOverrideIdOne";
    protected static final String HOURS_OF_OPERATION_OVERRIDE_ID_TWO = "hoursOfOperationOverrideIdTwo";
    protected static final String HOURS_OF_OPERATION_OVERRIDE_ID_THREE = "hoursOfOperationOverrideIdThree";
    protected static final Integer HOURS_ONE = 12;
    protected static final Integer MINUTES_ONE = 30;
    protected static final Integer HOURS_TWO = 10;
    protected static final Integer MINUTES_TWO = 50;
    protected static final Integer HOURS_THREE = 20;
    protected static final Integer MINUTES_THREE = 40;
    protected static final String DAY_ONE = "MONDAY";
    protected static final String DAY_TWO = "TUESDAY";
    protected static final String TUESDAY = "TUESDAY";
    protected static final String WEDNESDAY = "WEDNESDAY";
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
    public static final String OVERRIDE_NAME_ONE = "create-override-one";
    public static final String OVERRIDE_NAME_TWO = "create-override-two";
    public static final String OVERRIDE_NAME_THREE = "create-override-three";
    public static final Integer OVERRIDE_TIMESLICE_HOUR_9 = 9;
    public static final Integer OVERRIDE_TIMESLICE_HOUR_17 = 17;
    public static final Integer OVERRIDE_TIMESLICE_HOUR_15 = 15;
    public static final Integer OVERRIDE_TIMESLICE_HOUR_13 = 13;
    public static final Integer OVERRIDE_TIMESLICE_MIN_0 = 0;
    public static final String NEXT_TOKEN = "12345-67467-765433";
    private static final Date FROM_DATE = getFromDate(1, 1);
    private static final Date TILL_DATE = getFromDate(10, 1);
    public static final String OVERRIDE_EFFECTIVE_FROM = new SimpleDateFormat("yyyy-MM-dd").format(FROM_DATE);
    public static final String OVERRIDE_EFFECTIVE_TILL = new SimpleDateFormat("yyyy-MM-dd").format(TILL_DATE);

    protected static final HoursOfOperationOverrideConfig HOURS_OF_OPERATION_OVERRIDE_CONFIG_ONE = HoursOfOperationOverrideConfig.builder()
            .startTime(getOverrideTimeSlice(OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_MIN_0))
            .endTime(getOverrideTimeSlice(OVERRIDE_TIMESLICE_HOUR_17, OVERRIDE_TIMESLICE_MIN_0))
            .day(TUESDAY)
            .build();

    protected static final HoursOfOperationOverrideConfig HOURS_OF_OPERATION_OVERRIDE_CONFIG_TWO = HoursOfOperationOverrideConfig.builder()
            .startTime(getOverrideTimeSlice(OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_MIN_0))
            .endTime(getOverrideTimeSlice(OVERRIDE_TIMESLICE_HOUR_15, OVERRIDE_TIMESLICE_MIN_0))
            .day(WEDNESDAY)
            .build();

    protected static final HoursOfOperationOverrideConfig HOURS_OF_OPERATION_OVERRIDE_CONFIG_THREE = HoursOfOperationOverrideConfig.builder()
            .startTime(getOverrideTimeSlice(OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_MIN_0))
            .endTime(getOverrideTimeSlice(OVERRIDE_TIMESLICE_HOUR_13, OVERRIDE_TIMESLICE_MIN_0))
            .day(DAY_ONE)
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

    protected static ResourceModel buildHOOPOverridePreviousStateResourceModel() {
        List<HoursOfOperationOverride> overrides = new ArrayList<>();
        overrides.add(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_TWO, HOURS_OF_OPERATION_OVERRIDE_ID_TWO, OVERRIDE_NAME_TWO));

        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_TWO)
                .description(HOURS_OF_OPERATION_DESCRIPTION_TWO)
                .timeZone(TIME_ZONE_TWO)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_TWO, HOURS_OF_OPERATION_CONFIG_THREE))
                .hoursOfOperationOverrides(overrides)
                .build();
    }

    protected static ResourceModel buildHOOPOverridesPreviousStateResourceModel() {
        List<HoursOfOperationOverride> overrides = new ArrayList<>();
        overrides.add(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_ONE, HOURS_OF_OPERATION_OVERRIDE_ID_ONE, OVERRIDE_NAME_ONE));
        overrides.add(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_TWO, HOURS_OF_OPERATION_OVERRIDE_ID_TWO, OVERRIDE_NAME_TWO));

        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_TWO)
                .description(HOURS_OF_OPERATION_DESCRIPTION_TWO)
                .timeZone(TIME_ZONE_TWO)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_TWO, HOURS_OF_OPERATION_CONFIG_THREE))
                .hoursOfOperationOverrides(overrides)
                .build();
    }

    protected static ResourceModel buildHOOPOverrideDesiredStateResourceModel() {
        List<HoursOfOperationOverride> overrides = new ArrayList<>();
        overrides.add(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_ONE, HOURS_OF_OPERATION_OVERRIDE_ID_ONE, OVERRIDE_NAME_ONE));

        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .timeZone(TIME_ZONE_ONE)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .hoursOfOperationOverrides(overrides)
                .build();
    }

    protected static ResourceModel buildHOOPOverrideDesiredStateResourceModelThree() {
        List<HoursOfOperationOverride> overrides = new ArrayList<>();
        overrides.add(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_THREE, HOURS_OF_OPERATION_OVERRIDE_ID_THREE, OVERRIDE_NAME_THREE));

        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .timeZone(TIME_ZONE_ONE)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .hoursOfOperationOverrides(overrides)
                .build();
    }

    protected static ResourceModel buildHOOPEmptyOverrideDesiredStateResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .name(HOURS_OF_OPERATION_NAME_ONE)
                .description(HOURS_OF_OPERATION_DESCRIPTION_ONE)
                .timeZone(TIME_ZONE_ONE)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .hoursOfOperationOverrides(new ArrayList<>())
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

    public static HoursOfOperationOverride getOverride(final HoursOfOperationOverrideConfig overrideConfig, String overrideId, String OverrideName) {
        Set<HoursOfOperationOverrideConfig> configSet = new HashSet<>();
        configSet.add(overrideConfig);

        return HoursOfOperationOverride.builder()
                .hoursOfOperationOverrideId(overrideId)
                .overrideName(OverrideName)
                .overrideDescription(HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION)
                .overrideConfig(configSet)
                .effectiveFrom(OVERRIDE_EFFECTIVE_FROM)
                .effectiveTill(OVERRIDE_EFFECTIVE_TILL)
                .build();
    }

    public static software.amazon.awssdk.services.connect.model.HoursOfOperationOverride getHoursOfOperationOverrideThree() {
        return software.amazon.awssdk.services.connect.model.HoursOfOperationOverride.builder()
                .hoursOfOperationId(HOURS_OF_OPERATION_ARN)
                .hoursOfOperationOverrideId(HOURS_OF_OPERATION_OVERRIDE_ID_THREE)
                .name(OVERRIDE_NAME_THREE)
                .description(HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION)
                .effectiveFrom(OVERRIDE_EFFECTIVE_FROM)
                .effectiveTill(OVERRIDE_EFFECTIVE_TILL)
                .config(translateToHOOPOverrideConfigModel(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_THREE, HOURS_OF_OPERATION_OVERRIDE_ID_THREE, OVERRIDE_NAME_THREE)))
                .build();
    }

    public static software.amazon.awssdk.services.connect.model.HoursOfOperationOverride getHoursOfOperationOverrideTwo() {
        return software.amazon.awssdk.services.connect.model.HoursOfOperationOverride.builder()
                .hoursOfOperationId(HOURS_OF_OPERATION_ARN)
                .hoursOfOperationOverrideId(HOURS_OF_OPERATION_OVERRIDE_ID_TWO)
                .name(OVERRIDE_NAME_TWO)
                .description(HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION)
                .effectiveFrom(OVERRIDE_EFFECTIVE_FROM)
                .effectiveTill(OVERRIDE_EFFECTIVE_TILL)
                .config(translateToHOOPOverrideConfigModel(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_TWO, HOURS_OF_OPERATION_OVERRIDE_ID_TWO, OVERRIDE_NAME_TWO)))
                .build();
    }

    public static software.amazon.awssdk.services.connect.model.HoursOfOperationOverride getHoursOfOperationOverrideOne() {
        return software.amazon.awssdk.services.connect.model.HoursOfOperationOverride.builder()
                .hoursOfOperationId(HOURS_OF_OPERATION_ARN)
                .hoursOfOperationOverrideId(HOURS_OF_OPERATION_OVERRIDE_ID_ONE)
                .name(OVERRIDE_NAME_ONE)
                .description(HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION)
                .effectiveFrom(OVERRIDE_EFFECTIVE_FROM)
                .effectiveTill(OVERRIDE_EFFECTIVE_TILL)
                .config(translateToHOOPOverrideConfigModel(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_ONE, HOURS_OF_OPERATION_OVERRIDE_ID_ONE, OVERRIDE_NAME_ONE)))
                .build();
    }

    public static ListHoursOfOperationOverridesResponse getListHOOPOverridesResponse(String nextToken,
                                                                                     software.amazon.awssdk.services.connect.model.HoursOfOperationOverride override) {
        ListHoursOfOperationOverridesResponse.Builder builder = ListHoursOfOperationOverridesResponse.builder();
        builder.hoursOfOperationOverrideList(override);
        if (StringUtils.isNotBlank(nextToken)) {
            builder.nextToken(nextToken);
        }
        return builder.build();
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

    private static OverrideTimeSlice getOverrideTimeSlice(final int hours, final int minutes) {
        return OverrideTimeSlice.builder()
                .hours(hours)
                .minutes(minutes)
                .build();
    }

    private static Set<Tag> convertTagMapToSet() {
        Set<Tag> tags = Sets.newHashSet();
        TAGS_ONE.forEach((key, value) -> tags.add(Tag.builder().key(key).value(value).build()));
        return tags;
    }

    private static Date getFromDate(int fromDays, int year) {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.DATE, fromDays);
        return calendar.getTime();
    }
}
