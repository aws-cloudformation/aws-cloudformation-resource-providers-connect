package software.amazon.connect.trafficdistributiongroup;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    boolean callBackForStop = false;
    boolean callBackDelay = false;

    public void enableCallBackDelay(final boolean callBackDelay) {
        this.callBackDelay = callBackDelay;
    }

    public boolean getCallBackDelay() {
        return callBackDelay;
    }
}
