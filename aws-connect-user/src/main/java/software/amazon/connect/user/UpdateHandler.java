package software.amazon.connect.user;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.UpdateUserSecurityProfilesRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserRoutingProfileRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserHierarchyRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserIdentityInfoRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserPhoneConfigRequest;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Set;
import java.util.Collection;
import java.util.Optional;
import java.util.Map;
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

        logger.log(String.format("Invoked UpdateUserHandler with user:%s", desiredStateModel.getUserArn()));

        if (StringUtils.isNotEmpty(desiredStateModel.getInstanceArn()) && !desiredStateModel.getInstanceArn().equals(previousStateModel.getInstanceArn())) {
            throw new CfnInvalidRequestException("InstanceArn cannot be updated.");
        }

        if (StringUtils.isNotEmpty(desiredStateModel.getDirectoryUserId()) && !desiredStateModel.getDirectoryUserId().equals(previousStateModel.getDirectoryUserId())) {
            throw new CfnInvalidRequestException("DirectoryUserId cannot be updated.");
        }

        if (StringUtils.isNotEmpty(desiredStateModel.getPassword()) && !desiredStateModel.getPassword().equals(previousStateModel.getPassword())) {
            throw new CfnInvalidRequestException("Password cannot be updated.");
        }

        if (StringUtils.isNotEmpty(desiredStateModel.getUsername()) && !desiredStateModel.getUsername().equals(previousStateModel.getUsername())) {
            throw new CfnInvalidRequestException("Username cannot be updated.");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateUserIdentityInfo(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> updateUserPhoneConfig(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> updateUserRoutingProfile(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> updateUserHierarchy(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> updateUserSecurityProfiles(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> unTagResource(proxy, proxyClient, desiredStateModel, tagsToRemove, progress, callbackContext, logger))
                .then(progress -> tagResource(proxy, proxyClient, desiredStateModel, tagsToAdd, progress, callbackContext, logger))
                .then(progress -> ProgressEvent.defaultSuccessHandler(desiredStateModel));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateUserIdentityInfo(final AmazonWebServicesClientProxy proxy,
                                                                                 final ProxyClient<ConnectClient> proxyClient,
                                                                                 final ResourceModel desiredStateModel,
                                                                                 final ResourceModel previousStateModel,
                                                                                 final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                                 final CallbackContext context,
                                                                                 final Logger logger) {
        final String desiredUpdateUserEmail = desiredStateModel.getIdentityInfo() == null ? null : desiredStateModel.getIdentityInfo().getEmail();
        final String desiredUpdateUserFirstName = desiredStateModel.getIdentityInfo() == null ? null : desiredStateModel.getIdentityInfo().getFirstName();
        final String desiredUpdateUserLastName = desiredStateModel.getIdentityInfo() == null ? null : desiredStateModel.getIdentityInfo().getLastName();

        final boolean updateUserEmail = !StringUtils.equals(desiredUpdateUserEmail , previousStateModel.getIdentityInfo().getEmail());
        final boolean updateUserFirstName = !StringUtils.equals(desiredUpdateUserFirstName , previousStateModel.getIdentityInfo().getFirstName());
        final boolean updateUserLastName = !StringUtils.equals(desiredUpdateUserLastName , previousStateModel.getIdentityInfo().getLastName());

        if (updateUserEmail || updateUserFirstName || updateUserLastName) {
            logger.log(String.format("Calling UpdateUserIdentityInfo API for user:%s", desiredStateModel.getUserArn()));
            return proxy.initiate("connect::updateUserIdentityInfo", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(UpdateHandler::translateToUpdateUserIdentityInfoRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateUserIdentityInfo, logger))
                    .progress();
        } else {
            logger.log(String.format("User Email, FirstName and LastName fields are unchanged in the update operation, " +
                    "skipping updateUserIdentityInfo API call for user:%s", desiredStateModel.getUserArn()));
            return progress;
        }
    }

    private static UpdateUserIdentityInfoRequest translateToUpdateUserIdentityInfoRequest(final ResourceModel model) {
        return UpdateUserIdentityInfoRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .userId(model.getUserArn())
                .identityInfo(translateToUserIdentityInfo(model))
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateUserPhoneConfig(final AmazonWebServicesClientProxy proxy,
                                                                                final ProxyClient<ConnectClient> proxyClient,
                                                                                final ResourceModel desiredStateModel,
                                                                                final ResourceModel previousStateModel,
                                                                                final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                                final CallbackContext context,
                                                                                final Logger logger) {
        requireNotNull(desiredStateModel.getPhoneConfig() , USER_PHONE_CONFIG);

        final int desiredAfterContactWorkTimeLimit = desiredStateModel.getPhoneConfig().getAfterContactWorkTimeLimit() == null ? 0 : desiredStateModel.getPhoneConfig().getAfterContactWorkTimeLimit();
        final int previousAfterContactWorkTimeLimit = previousStateModel.getPhoneConfig().getAfterContactWorkTimeLimit() == null ? 0 : previousStateModel.getPhoneConfig().getAfterContactWorkTimeLimit();

        final boolean afterContactWorkTimeLimit = desiredAfterContactWorkTimeLimit != previousAfterContactWorkTimeLimit;
        final boolean autoAccept = desiredStateModel.getPhoneConfig().getAutoAccept() != previousStateModel.getPhoneConfig().getAutoAccept();
        final boolean deskPhoneNumber = !StringUtils.equals(desiredStateModel.getPhoneConfig().getDeskPhoneNumber(), previousStateModel.getPhoneConfig().getDeskPhoneNumber());
        final boolean phoneType = !StringUtils.equals(desiredStateModel.getPhoneConfig().getPhoneType(), previousStateModel.getPhoneConfig().getPhoneType());
        if (afterContactWorkTimeLimit || autoAccept || deskPhoneNumber || phoneType) {
            logger.log(String.format("Calling UpdateUserPhoneConfig API for user:%s", desiredStateModel.getUserArn()));
            return proxy.initiate("connect::updateUserPhoneConfig", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(UpdateHandler::translateToUpdateUserPhoneConfigRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateUserPhoneConfig, logger))
                    .progress();

        } else {
            logger.log(String.format("User afterContactWorkTimeLimit, autoAccept, deskPhoneNumber and phoneType fields are unchanged in the update operation, " +
                    "skipping updateUserPhoneConfig API call for user:%s", desiredStateModel.getUserArn()));
            return progress;
        }
    }

    private static UpdateUserPhoneConfigRequest translateToUpdateUserPhoneConfigRequest(final ResourceModel model) {
        return UpdateUserPhoneConfigRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .userId(model.getUserArn())
                .phoneConfig(translateToUserPhoneConfig(model))
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateUserRoutingProfile(final AmazonWebServicesClientProxy proxy,
                                                                                   final ProxyClient<ConnectClient> proxyClient,
                                                                                   final ResourceModel desiredStateModel,
                                                                                   final ResourceModel previousStateModel,
                                                                                   final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                                   final CallbackContext context,
                                                                                   final Logger logger) {
        if (!StringUtils.equals(desiredStateModel.getRoutingProfileArn(), previousStateModel.getRoutingProfileArn())) {
            logger.log(String.format("Calling UpdateUserRoutingProfile API for user:%s", desiredStateModel.getUserArn()));
            return proxy.initiate("connect::updateUserRoutingProfile", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(UpdateHandler::translateToUpdateUserRoutingProfileRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateUserRoutingProfile, logger))
                    .progress();

        } else {
            logger.log(String.format("User RoutingProfileId is unchanged in the update operation, " +
                    "skipping updateUserRoutingProfile API call for user:%s", desiredStateModel.getUserArn()));
            return progress;
        }
    }

    private static UpdateUserRoutingProfileRequest translateToUpdateUserRoutingProfileRequest(final ResourceModel model) {
        return UpdateUserRoutingProfileRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .userId(model.getUserArn())
                .routingProfileId(model.getRoutingProfileArn())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateUserHierarchy(final AmazonWebServicesClientProxy proxy,
                                                                              final ProxyClient<ConnectClient> proxyClient,
                                                                              final ResourceModel desiredStateModel,
                                                                              final ResourceModel previousStateModel,
                                                                              final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                              final CallbackContext context,
                                                                              final Logger logger) {
        if (!StringUtils.equals(desiredStateModel.getHierarchyGroupArn(), previousStateModel.getHierarchyGroupArn())) {
            logger.log(String.format("Calling UpdateUserHierarchy API for user:%s", desiredStateModel.getUserArn()));
            return proxy.initiate("connect::updateUserHierarchy", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(UpdateHandler::translateToUpdateUserHierarchyRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateUserHierarchy, logger))
                    .progress();

        } else {
            logger.log(String.format("User HierarchyGroupId is unchanged in the update operation, " +
                    "skipping updateUserHierarchy API call for user:%s", desiredStateModel.getUserArn()));
            return progress;
        }
    }

    private static UpdateUserHierarchyRequest translateToUpdateUserHierarchyRequest(final ResourceModel model) {
        return UpdateUserHierarchyRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .userId(model.getUserArn())
                .hierarchyGroupId(model.getHierarchyGroupArn())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateUserSecurityProfiles(final AmazonWebServicesClientProxy proxy,
                                                                                     final ProxyClient<ConnectClient> proxyClient,
                                                                                     final ResourceModel desiredStateModel,
                                                                                     final ResourceModel previousStateModel,
                                                                                     final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                                     final CallbackContext context,
                                                                                     final Logger logger) {
        if (!(desiredStateModel.getSecurityProfileArns().containsAll(previousStateModel.getSecurityProfileArns()))) {
            logger.log(String.format("Calling UpdateUserSecurityProfiles API for user:%s", desiredStateModel.getUserArn()));
            return proxy.initiate("connect::updateUserSecurityProfiles", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(UpdateHandler::translateToUpdateUserSecurityProfiles)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateUserSecurityProfiles, logger))
                    .progress();

        } else {
            logger.log(String.format("User SecurityProfiles are unchanged in the update operation, " +
                    "skipping updateUserSecurityProfiles API call for user:%s", desiredStateModel.getUserArn()));
            return progress;
        }
    }

    private static UpdateUserSecurityProfilesRequest translateToUpdateUserSecurityProfiles(final ResourceModel model) {
        return UpdateUserSecurityProfilesRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .userId(model.getUserArn())
                .securityProfileIds(model.getSecurityProfileArns())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> unTagResource(final AmazonWebServicesClientProxy proxy,
                                                                        final ProxyClient<ConnectClient> proxyClient,
                                                                        final ResourceModel desiredStateModel,
                                                                        final Set<Tag> tagsToRemove,
                                                                        final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                        final CallbackContext context,
                                                                        final Logger logger) {
        final String userArn = desiredStateModel.getUserArn();
        if (tagsToRemove.size() > 0) {
            logger.log(String.format("Tags have been removed in the update operation, " +
                    "Calling UnTagResource API for user:%s", userArn));
            return proxy.initiate("connect::untagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToUntagRequest(userArn, tagsToRemove))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::untagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No removal of tags in update operation, skipping UnTagResource API call " +
                "for User:%s", userArn));
        return progress;
    }

    private UntagResourceRequest translateToUntagRequest(final String userArn, final Set<Tag> tags) {
        final Set<String> tagKeys = streamOfOrEmpty(tags).map(Tag::getKey).collect(Collectors.toSet());

        return UntagResourceRequest.builder()
                .resourceArn(userArn)
                .tagKeys(tagKeys)
                .build();
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    private ProgressEvent<ResourceModel, CallbackContext> tagResource(final AmazonWebServicesClientProxy proxy,
                                                                      final ProxyClient<ConnectClient> proxyClient,
                                                                      final ResourceModel desiredStateModel,
                                                                      final Set<Tag> tagsToAdd,
                                                                      final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                      final CallbackContext context,
                                                                      final Logger logger) {
        final String userArn = desiredStateModel.getUserArn();
        if (tagsToAdd.size() > 0) {
            logger.log(String.format("Tags have been modified(addition/TagValue updated) in the update operation, " +
                    "Calling TagResource API for user:%s", userArn));
            return proxy.initiate("connect::tagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToTagRequest(userArn, tagsToAdd))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::tagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No new tags or change in value for existing keys in update operation," +
                " skipping TagResource API call for user:%s", userArn));
        return progress;
    }

    private TagResourceRequest translateToTagRequest(final String userArn, final Set<Tag> tags) {
        return TagResourceRequest.builder()
                .resourceArn(userArn)
                .tags(translateTagsToSdk(tags))
                .build();
    }

    private Map<String, String> translateTagsToSdk(final Set<Tag> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::getKey,
                Tag::getValue));
    }
}
