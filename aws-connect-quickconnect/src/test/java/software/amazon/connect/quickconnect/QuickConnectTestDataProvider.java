package software.amazon.connect.quickconnect;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.connect.model.QuickConnect;
import software.amazon.awssdk.services.connect.model.QuickConnectType;

import java.util.Map;
import java.util.Set;

public class QuickConnectTestDataProvider {
    protected static final String QUICK_CONNECT_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/transfer-destination/quickConnectId";
    protected static final String QUICK_CONNECT_ID = "quickConnectId";
    protected static final String CONTACT_FLOW_ID = "contactFlowId";
    protected static final String USER_ID = "userId";
    protected static final String QUEUE_ID = "queueId";
    protected static final String PHONE_NUMBER = "+1234567890";
    protected static final String CONTACT_FLOW_ID_2 = "contactFlowId2";
    protected static final String USER_ID_2 = "userId2";
    protected static final String QUEUE_ID_2 = "queueId2";
    protected static final String PHONE_NUMBER_2 = "+9876543210";
    protected static final String INSTANCE_ID = "instanceId";
    protected static final String QUICK_CONNECT_NAME_ONE = "quickConnectNameOne";
    protected static final String QUICK_CONNECT_DESCRIPTION_ONE = "quickConnectDescriptionOne";
    protected static final String QUICK_CONNECT_NAME_TWO = "quickConnectNameTwo";
    protected static final String QUICK_CONNECT_DESCRIPTION_TWO = "quickConnectDescriptionTwo";
    protected static final String QUICK_CONNECT_NAME_THREE = "quickConnectNameThree";
    protected static final String QUICK_CONNECT_DESCRIPTION_THREE = "quickConnectDescriptionThree";
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

    protected static QuickConnect getDescribeQuickConnectResponseObjectWithQuickConnectTypeUser() {
        final software.amazon.awssdk.services.connect.model.UserQuickConnectConfig userQuickConnectConfig =
                software.amazon.awssdk.services.connect.model.UserQuickConnectConfig
                        .builder()
                        .contactFlowId(CONTACT_FLOW_ID)
                        .userId(USER_ID)
                        .build();

        final software.amazon.awssdk.services.connect.model.QuickConnectConfig quickConnectConfigTypeUser = software.amazon.awssdk.services.connect.model.QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.USER.toString())
                .userConfig(userQuickConnectConfig)
                .build();

        return QuickConnect.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .name(QUICK_CONNECT_NAME_ONE)
                .description(QUICK_CONNECT_DESCRIPTION_ONE)
                .tags(TAGS_ONE)
                .quickConnectConfig(quickConnectConfigTypeUser)
                .build();
    }

    protected static QuickConnect getDescribeQuickConnectResponseObjectWithQuickConnectTypeQueue() {
        final software.amazon.awssdk.services.connect.model.QueueQuickConnectConfig queueQuickConnectConfig =
                software.amazon.awssdk.services.connect.model.QueueQuickConnectConfig
                        .builder()
                        .contactFlowId(CONTACT_FLOW_ID)
                        .queueId(QUEUE_ID)
                        .build();

        final software.amazon.awssdk.services.connect.model.QuickConnectConfig quickConnectConfigTypeQueue = software.amazon.awssdk.services.connect.model.QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.QUEUE.toString())
                .queueConfig(queueQuickConnectConfig)
                .build();

        return QuickConnect.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .name(QUICK_CONNECT_NAME_TWO)
                .description(QUICK_CONNECT_DESCRIPTION_TWO)
                .tags(TAGS_TWO)
                .quickConnectConfig(quickConnectConfigTypeQueue)
                .build();
    }

    protected static QuickConnect getDescribeQuickConnectResponseObjectWithQuickConnectTypePhoneNumber() {
        final software.amazon.awssdk.services.connect.model.PhoneNumberQuickConnectConfig phoneNumberQuickConnectConfig =
                software.amazon.awssdk.services.connect.model.PhoneNumberQuickConnectConfig
                        .builder()
                        .phoneNumber(PHONE_NUMBER)
                        .build();

        final software.amazon.awssdk.services.connect.model.QuickConnectConfig quickConnectConfigTypePhoneNumber = software.amazon.awssdk.services.connect.model.QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.PHONE_NUMBER.toString())
                .phoneConfig(phoneNumberQuickConnectConfig)
                .build();

        return QuickConnect.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .name(QUICK_CONNECT_NAME_THREE)
                .description(QUICK_CONNECT_DESCRIPTION_THREE)
                .tags(TAGS_ONE)
                .quickConnectConfig(quickConnectConfigTypePhoneNumber)
                .build();
    }

    protected static ResourceModel buildQuickConnectResourceModelWithQuickConnectTypeUser() {

        final QuickConnectConfig quickConnectConfigTypeUser = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.USER.toString())
                .userConfig(getUserQuickConnectConfig())
                .build();

        return ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_ONE)
                .description(QUICK_CONNECT_DESCRIPTION_ONE)
                .quickConnectConfig(quickConnectConfigTypeUser)
                .tags(TAGS_SET_ONE)
                .build();
    }

    protected static ResourceModel buildQuickConnectResourceModelWithQuickConnectTypeQueue() {
        final QuickConnectConfig quickConnectConfigTypeQueue = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.QUEUE.toString())
                .queueConfig(getQueueQuickConnectConfig())
                .build();

        return ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_TWO)
                .description(QUICK_CONNECT_DESCRIPTION_TWO)
                .quickConnectConfig(quickConnectConfigTypeQueue)
                .tags(TAGS_SET_TWO)
                .build();
    }


    protected static ResourceModel buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber() {
        final QuickConnectConfig quickConnectConfigTypePhoneNumber = QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.PHONE_NUMBER.toString())
                .phoneConfig(getPhoneQuickConnectConfig())
                .build();

        return ResourceModel.builder()
                .quickConnectARN(QUICK_CONNECT_ARN)
                .quickConnectId(QUICK_CONNECT_ID)
                .instanceId(INSTANCE_ID)
                .name(QUICK_CONNECT_NAME_THREE)
                .description(QUICK_CONNECT_DESCRIPTION_THREE)
                .quickConnectConfig(quickConnectConfigTypePhoneNumber)
                .tags(TAGS_SET_ONE)
                .build();
    }

    protected static UserQuickConnectConfig getUserQuickConnectConfig() {
        return UserQuickConnectConfig
                .builder()
                .userId(USER_ID)
                .contactFlowId(CONTACT_FLOW_ID)
                .build();
    }

    protected static QueueQuickConnectConfig getQueueQuickConnectConfig() {
        return QueueQuickConnectConfig
                .builder()
                .queueId(QUEUE_ID)
                .contactFlowId(CONTACT_FLOW_ID)
                .build();
    }

    protected static PhoneNumberQuickConnectConfig getPhoneQuickConnectConfig() {
        return PhoneNumberQuickConnectConfig
                .builder()
                .phoneNumber(PHONE_NUMBER)
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
