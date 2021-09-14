package software.amazon.connect.user;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTest {

    private static final String KEY_1 = "sameKey";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String SCHEMA_FILE_NAME = "aws-connect-user.json";

    private Configuration configuration;

    @BeforeEach
    public void setup() {
        configuration = new Configuration();
        assertThat(configuration.schemaFilename).isEqualTo(SCHEMA_FILE_NAME);
    }

    @Test
    public void testMergeDuplicateKeys() {
        final ResourceModel model = ResourceModel.builder()
                .tags(ImmutableSet.of(new Tag(KEY_1, VALUE_1), new Tag(KEY_1, VALUE_2)))
                .build();

        final Map<String, String> tags = configuration.resourceDefinedTags(model);

        assertThat(tags).isEqualTo(ImmutableMap.of(KEY_1, VALUE_2));
    }

    @Test
    public void testNullResourceTags() {
        final ResourceModel model = ResourceModel.builder()
                .tags(null)
                .build();

        final Map<String, String> tags = configuration.resourceDefinedTags(model);

        assertThat(tags).isNull();
    }

    @Test
    public void testEmptyResourceTags() {
        final ResourceModel model = ResourceModel.builder()
                .tags(ImmutableSet.of())
                .build();

        final Map<String, String> tags = configuration.resourceDefinedTags(model);

        assertThat(tags).isEmpty();
    }
}
