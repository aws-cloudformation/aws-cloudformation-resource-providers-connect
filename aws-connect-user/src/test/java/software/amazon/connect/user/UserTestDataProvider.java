package software.amazon.connect.user;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Collections;

import java.util.Map;
import java.util.Set;

public class UserTestDataProvider {

    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String ROUTING_PROFILE_ARN = "arn:aws:connect:us-west-2:768859163783:instance/e6c9d054-8058-4bd8-998b-fb5c148ce28c/routing-profile/6e40fa8d-a718-42b5-bd99-dfa7c4109a6e";
    protected static final String ROUTING_PROFILE_ARN_2 = "arn:aws:connect:us-west-2:768859163783:instance/e6c9d054-8058-4bd8-998b-fb5c148ce28c/routing-profile/6e40fa8d-a718-42b5-bd99-dfa7c4109b9o";
    protected static final String USER_ARN = "arn:aws:connect:us-west-2:768859163783:instance/a4db200b-d826-4c53-b226-a08d1300d34e/agent/11a6bd59-ef37-4117-a654-903aa6fede84";
    protected static final String SECURITY_PROFILE_ARN = "arn:aws:connect:us-west-2:768859163783:instance/e6c9d054-8058-4bd8-998b-fb5c148ce28c/security-profile/551e8310-05af-45b5-ac7e-d380ccafa4c0";
    protected static final String SECURITY_PROFILE_ARN_2 = "arn:aws:connect:us-west-2:768859163783:instance/e6c9d054-8058-4bd8-998b-fb5c148ce28c/security-profile/551e8310-05af-45b5-ac7e-d380ccafa4m9";
    protected static final String HIERARCHY_GROUP_ARN = "arn:aws:connect:us-west-2:954356521827:instance/1e8dce9d-1e3b-41a5-a526-2d484a7d8488/agent-group/a1e457eb-4594-4fe4-9a67-d7d686ae0b09";
    protected static final String HIERARCHY_GROUP_ARN_2 = "arn:aws:connect:us-west-2:954356521827:instance/1e8dce9d-1e3b-41a5-a526-2d484a7d8488/agent-group/a1e457eb-4594-4fe4-9a67-d7d686ae0c89";
    protected static final String PHONE_NUMBER = "+14122770115";
    protected static final String PHONE_TYPE_DESK = "DESK_PHONE";
    protected static final Integer AFTER_CONTACT_WORK_TIME_LIMIT = 0;
    protected static final String PHONE_TYPE_SOFT = "SOFT_PHONE";
    protected static final Boolean AUTO_ACCEPT = Boolean.TRUE;
    protected static final String FIRST_NAME = "FIRST_NAME_TEST";
    protected static final String LAST_NAME = "LAST_NAME_TEST";
    protected static final String EMAIL = "email@gmail.com";
    protected static final String FIRST_NAME_ONE = "FIRST_NAME_ONE_TEST";
    protected static final String LAST_NAME_ONE = "LAST_NAME_ONE_TEST";
    protected static final String EMAIL_ONE = "email1@gmail.com";
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

    protected static ResourceModel buildUserResourceModel() {
        final UserPhoneConfig userPhoneConfig = UserPhoneConfig.builder()
                .afterContactWorkTimeLimit(AFTER_CONTACT_WORK_TIME_LIMIT)
                .autoAccept(Boolean.TRUE)
                .deskPhoneNumber(PHONE_NUMBER)
                .phoneType(PHONE_TYPE_DESK)
                .build();

        final UserIdentityInfo userIdentityInfo = UserIdentityInfo.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();

        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .userArn(USER_ARN)
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .phoneConfig(userPhoneConfig)
                .identityInfo(userIdentityInfo)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .build();
    }

    protected static ResourceModel buildUserResourceModel1() {
        final UserPhoneConfig userPhoneConfig = UserPhoneConfig.builder()
                .afterContactWorkTimeLimit(AFTER_CONTACT_WORK_TIME_LIMIT)
                .autoAccept(Boolean.TRUE)
                .deskPhoneNumber(PHONE_NUMBER)
                .phoneType(PHONE_TYPE_SOFT)
                .build();

        final UserIdentityInfo userIdentityInfo = UserIdentityInfo.builder()
                .firstName(FIRST_NAME_ONE)
                .lastName(LAST_NAME_ONE)
                .email(EMAIL_ONE)
                .build();

        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .routingProfileArn(ROUTING_PROFILE_ARN_2)
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN_2))
                .phoneConfig(userPhoneConfig)
                .userArn(USER_ARN)
                .identityInfo(userIdentityInfo)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN_2)
                .build();
    }

    protected static UserIdentityInfo getUserIdentityInfo() {
        return UserIdentityInfo.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .build();
    }

    protected static UserIdentityInfo getUserIdentityInfo(String firstName, String lastName, String email) {
        return UserIdentityInfo.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();
    }

    protected static UserPhoneConfig getUserPhoneConfig() {
        return UserPhoneConfig.builder()
                .afterContactWorkTimeLimit(0)
                .autoAccept(AUTO_ACCEPT)
                .deskPhoneNumber(PHONE_NUMBER)
                .phoneType(PHONE_TYPE_DESK)
                .build();
    }

    protected static UserPhoneConfig getUserPhoneConfig(Integer afterContactWorkTimeLimit, Boolean autoAccept, String deskPhoneNumber, String phoneType) {
        return UserPhoneConfig.builder()
                .afterContactWorkTimeLimit(afterContactWorkTimeLimit)
                .autoAccept(autoAccept)
                .deskPhoneNumber(deskPhoneNumber)
                .phoneType(phoneType)
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
