package software.amazon.connect.quickconnect;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.QuickConnectType;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UpdateQuickConnectConfigRequest;
import software.amazon.awssdk.services.connect.model.UpdateQuickConnectNameRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
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

        logger.log(String.format("Invoked UpdateQuickConnectHandler with QuickConnect:%s", desiredStateModel.getQuickConnectArn()));

        if (StringUtils.isNotEmpty(desiredStateModel.getInstanceArn()) && !desiredStateModel.getInstanceArn().equals(previousStateModel.getInstanceArn())){
            throw new CfnInvalidRequestException("InstanceArn cannot be updated.");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateQuickConnectName(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> updateQuickConnectConfig(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> unTagResource(proxy, proxyClient, desiredStateModel, tagsToRemove, progress, callbackContext, logger))
                .then(progress -> tagResource(proxy, proxyClient, desiredStateModel, tagsToAdd, progress, callbackContext, logger))
                .then(progress -> ProgressEvent.defaultSuccessHandler(desiredStateModel));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateQuickConnectName(final AmazonWebServicesClientProxy proxy,
                                                                                 final ProxyClient<ConnectClient> proxyClient,
                                                                                 final ResourceModel desiredStateModel,
                                                                                 final ResourceModel previousStateModel,
                                                                                 final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                                 final CallbackContext context,
                                                                                 final Logger logger) {

        final boolean updateQuickConnectName = !StringUtils.equals(desiredStateModel.getName(), previousStateModel.getName());
        final boolean updateQuickConnectDescription = !StringUtils.equals(desiredStateModel.getDescription(), previousStateModel.getDescription());

        if (updateQuickConnectName || updateQuickConnectDescription) {
            logger.log(String.format("Calling UpdateQuickConnectName API for QuickConnect:%s", desiredStateModel.getQuickConnectArn()));
            return proxy.initiate("connect::updateQuickConnectName", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(UpdateHandler::translateToUpdateQuickConnectNameRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateQuickConnectName, logger))
                    .progress();
        } else {
            logger.log(String.format("QuickConnect name and description fields are unchanged from in the update operation, " +
                    "skipping UpdateQuickConnectName API call for QuickConnect:%s", desiredStateModel.getQuickConnectArn()));
            return progress;
        }
    }

    private static UpdateQuickConnectNameRequest translateToUpdateQuickConnectNameRequest(final ResourceModel model) {
        return UpdateQuickConnectNameRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .quickConnectId(model.getQuickConnectArn())
                .name(model.getName())
                .description(model.getDescription())
                .build();
    }

    private static UpdateQuickConnectConfigRequest translateToUpdateQuickConnectConfigRequest(final ResourceModel model) {
        return UpdateQuickConnectConfigRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .quickConnectId(model.getQuickConnectArn())
                .quickConnectConfig(translateToQuickConnectConfig(model))
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateQuickConnectConfig(final AmazonWebServicesClientProxy proxy,
                                                                                   final ProxyClient<ConnectClient> proxyClient,
                                                                                   final ResourceModel desiredStateModel,
                                                                                   final ResourceModel previousStateModel,
                                                                                   final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                                   final CallbackContext context,
                                                                                   final Logger logger) {
        if (!StringUtils.equals(desiredStateModel.getQuickConnectConfig().getQuickConnectType(), previousStateModel.getQuickConnectConfig().getQuickConnectType())) {
            logger.log(String.format("QuickConnectType:%s has changed from current type:%s in update operation, " +
                            "Calling UpdateQuickConnectConfig API for QuickConnect:%s", desiredStateModel.getQuickConnectConfig().getQuickConnectType(),
                    previousStateModel.getQuickConnectConfig().getQuickConnectType(), desiredStateModel.getQuickConnectArn()));
            return proxy.initiate("connect::updateQuickConnectConfig", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(UpdateHandler::translateToUpdateQuickConnectConfigRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateQuickConnectConfig, logger))
                    .progress();
        } else if (StringUtils.equals(desiredStateModel.getQuickConnectConfig().getQuickConnectType(), QuickConnectType.USER.toString())) {
            if (!StringUtils.equals(desiredStateModel.getQuickConnectConfig().getUserConfig().getContactFlowArn(), previousStateModel.getQuickConnectConfig().getUserConfig().getContactFlowArn()) ||
                    !StringUtils.equals(desiredStateModel.getQuickConnectConfig().getUserConfig().getUserArn(), previousStateModel.getQuickConnectConfig().getUserConfig().getUserArn())) {
                logger.log(String.format("QuickConnectType:USER is unchanged in update operation but the UserConfig properties " +
                        "have changed, Calling UpdateQuickConnectConfig API for QuickConnect:%s", desiredStateModel.getQuickConnectArn()));
                return proxy.initiate("connect::updateQuickConnectConfig", proxyClient, desiredStateModel, context)
                        .translateToServiceRequest(UpdateHandler::translateToUpdateQuickConnectConfigRequest)
                        .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateQuickConnectConfig, logger))
                        .progress();
            }
        } else if (StringUtils.equals(desiredStateModel.getQuickConnectConfig().getQuickConnectType(), QuickConnectType.QUEUE.toString())) {
            if (!StringUtils.equals(desiredStateModel.getQuickConnectConfig().getQueueConfig().getContactFlowArn(), previousStateModel.getQuickConnectConfig().getQueueConfig().getContactFlowArn()) ||
                    !StringUtils.equals(desiredStateModel.getQuickConnectConfig().getQueueConfig().getQueueArn(), previousStateModel.getQuickConnectConfig().getQueueConfig().getQueueArn())) {
                logger.log(String.format("QuickConnectType:QUEUE is unchanged in update operation but the QueueConfig properties " +
                        "have changed, Calling UpdateQuickConnectConfig API for QuickConnect:%s", desiredStateModel.getQuickConnectArn()));
                return proxy.initiate("connect::updateQuickConnectConfig", proxyClient, desiredStateModel, context)
                        .translateToServiceRequest(UpdateHandler::translateToUpdateQuickConnectConfigRequest)
                        .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateQuickConnectConfig, logger))
                        .progress();
            }
        } else if (StringUtils.equals(desiredStateModel.getQuickConnectConfig().getQuickConnectType(), QuickConnectType.PHONE_NUMBER.toString())) {
            if (!StringUtils.equals(desiredStateModel.getQuickConnectConfig().getPhoneConfig().getPhoneNumber(), previousStateModel.getQuickConnectConfig().getPhoneConfig().getPhoneNumber())) {
                logger.log(String.format("QuickConnectType:PHONE_NUMBER is unchanged in update operation but the PhoneConfig properties " +
                        "have changed, Calling UpdateQuickConnectConfig API for QuickConnect:%s", desiredStateModel.getQuickConnectArn()));
                return proxy.initiate("connect::updateQuickConnectConfig", proxyClient, desiredStateModel, context)
                        .translateToServiceRequest(UpdateHandler::translateToUpdateQuickConnectConfigRequest)
                        .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateQuickConnectConfig, logger))
                        .progress();
            }
        }
        logger.log(String.format("QuickConnectConfig is unchanged in the update operation, skipping " +
                "UpdateQuickConnectConfig API call for QuickConnect:%s", desiredStateModel.getQuickConnectArn()));
        return progress;
    }

    private ProgressEvent<ResourceModel, CallbackContext> tagResource(final AmazonWebServicesClientProxy proxy,
                                                                      final ProxyClient<ConnectClient> proxyClient,
                                                                      final ResourceModel desiredStateModel,
                                                                      final Set<Tag> tagsToAdd,
                                                                      final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                      final CallbackContext context,
                                                                      final Logger logger) {
        final String quickConnectArn = desiredStateModel.getQuickConnectArn();

        if (tagsToAdd.size() > 0) {
            logger.log(String.format("Tags have been modified(addition/TagValue updated) in the update operation, " +
                    "Calling TagResource API for QuickConnect:%s", quickConnectArn));
            return proxy.initiate("connect::tagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToTagRequest(quickConnectArn, tagsToAdd))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::tagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No new tags or change in value for existing keys in update operation," +
                " skipping TagResource API call for QuickConnect:%s", quickConnectArn));
        return progress;
    }

    private ProgressEvent<ResourceModel, CallbackContext> unTagResource(final AmazonWebServicesClientProxy proxy,
                                                                        final ProxyClient<ConnectClient> proxyClient,
                                                                        final ResourceModel desiredStateModel,
                                                                        final Set<Tag> tagsToRemove,
                                                                        final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                        final CallbackContext context,
                                                                        final Logger logger) {
        final String quickConnectArn = desiredStateModel.getQuickConnectArn();

        if (tagsToRemove.size() > 0) {
            logger.log(String.format("Tags have been removed in the update operation, " +
                    "Calling UnTagResource API for QuickConnect:%s", quickConnectArn));
            return proxy.initiate("connect::untagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToUntagRequest(quickConnectArn, tagsToRemove))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::untagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No removal of tags in update operation, skipping UnTagResource API call " +
                "for QuickConnect:%s", quickConnectArn));
        return progress;
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    private UntagResourceRequest translateToUntagRequest(final String quickConnectArn, final Set<Tag> tags) {
        final Set<String> tagKeys = streamOfOrEmpty(tags).map(Tag::getKey).collect(Collectors.toSet());

        return UntagResourceRequest.builder()
                .resourceArn(quickConnectArn)
                .tagKeys(tagKeys)
                .build();
    }

    private TagResourceRequest translateToTagRequest(final String quickConnectArn, final Set<Tag> tags) {
        return TagResourceRequest.builder()
                .resourceArn(quickConnectArn)
                .tags(translateTagsToSdk(tags))
                .build();
    }

    private Map<String, String> translateTagsToSdk(final Set<Tag> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::getKey,
                Tag::getValue));
    }
}
