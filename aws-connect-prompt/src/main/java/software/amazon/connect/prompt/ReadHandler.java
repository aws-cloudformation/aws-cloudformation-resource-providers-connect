package software.amazon.connect.prompt;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribePromptRequest;
import software.amazon.awssdk.services.connect.model.Prompt;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {
    private ProxyClient<ConnectClient> proxyClient;
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

        logger.log(String.format("Invoked Prompt ReadHandler with Prompt: %s ", model.getPromptArn()));

        if (!ArnHelper.isValidPromptArn(promptArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Prompt Arn", promptArn)));
        }

        return proxy.initiate("connect::describePrompt", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribePromptRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describePrompt, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setPromptProperties(model, response.prompt())));
    }

    private DescribePromptRequest translateToDescribePromptRequest(final ResourceModel model) {
        return DescribePromptRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromPromptArn(model.getPromptArn()))
                .promptId(model.getPromptArn())
                .build();
    }

    private ResourceModel setPromptProperties(final ResourceModel model, final Prompt prompt) {
        final String instanceArn = ArnHelper.getInstanceArnFromPromptArn(model.getPromptArn());
        model.setInstanceArn(instanceArn);
        model.setPromptArn(prompt.promptARN());
        model.setName(prompt.name());
        model.setDescription(prompt.description());
        model.setTags(convertResourceTagsToSet(prompt.tags()));
        return model;
    }
}
