package software.amazon.connect.prompt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

public class PromptTestDataProvider {

    protected static final String PROMPT_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/prompt/promptId";
    protected static final String INVALID_PROMPT_ARN = "invalidPromptArn";
    protected static final String PROMPT_ID = "promptId";
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String INSTANCE_ARN_TWO = "arn:aws:connect:us-west-2:111111111111:instance/instanceIdTwo";
    protected static final String PROMPT_NAME_ONE = "promptNameOne";
    protected static final String PROMPT_DESCRIPTION_ONE = "promptDescriptionOne";
    protected static final String PROMPT_S3URI_ONE = "s3://prompt-bucket/prompt.wav";


    protected static final String PROMPT_NAME_TWO = "promptNameTwo";
    protected static final String PROMPT_DESCRIPTION_TWO = "promptDescriptionTwo";
    protected static final String PROMPT_S3URI_TWO = "s3://prompt-bucket-two/prompt.wav";
    protected static final String VALID_TAG_KEY_ONE = "TagKeyOne";
    protected static final String VALID_TAG_VALUE_ONE = "A";
    protected static final String VALID_TAG_KEY_TWO = "TagKeyTwo";
    protected static final String VALID_TAG_VALUE_TWO = "B";
    protected static final String VALID_TAG_KEY_THREE = "TagKeyThree";
    protected static final String VALID_TAG_VALUE_THREE = "C";

    protected static final String VALID_SYSTEM_TAG_KEY_ONE = "aws:cfn-stack-name";
    protected static final String VALID_SYSTEM_TAG_VALUE_ONE = "test";

    protected static final String VALID_RES_STATE_TAG_KEY_ONE = "resource-tag-key";
    protected static final String VALID_RES_STATE_TAG_VALUE_ONE = "resource-tag-value";

    protected static final Map<String, String> TAGS_ONE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE);
    protected static final Map<String, String> TAGS_TWO = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE,
            VALID_TAG_KEY_TWO, VALID_TAG_VALUE_TWO);
    protected static final Map<String, String> TAGS_THREE = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE,
            VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);

    protected static final Map<String, String> SYSTEM_TAGS_ONE = ImmutableMap.of(VALID_SYSTEM_TAG_KEY_ONE, VALID_SYSTEM_TAG_VALUE_ONE);

    protected static final Map<String, String> RES_STATE_TAGS_ONE = ImmutableMap.of(VALID_RES_STATE_TAG_KEY_ONE, VALID_RES_STATE_TAG_VALUE_ONE);
    protected static final Set<Tag> TAGS_SET_ONE = convertTagMapToSet(TAGS_ONE);
    protected static final Set<Tag> TAGS_SET_TWO = convertTagMapToSet(TAGS_TWO);

    protected static final Set<Tag> RES_STATE_TAGS_SET_ONE = convertTagMapToSet(RES_STATE_TAGS_ONE);



    protected static ResourceModel buildPromptDesiredStateResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .promptArn(PROMPT_ARN)
                .name(PROMPT_NAME_ONE)
                .description(PROMPT_DESCRIPTION_ONE)
                .s3Uri(PROMPT_S3URI_ONE)
                .tags(convertTagMapToSet(RES_STATE_TAGS_ONE))
                .build();
    }

    protected static ResourceModel buildPromptPreviousStateResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .promptArn(PROMPT_ARN)
                .name(PROMPT_NAME_TWO)
                .description(PROMPT_DESCRIPTION_TWO)
                .s3Uri(PROMPT_S3URI_TWO)
                .tags(RES_STATE_TAGS_SET_ONE)
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
