package software.amazon.connect.contactflow;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.*;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowMetadataRequest;
import software.amazon.awssdk.services.connect.model.UpdateContactFlowContentRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel desiredStateModel = request.getDesiredResourceState();
        final ResourceModel previousStateModel = request.getPreviousResourceState();
        final Set<Tag> previousResourceTags = convertResourceTagsToSet(request.getPreviousResourceTags());
        final Set<Tag> desiredResourceTags = convertResourceTagsToSet(request.getDesiredResourceTags());
        final Set<Tag> tagsToRemove = Sets.difference(previousResourceTags, desiredResourceTags);
        final Set<Tag> tagsToAdd = Sets.difference(desiredResourceTags, previousResourceTags);

        logger.log(String.format("Invoked UpdateContactFlowHandler with Instance:%s, ContactFlow:%s", desiredStateModel.getInstanceArn(), desiredStateModel.getContactFlowArn()));

        if (StringUtils.isNotEmpty(desiredStateModel.getInstanceArn()) && !desiredStateModel.getInstanceArn().equals(previousStateModel.getInstanceArn())) {
            throw new CfnInvalidRequestException("InstanceArn cannot be updated.");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateContactFlowMetadata(proxy, proxyClient, desiredStateModel, progress, callbackContext, logger))
                .then(progress -> updateContactFlowContent(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> unTagResource(proxy, proxyClient, desiredStateModel, tagsToRemove, progress, callbackContext, logger))
                .then(progress -> tagResource(proxy, proxyClient, desiredStateModel, tagsToAdd, progress, callbackContext, logger))
                .then(progress -> ProgressEvent.defaultSuccessHandler(desiredStateModel));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateContactFlowMetadata(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<ConnectClient> proxyClient,
            final ResourceModel desiredStateModel,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final CallbackContext context,
            final Logger logger) {
        logger.log(String.format("Calling UpdateContactFlowMetadata API for ContactFlow:%s", desiredStateModel.getContactFlowArn()));
        return proxy.initiate("connect::updateContactFlowMetadata", proxyClient, desiredStateModel, context)
                .translateToServiceRequest(UpdateHandler::translateToUpdateContactFlowMetadataRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateContactFlowMetadata, logger))
                .progress();
    }

    private static UpdateContactFlowContentRequest translateToUpdateContactFlowContentRequest(final ResourceModel model) {
        return UpdateContactFlowContentRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .contactFlowId(model.getContactFlowArn())
                .content(model.getContent())
                .build();
    }

    private static UpdateContactFlowMetadataRequest translateToUpdateContactFlowMetadataRequest(final ResourceModel model) {
        return UpdateContactFlowMetadataRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .contactFlowId(model.getContactFlowArn())
                .name(model.getName())
                .description(model.getDescription())
                .contactFlowState(model.getState())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateContactFlowContent(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<ConnectClient> proxyClient,
            final ResourceModel desiredStateModel,
            final ResourceModel previousStateModel,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final CallbackContext context,
            final Logger logger) {
        logger.log(String.format("Calling UpdateContactFlowContent API for ContactFlow:%s", desiredStateModel.getContactFlowArn()));
        return proxy.initiate("connect::updateContactFlowMetadata", proxyClient, desiredStateModel, context)
                .translateToServiceRequest(UpdateHandler::translateToUpdateContactFlowContentRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateContactFlowContent, logger))
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> tagResource(final AmazonWebServicesClientProxy proxy,
                                                                      final ProxyClient<ConnectClient> proxyClient,
                                                                      final ResourceModel desiredStateModel,
                                                                      final Set<Tag> tagsToAdd,
                                                                      final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                      final CallbackContext context,
                                                                      final Logger logger) {
        final String contactFlowArn = desiredStateModel.getContactFlowArn();

        if (tagsToAdd.size() > 0) {
            logger.log(String.format("Tags have been modified(addition/TagValue updated) in the update operation, " +
                    "Calling TagResource API for ContactFlow:%s", contactFlowArn));
            return proxy.initiate("connect::tagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToTagRequest(contactFlowArn, tagsToAdd))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::tagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No new tags or change in value for existing keys in update operation," +
                " skipping TagResource API call for ContactFlow:%s", contactFlowArn));
        return progress;
    }

    private ProgressEvent<ResourceModel, CallbackContext> unTagResource(final AmazonWebServicesClientProxy proxy,
                                                                        final ProxyClient<ConnectClient> proxyClient,
                                                                        final ResourceModel desiredStateModel,
                                                                        final Set<Tag> tagsToRemove,
                                                                        final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                        final CallbackContext context,
                                                                        final Logger logger) {
        final String contactFlowArn = desiredStateModel.getContactFlowArn();

        if (tagsToRemove.size() > 0) {
            logger.log(String.format("Tags have been removed in the update operation, " +
                    "Calling UnTagResource API for ContactFlow Arn:%s", contactFlowArn));
            return proxy.initiate("connect::untagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToUntagRequest(contactFlowArn, tagsToRemove))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::untagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No removal of tags in update operation, skipping UnTagResource API call " +
                "for ContactFlow: %s", contactFlowArn));
        return progress;
    }

    private UntagResourceRequest translateToUntagRequest(final String contactFlowArn, final Set<Tag> tags) {
        final Set<String> tagKeys = streamOfOrEmpty(tags).map(Tag::getKey).collect(Collectors.toSet());

        return UntagResourceRequest.builder()
                .resourceArn(contactFlowArn)
                .tagKeys(tagKeys)
                .build();
    }

    private TagResourceRequest translateToTagRequest(final String contactFlowArn, final Set<Tag> tags) {
        return TagResourceRequest.builder()
                .resourceArn(contactFlowArn)
                .tags(translateTagsToSdk(tags))
                .build();
    }

    private Map<String, String> translateTagsToSdk(final Set<Tag> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::getKey,
                Tag::getValue));
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
