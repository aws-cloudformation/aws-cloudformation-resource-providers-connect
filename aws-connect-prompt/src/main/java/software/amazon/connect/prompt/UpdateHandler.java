package software.amazon.connect.prompt;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UpdatePromptRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static software.amazon.connect.prompt.TagHelper.convertToSet;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final ResourceModel desiredStateModel = request.getDesiredResourceState();
        final ResourceModel previousStateModel = request.getPreviousResourceState();
        String promptArn = desiredStateModel.getPromptArn();

        final Set<Tag> previousResourceTags = convertToSet(TagHelper.getPreviouslyAttachedTags(request));
        final Set<Tag> desiredResourceTags = convertToSet(TagHelper.getNewDesiredTags(request));
        final Set<Tag> tagsToRemove = TagHelper.generateTagsToRemove(previousResourceTags, desiredResourceTags);
        final Set<Tag> tagsToAdd = TagHelper.generateTagsToAdd(previousResourceTags, desiredResourceTags);

        if (StringUtils.isNotEmpty(desiredStateModel.getInstanceArn()) && !desiredStateModel.getInstanceArn().equals(previousStateModel.getInstanceArn())) {
            throw new CfnInvalidRequestException("InstanceArn cannot be updated.");
        }

        if (!ArnHelper.isValidPromptArn(promptArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Prompt Arn", promptArn)));
        }

        logger.log(String.format("Invoked Prompt UpdateHandler with Prompt: %s ", promptArn));

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updatePrompt(proxy, proxyClient, desiredStateModel, callbackContext, logger))
                .then(progress -> unTagResource(proxy, proxyClient, desiredStateModel, tagsToRemove, progress, callbackContext, logger))
                .then(progress -> tagResource(proxy, proxyClient, desiredStateModel, tagsToAdd, progress, callbackContext, logger))
                .then(progress -> ProgressEvent.defaultSuccessHandler(desiredStateModel));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updatePrompt(final AmazonWebServicesClientProxy proxy,
                                                                                 final ProxyClient<ConnectClient> proxyClient,
                                                                                 final ResourceModel desiredStateModel,
                                                                                 final CallbackContext context,
                                                                                 final Logger logger) {

        logger.log(String.format("Calling UpdatePrompt API for prompt:%s", desiredStateModel.getPromptArn()));
        return proxy.initiate("connect::updatePrompt", proxyClient, desiredStateModel, context)
                .translateToServiceRequest(desired -> translateToUpdatePromptRequest(desiredStateModel))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updatePrompt, logger))
                .done(response -> ProgressEvent.progress(desiredStateModel, context));
    }

    private UpdatePromptRequest translateToUpdatePromptRequest(final ResourceModel model) {

        return UpdatePromptRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .promptId(model.getPromptArn())
                .name(model.getName())
                .description(model.getDescription() == null ? "" : model.getDescription())
                .s3Uri(model.getS3Uri())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> unTagResource(final AmazonWebServicesClientProxy proxy,
                                                                        final ProxyClient<ConnectClient> proxyClient,
                                                                        final ResourceModel desiredStateModel,
                                                                        final Set<Tag> tagsToRemove,
                                                                        final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                        final CallbackContext context,
                                                                        final Logger logger) {
        final String promptArn = desiredStateModel.getPromptArn();

        if (tagsToRemove.size() > 0) {
            logger.log(String.format("Tags are removed in the update operation, " +
                    "Calling UnTagResource API for Prompt:%s", promptArn));
            return proxy.initiate("connect::untagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToUntagRequest(promptArn, tagsToRemove))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::untagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No removal of tags in update operation, skipping UnTagResource API call " +
                "for Prompt:%s", promptArn));
        return progress;
    }

    private ProgressEvent<ResourceModel, CallbackContext> tagResource(final AmazonWebServicesClientProxy proxy,
                                                                      final ProxyClient<ConnectClient> proxyClient,
                                                                      final ResourceModel desiredStateModel,
                                                                      final Set<Tag> tagsToAdd,
                                                                      final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                      final CallbackContext context,
                                                                      final Logger logger) {
        final String promptArn = desiredStateModel.getPromptArn();

        if (tagsToAdd.size() > 0) {
            logger.log(String.format("New tags or updated tags values are present in the update operation, " +
                    "Calling TagResource API for Prompt:%s", promptArn));
            return proxy.initiate("connect::tagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToTagRequest(promptArn, tagsToAdd))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::tagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No new tags or values for existing keys found in update operation," +
                " skipping TagResource API call for Prompt:%s", promptArn));
        return progress;
    }

    private UntagResourceRequest translateToUntagRequest(final String promptArn, final Set<Tag> tags) {
        final Set<String> tagKeys = streamOfOrEmpty(tags).map(Tag::getKey).collect(Collectors.toSet());

        return UntagResourceRequest.builder()
                .resourceArn(promptArn)
                .tagKeys(tagKeys)
                .build();
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    private TagResourceRequest translateToTagRequest(final String promptArn, final Set<Tag> tags) {
        return TagResourceRequest.builder()
                .resourceArn(promptArn)
                .tags(translateTagsToSdk(tags))
                .build();
    }

    private Map<String, String> translateTagsToSdk(final Set<Tag> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::getKey,
                Tag::getValue));
    }
}
