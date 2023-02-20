package software.amazon.connect.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.connect.model.Instance;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private Instance instance;
    private int stabilizationRetriesRemaining; // Instance creation could take up to 15 minutes, this is used to keep track of the amount of retries after which resource stabilization fails.
}
