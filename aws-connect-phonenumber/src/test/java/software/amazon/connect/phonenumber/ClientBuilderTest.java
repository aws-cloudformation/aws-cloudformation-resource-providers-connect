package software.amazon.connect.phonenumber;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.connect.ConnectClient;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class ClientBuilderTest {

    private static final String CONNECT_CLIENT = "ConnectClient";

    @Test
    public void testCreateConnectClient() {
        ConnectClient client = ClientBuilder.getClient();
        assertNotNull(client);
        assertTrue(client.toString().contains(CONNECT_CLIENT));
    }
}
