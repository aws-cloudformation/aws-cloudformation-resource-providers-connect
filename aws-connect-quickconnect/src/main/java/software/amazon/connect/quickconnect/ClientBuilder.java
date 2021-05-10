package software.amazon.connect.quickconnect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.LambdaWrapper;
import software.amazon.awssdk.core.retry.RetryPolicy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ClientBuilder {

    private static final int NUMBER_OF_RETRIES = 3;

    private static final RetryPolicy RETRY_POLICY =
            RetryPolicy.builder()
                    .numRetries(NUMBER_OF_RETRIES)
                    .retryCondition(RetryCondition.defaultRetryCondition())
                    .build();

    public static ConnectClient getClient() {
        return ConnectClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RETRY_POLICY).build())
                .build();
    }
}
