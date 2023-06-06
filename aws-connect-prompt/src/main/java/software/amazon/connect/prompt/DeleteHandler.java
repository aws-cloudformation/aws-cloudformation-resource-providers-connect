package software.amazon.connect.prompt;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeletePromptRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        final String promptArn = model.getPromptArn();

        logger.log(String.format("Invoked Prompt DeleteHandler with Prompt: %s ", model.getPromptArn()));

        if (!ArnHelper.isValidPromptArn(promptArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Prompt Arn", promptArn)));
        }

        return proxy.initiate("connect::deletePrompt", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDeletePromptRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deletePrompt, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeletePromptRequest translateToDeletePromptRequest(final ResourceModel model) {
        return DeletePromptRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromPromptArn(model.getPromptArn()))
                .promptId(model.getPromptArn())
                .build();
    }
}
