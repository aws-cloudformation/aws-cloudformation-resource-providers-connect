package software.amazon.connect.phonenumber;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTest {

    private Configuration configuration;

    @BeforeEach
    public void setup() {
        configuration = new Configuration();
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
