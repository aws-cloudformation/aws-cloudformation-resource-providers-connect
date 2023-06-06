package software.amazon.connect.prompt;

import software.amazon.awssdk.services.connect.ConnectClient;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class ClientBuilderTest {

    private static final String CONNECT_CLIENT = "ConnectClient";

    @Test
    public void testCreateConnectClient() {
        ConnectClient client = ClientBuilder.getClient();
        assertThat(client).isNotNull();
        assertThat(client.toString().contains(CONNECT_CLIENT)).isTrue();
    }
}
