package software.amazon.connect.contactflowmodule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;

public class ContactFlowModuleTestDataProvider {
    protected static final String CONTACT_FLOW_MODULE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/flow-module/flowModuleId";
    protected static final String INVALID_CONTACT_FLOW_MODULE_ARN = "invalidContactFlowArn";
    protected static final String CONTACT_FLOW_MODULE_ID = "contactFlowModuleId";
    protected static final String CONTACT_FLOW_MODULE_NAME = "contactFlowModuleName";
    protected static final String CONTACT_FLOW_MODULE_DESCRIPTION = "contactFlowModuleDescription";
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String VALID_TAG_KEY_ONE = "TagKeyOne";
    protected static final String VALID_TAG_VALUE_ONE = "A";
    protected static final String FLOW_MODULE_CONTENT = "test-flow-module-content";
    protected static final String FLOW_MODULE_STATE_ACTIVE= "Active";
    protected static final String FLOW_MODULE_STATUS_PUBLISHED= "Published";
    protected static final String FLOW_MODULE_STATUS_SAVED= "Saved";

    protected static final String FLOW_MODULE_STATE_ARCHIVE = "Archive";
    protected static final String VALID_TAG_KEY_TWO = "TagKeyTwo";
    protected static final String VALID_TAG_KEY_THREE = "TagKeyThree";
    protected static final String VALID_TAG_VALUE_TWO = "B";
    protected static final String VALID_TAG_VALUE_THREE = "C";
    protected static final Map<String, String> TAGS_ONE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE);
    protected static final Map<String, String> TAGS_TWO = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);
    protected static final String CONTACT_FLOW_MODULE_NAME_TWO = "contactFlowModuleNameTwo";
    protected static final String CONTACT_FLOW_MODULE_DESCRIPTION_TWO = "contactFlowModuleDescriptionTwo";
    protected static final String FLOW_MODULE_CONTENT_TWO = "test-flow-module-content-two";
    protected static final Map<String, String> TAGS_THREE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE,
            VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);
    protected static final Set<Tag> TAGS_SET_ONE = convertTagMapToSet(TAGS_ONE);
    protected static final Set<Tag> TAGS_SET_TWO = convertTagMapToSet(TAGS_TWO);
    protected static ResourceModel buildContactFlowModuleDesiredStateResourceModel() {
        return ResourceModel.builder()
                .contactFlowModuleArn(CONTACT_FLOW_MODULE_ARN)
                .instanceArn(INSTANCE_ARN)
                .name(CONTACT_FLOW_MODULE_NAME)
                .description(CONTACT_FLOW_MODULE_DESCRIPTION)
                .content(FLOW_MODULE_CONTENT)
                .state(FLOW_MODULE_STATE_ACTIVE)
                .status(FLOW_MODULE_STATUS_PUBLISHED)
                .build();
    }

    protected static ResourceModel buildContactFlowModulePreviousStateResourceModel() {
        return ResourceModel.builder()
                .contactFlowModuleArn(CONTACT_FLOW_MODULE_ARN)
                .instanceArn(INSTANCE_ARN)
                .name(CONTACT_FLOW_MODULE_NAME_TWO)
                .description(CONTACT_FLOW_MODULE_DESCRIPTION_TWO)
                .content(FLOW_MODULE_CONTENT_TWO)
                .state(FLOW_MODULE_STATE_ARCHIVE)
                .status(FLOW_MODULE_STATUS_SAVED)
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
