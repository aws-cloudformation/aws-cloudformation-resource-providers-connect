package software.amazon.connect.trafficdistributiongroup;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TrafficDistributionDataProvider {
    protected static final UUID ID = UUID.randomUUID();
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:123456789012:instance/" + ID;
    protected static final String TDG_NAME = "TestTrafficDistributionGroup";
    protected static final String TDG_DESCRIPTION = "Test description";
    protected static final String TDG_ARN = "arn:aws:connect:us-west-2:123456789012:traffic-distribution-group/" + ID;
    protected static final String VALID_TAG_KEY_ONE = "TagKeyOne";
    protected static final String VALID_TAG_VALUE_ONE = "A";
    protected static final String VALID_TAG_KEY_TWO = "TagKeyTwo";
    protected static final String VALID_TAG_VALUE_TWO = "B";
    protected static final String VALID_TAG_KEY_THREE = "TagKeyThree";
    protected static final String VALID_TAG_VALUE_THREE = "C";
    protected static final String VALID_TAG_KEY_FOUR = "TagKeyFour";
    protected static final String VALID_TAG_VALUE_FOUR = "D";
    protected static final int EXPECTED_CALLBACK_DELAY_SECONDS = 30;
    protected static final Map<String, String> TAGS_ONE = ImmutableMap.of(
            VALID_TAG_KEY_ONE,
            VALID_TAG_VALUE_ONE,
            VALID_TAG_KEY_TWO,
            VALID_TAG_VALUE_TWO);
    protected static final Set<Tag> TAGS_SET_ONE = TagHelper.convertToSet(TAGS_ONE);
    protected static final Map<String, String> TAGS_TWO = ImmutableMap.of(
            VALID_TAG_KEY_ONE,
            VALID_TAG_VALUE_THREE,
            VALID_TAG_KEY_TWO,
            VALID_TAG_VALUE_FOUR);
    protected static final Set<Tag> TAGS_SET_TWO = TagHelper.convertToSet(TAGS_TWO);
    protected static final Map<String, String> TAGS_THREE = ImmutableMap.of(
            VALID_TAG_KEY_THREE,
            VALID_TAG_VALUE_THREE,
            VALID_TAG_KEY_FOUR,
            VALID_TAG_VALUE_FOUR);
    protected static final Set<Tag> TAGS_SET_THREE = TagHelper.convertToSet(TAGS_THREE);

    protected static final String TDG_STATUS = "ACTIVE";

    protected static final boolean TDG_IS_DEFAULT = true;

    protected static ResourceModel buildCreateTrafficDistributionGroupDesiredStateResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .name(TDG_NAME)
                .description(TDG_DESCRIPTION)
                .tags(TAGS_SET_ONE)
                .build();
    }

    protected static ResourceModel buildDeleteTrafficDistributionGroupDesiredStateResourceModel() {
        return ResourceModel.builder()
                .trafficDistributionGroupArn(TDG_ARN)
                .build();
    }

    protected static ResourceModel buildListTrafficDistributionGroupDesiredStateResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .build();
    }

    protected static ResourceModel buildReadTrafficDistributionGroupDesiredStateResourceModel() {
        return ResourceModel.builder()
                .trafficDistributionGroupArn(TDG_ARN)
                .status(TDG_STATUS)
                .instanceArn(INSTANCE_ARN)
                .name(TDG_NAME)
                .description(TDG_DESCRIPTION)
                .tags(TAGS_SET_ONE)
                .isDefault(TDG_IS_DEFAULT)
                .build();
    }

    protected static ResourceModel buildUpdateTrafficDistributionGroupDesiredStateResourceModelWithSameKeys() {
        return ResourceModel.builder()
                .trafficDistributionGroupArn(TDG_ARN)
                .status(TDG_STATUS)
                .instanceArn(INSTANCE_ARN)
                .name(TDG_NAME)
                .description(TDG_DESCRIPTION)
                .tags(TAGS_SET_TWO)
                .build();
    }

    protected static ResourceModel buildUpdateTrafficDistributionGroupDesiredStateResourceModelWithDifferentKeys() {
        return ResourceModel.builder()
                .trafficDistributionGroupArn(TDG_ARN)
                .status(TDG_STATUS)
                .instanceArn(INSTANCE_ARN)
                .name(TDG_NAME)
                .description(TDG_DESCRIPTION)
                .tags(TAGS_SET_THREE)
                .build();
    }

    protected static ResourceModel buildUpdateTrafficDistributionGroupPreviousStateResourceModel() {
        return ResourceModel.builder()
                .trafficDistributionGroupArn(TDG_ARN)
                .status(TDG_STATUS)
                .instanceArn(INSTANCE_ARN)
                .name(TDG_NAME)
                .description(TDG_DESCRIPTION)
                .tags(TAGS_SET_ONE)
                .build();
    }
}
