package software.amazon.connect.user;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Collections;

import java.util.Map;
import java.util.Set;

public class UserTestDataProvider {
    protected static final String USER_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent/userID";
    protected static final String INVALID_USER_ARN = "invalidUserArn";
    protected static final String USER_ID = "userId";
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String INSTANCE_ARN_TWO = "arn:aws:connect:us-west-2:111111111111:instance/instanceIdTwo";
    protected static final String USERNAME = "username";
    protected static final String USER_PASSWORD = "userPassword";
    protected static final String DIRECTORY_USER_ID = "directoryUserId";
    protected static final String DIRECTORY_USER_ID_TWO = "directoryUserId2";
    protected static final String ROUTING_PROFILE_ID = "routingProfileId";
    protected static final String ROUTING_PROFILE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/routing-profile/routingProfileId";
    protected static final String ROUTING_PROFILE_ARN_TWO = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/routing-profile/routingProfileId2";
    protected static final String SECURITY_PROFILE_ID = "securityProfileId";
    protected static final String SECURITY_PROFILE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/security-profile/securityProfileId";
    protected static final String SECURITY_PROFILE_ARN_TWO = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/security-profile/securityProfileId2";
    protected static final String HIERARCHY_GROUP_ID = "userHierarchyGroupId";
    protected static final String HIERARCHY_GROUP_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent-group/userHierarchyGroupId";
    protected static final String HIERARCHY_GROUP_ARN_TWO = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent-group/userHierarchyGroupId2";
    protected static final String PHONE_NUMBER = "+14122770115";
    protected static final String PHONE_TYPE_DESK = "DESK_PHONE";
    protected static final Integer AFTER_CONTACT_WORK_TIME_LIMIT = 0;
    protected static final String PHONE_TYPE_SOFT = "SOFT_PHONE";
    protected static final Boolean AUTO_ACCEPT = Boolean.TRUE;
    protected static final String FIRST_NAME = "FIRST_NAME_TEST";
    protected static final String LAST_NAME = "LAST_NAME_TEST";
    protected static final String EMAIL = "email@gmail.com";
    protected static final String SECONDARY_EMAIL = "secondary_email@gmail.com";
    protected static final String MOBILE = "+7745781234";
    protected static final String FIRST_NAME_ONE = "FIRST_NAME_ONE_TEST";
    protected static final String LAST_NAME_ONE = "LAST_NAME_ONE_TEST";
    protected static final String EMAIL_ONE = "email1@gmail.com";
    protected static final String SECONDARY_EMAIL_ONE = "secondary_email1@gmail.com";
    protected static final String MOBILE_ONE = "+7745781235";
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
    protected static final Set<Tag> TAGS_SET_ONE = convertTagMapToSet();

    protected static ResourceModel buildUserDesiredStateResourceModel() {
        final UserPhoneConfig userPhoneConfig = getUserPhoneConfig();

        final UserIdentityInfo userIdentityInfo = getUserIdentityInfo(FIRST_NAME, LAST_NAME, EMAIL, SECONDARY_EMAIL, MOBILE);

        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .username(USERNAME)
                .password(USER_PASSWORD)
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .userArn(USER_ARN)
                .directoryUserId(DIRECTORY_USER_ID)
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .phoneConfig(userPhoneConfig)
                .identityInfo(userIdentityInfo)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .build();
    }

    protected static ResourceModel buildUserPreviousStateResourceModel() {
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
                .secondaryEmail(SECONDARY_EMAIL_ONE)
                .mobile(MOBILE_ONE)
                .build();

        return ResourceModel.builder()
                .username(USERNAME)
                .password(USER_PASSWORD)
                .instanceArn(INSTANCE_ARN)
                .routingProfileArn(ROUTING_PROFILE_ARN_TWO)
                .directoryUserId(DIRECTORY_USER_ID)
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN_TWO))
                .phoneConfig(userPhoneConfig)
                .userArn(USER_ARN)
                .identityInfo(userIdentityInfo)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN_TWO)
                .build();
    }

    protected static UserIdentityInfo getUserIdentityInfo() {
        return UserIdentityInfo.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .secondaryEmail(SECONDARY_EMAIL)
                .mobile(MOBILE)
                .build();
    }

    protected static UserIdentityInfo getUserIdentityInfo(String firstName, String lastName, String email, String secondaryEmail, String mobile) {
        return UserIdentityInfo.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .secondaryEmail(secondaryEmail)
                .mobile(mobile)
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

    private static Set<Tag> convertTagMapToSet() {
        Set<Tag> tags = Sets.newHashSet();
        TAGS_ONE.forEach((key, value) -> tags.add(Tag.builder().key(key).value(value).build()));
        return tags;
    }
}
