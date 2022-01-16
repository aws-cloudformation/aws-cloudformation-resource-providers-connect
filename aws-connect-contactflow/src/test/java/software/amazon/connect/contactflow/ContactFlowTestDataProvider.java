package software.amazon.connect.contactflow;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.connect.model.ContactFlowType;

import java.util.Map;
import java.util.Set;

public class ContactFlowTestDataProvider {
    protected static final String CONTACT_FLOW_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/contact-flow/contactflowId";
    protected static final String INVALID_CONTACT_FLOW_ARN = "invalidContactFlowArn";
    protected static final String CONTACT_FLOW_ID = "contactFlowId";
    protected static final String CONTACT_FLOW_NAME = "contactFlowName";
    protected static final String CONTACT_FLOW_DESCRIPTION = "contactFlowDescription";
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String VALID_TAG_KEY_ONE = "TagKeyOne";
    protected static final String VALID_TAG_VALUE_ONE = "A";
    protected static final String FLOW_CONTENT = "test-flow-content";
    protected static final String FLOW_STATE_ACTIVE= "Active";
    protected static final String FLOW_STATUS_PUBLISHED= "Published";
    protected static final String FLOW_TYPE= "ContactFlow";

    protected static final String FLOW_STATE_ARCHIVE = "Archive";
    protected static final String VALID_TAG_KEY_TWO = "TagKeyTwo";
    protected static final String VALID_TAG_VALUE_TWO = "B";
    protected static final String VALID_TAG_KEY_THREE = "TagKeyThree";
    protected static final String VALID_TAG_VALUE_THREE = "C";
    protected static final Map<String, String> TAGS_ONE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE);
    protected static final Map<String, String> TAGS_TWO = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);
    protected static final String CONTACT_FLOW_NAME_TWO = "contactFlowNameTwo";
    protected static final String CONTACT_FLOW_DESCRIPTION_TWO = "contactFlowDescriptionTwo";
    protected static final String FLOW_CONTENT_TWO = "test-flow-content-two";
    protected static final String CONTACT_FLOW_NAME_THREE = "contactFlowNameThree";
    protected static final String CONTACT_FLOW_DESCRIPTION_THREE = "contactFlowDescriptionThree";
    protected static final Map<String, String> TAGS_THREE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE,
            VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);
    protected static final Set<Tag> TAGS_SET_ONE = convertTagMapToSet(TAGS_ONE);
    protected static final Set<Tag> TAGS_SET_TWO = convertTagMapToSet(TAGS_TWO);
    protected static ResourceModel buildContactFlowDesiredStateResourceModel() {
        return ResourceModel.builder()
                .contactFlowArn(CONTACT_FLOW_ARN)
                .instanceArn(INSTANCE_ARN)
                .name(CONTACT_FLOW_NAME)
                .description(CONTACT_FLOW_DESCRIPTION)
                .content(FLOW_CONTENT)
                .state(FLOW_STATE_ACTIVE)
                .type(FLOW_TYPE)
                .build();
    }

    protected static ResourceModel buildContactFlowPreviousStateResourceModel() {
        return ResourceModel.builder()
                .contactFlowArn(CONTACT_FLOW_ARN)
                .instanceArn(INSTANCE_ARN)
                .name(CONTACT_FLOW_NAME_TWO)
                .description(CONTACT_FLOW_DESCRIPTION_TWO)
                .content(FLOW_CONTENT_TWO)
                .state(FLOW_STATE_ARCHIVE)
                .type(FLOW_TYPE)
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
