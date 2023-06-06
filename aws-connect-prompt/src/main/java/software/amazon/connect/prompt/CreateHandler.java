package software.amazon.connect.prompt;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreatePromptRequest;
import software.amazon.awssdk.services.connect.model.CreatePromptResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Map;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final Map<String, String> tags = request.getDesiredResourceTags();

        if (model == null) {
            throw new CfnInvalidRequestException("DesiredResourceState is null, unable to get instanceArn to create Prompts");
        }

        if (!ArnHelper.isValidInstanceArn(model.getInstanceArn())) {
            throw new CfnInvalidRequestException(String.format("%s is not a valid instance Arn", model.getInstanceArn()));
        }

        if (StringUtils.isEmpty(model.getS3Uri())) {
            throw new CfnInvalidRequestException("S3URI is a mandatory parameter for creating prompt");
        }

        logger.log(String.format("Invoked Prompt CreateHandler with InstanceArn:%s ", model.getInstanceArn()));

        return proxy.initiate("connect:createPrompt", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreatePromptRequest(resourceModel, tags))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createPrompt, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setPromptIdentifier(model, response)));
    }

    private CreatePromptRequest translateToCreatePromptRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreatePromptRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .name(model.getName())
                .description(model.getDescription() == null ? "" : model.getDescription())
                .s3Uri(model.getS3Uri())
                .tags(tags)
                .build();
    }

    private ResourceModel setPromptIdentifier(final ResourceModel model, final CreatePromptResponse createPromptResponse) {
        model.setPromptArn(createPromptResponse.promptARN());
        return model;
    }
}
