package software.amazon.connect.prompt;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ListPromptsRequest;
import software.amazon.awssdk.services.connect.model.ListPromptsResponse;
import software.amazon.awssdk.services.connect.model.PromptSummary;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListHandler extends BaseHandlerStd {
    private Logger logger;
    private ProxyClient<ConnectClient> proxyClient;
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        this.proxyClient = proxyClient;
        final ResourceModel model = request.getDesiredResourceState();

        if (model == null) {
            throw new CfnInvalidRequestException("DesiredResourceState is null, unable to get instanceArn to list Prompts");
        }

        if (!ArnHelper.isValidInstanceArn(model.getInstanceArn())) {
            throw new CfnInvalidRequestException(String.format("%s is not a valid instance Arn", model.getInstanceArn()));
        }

        logger.log(String.format("Invoking Prompt ListHandler with instanceArn:{}", model.getInstanceArn()));

        ListPromptsResponse listPromptsResponse = listPrompts(request.getNextToken(), model);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(translateFromListResponse(listPromptsResponse, model.getInstanceArn()))
                .nextToken(listPromptsResponse.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private ListPromptsResponse listPrompts(String nextToken, ResourceModel model) {
        logger.log(String.format("Invoked listPrompts operation with NextToken: {}",nextToken));
        ListPromptsRequest request = ListPromptsRequest.builder().instanceId(model.getInstanceArn()).nextToken(nextToken).build();
        return invoke(request,proxyClient, proxyClient.client()::listPrompts,logger);
    }

    private List<ResourceModel> translateFromListResponse(final ListPromptsResponse listResponse, final String instanceArn){
        return streamOfOrEmpty(listResponse.promptSummaryList())
                .map(promptSummary -> translateToResourceModel(promptSummary,instanceArn))
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }

    private ResourceModel translateToResourceModel(final PromptSummary promptSummary, final String instanceArn) {
        return ResourceModel.builder()
                .promptArn(promptSummary.arn())
                .name(promptSummary.name())
                .build();
    }
}
