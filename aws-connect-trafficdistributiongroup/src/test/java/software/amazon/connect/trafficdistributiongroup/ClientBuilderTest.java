package software.amazon.connect.trafficdistributiongroup;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.connect.ConnectClient;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientBuilderTest {
    private static final String CONNECT_CLIENT = "ConnectClient";
    private static final Region region = Region.US_WEST_2;

    @Test
    public void testCreateConnectClient() {
        ConnectClient client = ClientBuilder.getClient(region);
        assertThat(client).isNotNull();
        assertThat(client.toString().contains(CONNECT_CLIENT)).isTrue();
    }
}
