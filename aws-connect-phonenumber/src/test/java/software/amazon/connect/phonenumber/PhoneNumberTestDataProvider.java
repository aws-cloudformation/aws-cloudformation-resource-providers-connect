package software.amazon.connect.phonenumber;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.connect.model.PhoneNumberStatus;

import java.util.Map;
import java.util.Set;

public class PhoneNumberTestDataProvider {
    protected static final String INSTANCE_ARN_ONE = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String INSTANCE_ARN_TWO = "arn:aws:connect:us-west-2:222222222222:instance/instanceId";
    protected static final String PHONE_NUMBER_ID_ONE = "phoneNumberId";
    protected static final String PHONE_NUMBER_ARN_ONE = "arn:aws:connect:us-west-2:111111111111:phone-number" +
            "/11111111-1111-1111-1111-111111111111";
    protected static final String PHONE_NUMBER_ARN_TWO = "arn:aws:connect:us-west-2:222222222222:phone-number" +
            "/22222222-2222-2222-2222-222222222222";
    protected static final String INVALID_PHONE_NUMBER_ARN = "invalidPhoneNumberArn";
    protected static final String DESCRIPTION = "Sample phone number description.";
    protected static final String ADDRESS = "+11111111111";
    protected static final String ADDRESS_TWO = "+22222222222";
    protected static final String PREFIX = "+1";
    protected static final String TOLL_FREE = "TOLL_FREE";
    protected static final String DID = "DID";
    protected static final String COUNTRY_CODE = "US";
    protected static final String COUNTRY_CODE_TWO = "CA";
    protected static final String VALID_TAG_KEY_ONE = "TagKeyOne";
    protected static final String VALID_TAG_VALUE_ONE = "A";
    protected static final String VALID_TAG_KEY_TWO = "TagKeyTwo";
    protected static final String VALID_TAG_VALUE_TWO = "B";
    protected static final String VALID_TAG_KEY_THREE = "TagKeyThree";
    protected static final String VALID_TAG_VALUE_THREE = "C";
    protected static final Map<String, String> TAGS_ONE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE);
    protected static final Map<String, String> TAGS_TWO = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE,
                                                                          VALID_TAG_KEY_TWO, VALID_TAG_VALUE_TWO);
    protected static final Map<String, String> TAGS_THREE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE,
                                                                            VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);
    protected static final Set<Tag> TAGS_SET_ONE = convertTagMapToSet(TAGS_ONE);
    protected static final Set<Tag> TAGS_SET_TWO = convertTagMapToSet(TAGS_TWO);
    protected static final String PHONE_NUMBER_CLAIMED = "CLAIMED";
    protected static final String PHONE_NUMBER_FAILED = "FAILED";
    protected static final String PHONE_NUMBER_IN_PROGRESS = "IN_PROGRESS";

    protected static ResourceModel buildPhoneNumberDesiredStateResourceModel() {
        return ResourceModel.builder()
                            .targetArn(INSTANCE_ARN_ONE)
                            .phoneNumberArn(PHONE_NUMBER_ARN_ONE)
                            .description(DESCRIPTION)
                            .address(ADDRESS)
                            .type(TOLL_FREE)
                            .countryCode(COUNTRY_CODE)
                            .prefix(PREFIX)
                            .tags(TAGS_SET_ONE)
                            .build();
    }

    protected static ResourceModel buildPhoneNumberPreviousStateResourceModel() {
        return ResourceModel.builder()
                            .targetArn(INSTANCE_ARN_TWO)
                            .phoneNumberArn(PHONE_NUMBER_ARN_ONE)
                            .description(DESCRIPTION)
                            .address(ADDRESS)
                            .type(TOLL_FREE)
                            .countryCode(COUNTRY_CODE)
                            .prefix(PREFIX)
                            .tags(TAGS_SET_TWO)
                            .build();
    }

    protected static ResourceModel buildCreatePhoneNumberResourceModel() {
        return ResourceModel.builder()
                            .targetArn(INSTANCE_ARN_ONE)
                            .type(TOLL_FREE)
                            .countryCode(COUNTRY_CODE)
                            .build();
    }

    private static Set<Tag> convertTagMapToSet(Map<String, String> tagMap) {
        Set<Tag> tags = Sets.newHashSet();
        tagMap.forEach((key, value) -> tags.add(Tag.builder().key(key).value(value).build()));
        return tags;
    }
}
