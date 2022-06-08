package software.amazon.connect.phonenumber;

import com.google.common.collect.Sets;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.UpdatePhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.UpdatePhoneNumberResponse;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class UpdateHandler extends BaseHandlerStd {

    /**
     * Using the specified resource model, calls UpdatePhoneNumber API to update the targetArn.
     * If there are any tags to update, those will be updated using TagResource/UntagResource APIs.
     * @param proxy
     * @param request
     * @param callbackContext
     * @param proxyClient
     * @param logger
     * @return ProgressEvent<ResourceModel, CallbackContext>
     * */
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
        final String phoneNumberArn = desiredStateModel.getPhoneNumberArn();

        logger.log(String.format("Invoked UpdatePhoneNumberHandler with PhoneNumber: %s. Called with target ARN: %s," +
                                " tag(s): %s", phoneNumberArn, desiredStateModel.getTargetArn(), desiredResourceTags));

        if (!ArnHelper.isValidPhoneNumberArn(phoneNumberArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid PhoneNumber ARN", phoneNumberArn)));
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                            .then(progress -> updateTargetArn(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                            .then(progress -> unTagResource(proxy, proxyClient, desiredStateModel, tagsToRemove, progress, callbackContext, logger))
                            .then(progress -> tagResource(proxy, proxyClient, desiredStateModel, tagsToAdd, progress, callbackContext, logger))
                            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Calls UpdatePhoneNumber API to update the TargetArn.
     * @param proxy
     * @param proxyClient
     * @param desiredStateModel
     * @param progress
     * @param proxyClient
     * @param logger
     * @return ProgressEvent<ResourceModel, CallbackContext>
     * */
    private ProgressEvent<ResourceModel, CallbackContext> updateTargetArn(final AmazonWebServicesClientProxy proxy,
                                                                          final ProxyClient<ConnectClient> proxyClient,
                                                                          final ResourceModel desiredStateModel,
                                                                          final ResourceModel previousStateModel,
                                                                          final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                          final CallbackContext context,
                                                                          final Logger logger) {

        final boolean updateTargetArn = !StringUtils.equals(desiredStateModel.getTargetArn(), previousStateModel.getTargetArn());
        if (updateTargetArn) {
            logger.log(String.format("Calling UpdatePhoneNumber API for PhoneNumber:%s", desiredStateModel.getPhoneNumberArn()));
            return proxy.initiate("connect::updatePhoneNumber", proxyClient, desiredStateModel, context)
                        .translateToServiceRequest(desired -> translateToUpdatePhoneNumberRequest(desiredStateModel))
                        .backoffDelay(STABILIZATION_DELAY)
                        .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updatePhoneNumber, logger))
                        .stabilize(this::stabilize)
                        .progress();
        }

        logger.log(String.format("TargetArn is unchanged in the update operation, " +
                                 "skipping updatePhoneNumber API call for PhoneNumber:%s", desiredStateModel.getPhoneNumberArn()));
        return progress;
    }

    /**
     * Check for update stabilization by checking if target ARN matches the desired target ARN.
     * @param updatePhoneNumberRequest
     * @param updatePhoneNumberResponse
     * @param proxyClient
     * @param desiredModel
     * @param callbackContext
     * @return boolean
     * */
    private boolean stabilize(
            final UpdatePhoneNumberRequest updatePhoneNumberRequest,
            final UpdatePhoneNumberResponse updatePhoneNumberResponse,
            final ProxyClient<ConnectClient> proxyClient,
            final ResourceModel desiredModel,
            final CallbackContext callbackContext) {
        DescribePhoneNumberRequest req = DescribePhoneNumberRequest.builder()
                                                                   .phoneNumberId(desiredModel.getPhoneNumberArn())
                                                                   .build();
        DescribePhoneNumberResponse response = proxyClient.injectCredentialsAndInvokeV2(req, proxyClient.client()::describePhoneNumber);
        return desiredModel.getTargetArn().equals(response.claimedPhoneNumberSummary().targetArn());
    }

    /**
     * Returns a UpdatePhoneNumberRequest built from the provided resource model.
     * @param model
     * @return UpdatePhoneNumberRequest
     * */
    private static UpdatePhoneNumberRequest translateToUpdatePhoneNumberRequest(final ResourceModel model) {
        return UpdatePhoneNumberRequest
                .builder()
                .phoneNumberId(model.getPhoneNumberArn())
                .targetArn(model.getTargetArn())
                .build();
    }

    /**
     * Checks if there are any tags to remove and calls UntagResource API.
     * @param proxy
     * @param proxyClient
     * @param desiredStateModel
     * @param tagsToRemove
     * @param progress
     * @param context
     * @param logger
     * @return ProgressEvent<ResourceModel, CallbackContext>
     * */
    private ProgressEvent<ResourceModel, CallbackContext> unTagResource(final AmazonWebServicesClientProxy proxy,
                                                                        final ProxyClient<ConnectClient> proxyClient,
                                                                        final ResourceModel desiredStateModel,
                                                                        final Set<Tag> tagsToRemove,
                                                                        final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                        final CallbackContext context,
                                                                        final Logger logger) {
        final String phoneNumberArn = desiredStateModel.getPhoneNumberArn();
        if (tagsToRemove.size() > 0) {
            logger.log(String.format("Removing %d tag(s) in the update operation with " +
                                     "UntagResource API for PhoneNumber:%s", tagsToRemove.size(), phoneNumberArn));
            return proxy.initiate("connect::untagResource", proxyClient, desiredStateModel, context)
                        .translateToServiceRequest(desired -> translateToUntagRequest(phoneNumberArn, tagsToRemove))
                        .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::untagResource, logger))
                        .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No removal of tags in update operation, skipping UnTagResource API call " +
                                 "for PhoneNumber:%s", phoneNumberArn));
        return progress;
    }

    /**
     * Returns an optional stream from a collection.
     * @param collection
     * @return Stream
     * */
    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                       .map(Collection::stream)
                       .orElseGet(Stream::empty);
    }

    /**
     * Translates a phoneNumberArn and tags to an UntagResourceRequest.
     * @param phoneNumberArn
     * @param tags
     * @return UntagResourceRequest
     * */
    private UntagResourceRequest translateToUntagRequest(final String phoneNumberArn, final Set<Tag> tags) {
        final Set<String> tagKeys = streamOfOrEmpty(tags).map(Tag::getKey).collect(Collectors.toSet());

        return UntagResourceRequest.builder()
                                   .resourceArn(phoneNumberArn)
                                   .tagKeys(tagKeys)
                                   .build();
    }

    /**
     * Checks if there are any tags to add and calls TagResource API.
     * @param proxy
     * @param proxyClient
     * @param desiredStateModel
     * @param tagsToAdd
     * @param progress
     * @param context
     * @param logger
     * @return ProgressEvent<ResourceModel, CallbackContext>
     * */
    private ProgressEvent<ResourceModel, CallbackContext> tagResource(final AmazonWebServicesClientProxy proxy,
                                                                      final ProxyClient<ConnectClient> proxyClient,
                                                                      final ResourceModel desiredStateModel,
                                                                      final Set<Tag> tagsToAdd,
                                                                      final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                      final CallbackContext context,
                                                                      final Logger logger) {
        final String phoneNumberArn = desiredStateModel.getPhoneNumberArn();
        if (tagsToAdd.size() > 0) {
            logger.log(String.format("Adding %d tag(s) in the update operation with " +
                                     "TagResource API for PhoneNumber:%s", tagsToAdd.size(), phoneNumberArn));
            return proxy.initiate("connect::tagResource", proxyClient, desiredStateModel, context)
                        .translateToServiceRequest(desired -> translateToTagRequest(phoneNumberArn, tagsToAdd))
                        .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::tagResource, logger))
                        .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No new tags or change in value for existing keys in update operation," +
                                 " skipping TagResource API call for PhoneNumber:%s", phoneNumberArn));
        return progress;
    }

    /**
     * Translates a phoneNumberArn and tags to a TagResourceRequest.
     * @param phoneNumberArn
     * @param tags
     * @return TagResourceRequest
     * */
    private TagResourceRequest translateToTagRequest(final String phoneNumberArn, final Set<Tag> tags) {
        return TagResourceRequest.builder()
                                 .resourceArn(phoneNumberArn)
                                 .tags(translateTagsToSdk(tags))
                                 .build();
    }

    /**
     * Translates a set of tags to a map of tags.
     * @param tags
     * @return Map<String, String>
     * */
    private Map<String, String> translateTagsToSdk(final Set<Tag> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::getKey,
                                                      Tag::getValue));
    }
}
