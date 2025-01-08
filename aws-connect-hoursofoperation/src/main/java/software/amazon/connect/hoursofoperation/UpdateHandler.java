package software.amazon.connect.hoursofoperation;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ConnectRequest;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationOverrideRequest;
import software.amazon.awssdk.services.connect.model.DeleteHoursOfOperationOverrideRequest;
import software.amazon.awssdk.services.connect.model.HoursOfOperationOverride;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationOverridesResponse;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UpdateHoursOfOperationOverrideRequest;
import software.amazon.awssdk.services.connect.model.UpdateHoursOfOperationRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The updateHandler support update of HoursOfOperation resource and its child entities - HoursOfOperationOverride
 * This method also supports Sync-service use-case to replicate resources with same resourceId by setting the
 * resourceId in the header.
 *
 *  If the request has Override resourceId in the request, it updates overrides if those overrides exist in database.In case
 *  the Ids do not match, it creates new overrides.
 *  If the request does not have Override resourceId, it uses Override name to compare data and follow similar flow to update overrides.
 *  It also calculates the diff between DesiredModel and PreviousModel and cleanups data that do not exist in desired model.
 */
public class UpdateHandler extends BaseHandlerStd {
    public static final String RESOURCE_ID_HEADER = "x-amz-resource-id";

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

        if (StringUtils.isNotEmpty(desiredStateModel.getInstanceArn()) && !desiredStateModel.getInstanceArn().equals(previousStateModel.getInstanceArn())) {
            throw new CfnInvalidRequestException("InstanceArn cannot be updated.");
        }

        if (desiredStateModel.getHoursOfOperationOverrides() == null
                && !driftDetectedInHoursOfOperation(desiredStateModel, previousStateModel, tagsToAdd, tagsToRemove)) {
            logger.log(String.format("HoursOfOperationUpdate request has no change from existing state, " +
                    "skipping update operation for HoursOfOperation:%s", desiredStateModel.getHoursOfOperationArn()));
            return ProgressEvent.defaultSuccessHandler(desiredStateModel);
        }

        ProgressEvent<ResourceModel, CallbackContext> hoopProgressEvent = ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateHoursOfOperation(proxy, proxyClient, desiredStateModel, callbackContext, logger))
                .then(progress -> unTagResource(proxy, proxyClient, desiredStateModel, tagsToRemove, progress, callbackContext, logger))
                .then(progress -> tagResource(proxy, proxyClient, desiredStateModel, tagsToAdd, progress, callbackContext, logger));

        if (desiredStateModel.getHoursOfOperationOverrides() == null) {
            return hoopProgressEvent.then(progress -> ProgressEvent.defaultSuccessHandler(desiredStateModel));
        }

        List<HoursOfOperationOverride> existingOverrides = getExistingHoursOfOperationOverrides(proxyClient, previousStateModel, logger);

        OverrideUpdateOperationResolver overrideUpdateOperationResolver = new OverrideUpdateOperationResolver();
        overrideUpdateOperationResolver.handleOverrides(proxy, proxyClient, callbackContext,
                desiredStateModel, existingOverrides, hoopProgressEvent, logger);

        return hoopProgressEvent.then(progress -> ProgressEvent.defaultSuccessHandler(desiredStateModel));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateHoursOfOperation(final AmazonWebServicesClientProxy proxy,
                                                                                 final ProxyClient<ConnectClient> proxyClient,
                                                                                 final ResourceModel desiredStateModel,
                                                                                 final CallbackContext context,
                                                                                 final Logger logger) {

        logger.log(String.format("Calling UpdateHoursOfOperation API for HoursOfOperation:%s", desiredStateModel.getHoursOfOperationArn()));
        return proxy.initiate("connect::updateHoursOfOperation", proxyClient, desiredStateModel, context)
                .translateToServiceRequest(desired -> translateToUpdateHoursOfOperationRequest(desiredStateModel))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateHoursOfOperation, logger))
                .done(response -> ProgressEvent.progress(desiredStateModel, context));
    }

    private ProgressEvent<ResourceModel, CallbackContext> unTagResource(final AmazonWebServicesClientProxy proxy,
                                                                        final ProxyClient<ConnectClient> proxyClient,
                                                                        final ResourceModel desiredStateModel,
                                                                        final Set<Tag> tagsToRemove,
                                                                        final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                        final CallbackContext context,
                                                                        final Logger logger) {
        final String hoursOfOperationArn = desiredStateModel.getHoursOfOperationArn();

        if (tagsToRemove.size() > 0) {
            logger.log(String.format("Tags have been removed in the update operation, " +
                    "Calling UnTagResource API for HoursOfOperation:%s", hoursOfOperationArn));
            return proxy.initiate("connect::untagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToUntagRequest(hoursOfOperationArn, tagsToRemove))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::untagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No removal of tags in update operation, skipping UnTagResource API call " +
                "for HoursOfOperation:%s", hoursOfOperationArn));
        return progress;
    }

    private ProgressEvent<ResourceModel, CallbackContext> tagResource(final AmazonWebServicesClientProxy proxy,
                                                                      final ProxyClient<ConnectClient> proxyClient,
                                                                      final ResourceModel desiredStateModel,
                                                                      final Set<Tag> tagsToAdd,
                                                                      final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                      final CallbackContext context,
                                                                      final Logger logger) {
        final String hoursOfOperationArn = desiredStateModel.getHoursOfOperationArn();

        if (tagsToAdd.size() > 0) {
            logger.log(String.format("Tags have been modified(addition/TagValue updated) in the update operation, " +
                    "Calling TagResource API for HoursOfOperation:%s", hoursOfOperationArn));
            return proxy.initiate("connect::tagResource", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(desired -> translateToTagRequest(hoursOfOperationArn, tagsToAdd))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::tagResource, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, context));
        }
        logger.log(String.format("No new tags or change in value for existing keys in update operation," +
                " skipping TagResource API call for HoursOfOperation:%s", hoursOfOperationArn));
        return progress;
    }

    private boolean driftDetectedInHoursOfOperation(ResourceModel desiredStateModel,
                                                    ResourceModel previousStateModel,
                                                    Set<Tag> tagsToRemove,
                                                    Set<Tag> tagsToAdd) {
        // skipping validating the HOOPId as new HOOPs does not have ids
        boolean sameHoursOfOperation = StringUtils.equals(desiredStateModel.getInstanceArn(), previousStateModel.getInstanceArn())
                && StringUtils.equals(desiredStateModel.getHoursOfOperationArn(), previousStateModel.getHoursOfOperationArn())
                && StringUtils.equals(desiredStateModel.getName(), previousStateModel.getName())
                && StringUtils.equals(desiredStateModel.getDescription(), previousStateModel.getDescription())
                && StringUtils.equals(desiredStateModel.getTimeZone(), previousStateModel.getTimeZone())
                && desiredStateModel.getConfig().equals(previousStateModel.getConfig());
        return !sameHoursOfOperation || !tagsToAdd.isEmpty() || !tagsToRemove.isEmpty();
    }

    private List<HoursOfOperationOverride> getExistingHoursOfOperationOverrides(
            final ProxyClient<ConnectClient> proxyClient,
            final ResourceModel previousStateModel,
            final Logger logger) {
        List<HoursOfOperationOverride> hoursOfOperationOverrides = new ArrayList<>();
        String nextToken = null;
        logger.log(String.format("Calling ListHoursOfOperationOverrides API for HoursOfOperation: %s",
                previousStateModel.getHoursOfOperationArn()));
        do {
            ListHoursOfOperationOverridesResponse response = listHoursOfOperationOverrides(previousStateModel, proxyClient, nextToken);
            if (response == null || response.hoursOfOperationOverrideList() == null) {
                return hoursOfOperationOverrides;
            }
            hoursOfOperationOverrides.addAll(response.hoursOfOperationOverrideList());
            nextToken = response.nextToken();
        } while (nextToken != null);
        return hoursOfOperationOverrides;
    }

    private UpdateHoursOfOperationRequest translateToUpdateHoursOfOperationRequest(final ResourceModel model) {

        return UpdateHoursOfOperationRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .hoursOfOperationId(model.getHoursOfOperationArn())
                .name(model.getName())
                .description(model.getDescription() == null ? "" : model.getDescription())
                .config(translateToHoursOfOperationConfig(model))
                .timeZone(model.getTimeZone())
                .build();
    }

    private UntagResourceRequest translateToUntagRequest(final String hoursOfOperationArn, final Set<Tag> tags) {
        final Set<String> tagKeys = streamOfOrEmpty(tags).map(Tag::getKey).collect(Collectors.toSet());

        return UntagResourceRequest.builder()
                .resourceArn(hoursOfOperationArn)
                .tagKeys(tagKeys)
                .build();
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    private TagResourceRequest translateToTagRequest(final String hoursOfOperationArn, final Set<Tag> tags) {
        return TagResourceRequest.builder()
                .resourceArn(hoursOfOperationArn)
                .tags(translateTagsToSdk(tags))
                .build();
    }

    private Map<String, String> translateTagsToSdk(final Set<Tag> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::getKey,
                Tag::getValue));
    }

    @Getter
    enum OverrideRequestType {
        DELETE(0),
        UPDATE(1),
        CREATE(2);

        private final int value;

        OverrideRequestType(int value) {
            this.value = value;
        }
    }

    @Getter
    static class OverrideRequest {
        private final ConnectRequest request;
        private final OverrideRequestType requestType;

        public OverrideRequest(ConnectRequest request, OverrideRequestType requestType) {
            this.request = request;
            this.requestType = requestType;
        }
    }

    static class OverrideUpdateOperationResolver {
        public void handleOverrides(final AmazonWebServicesClientProxy proxy,
                                    final ProxyClient<ConnectClient> proxyClient,
                                    final CallbackContext callbackContext,
                                    final ResourceModel desiredStateModel,
                                    List<HoursOfOperationOverride> existingOverrides,
                                    ProgressEvent<ResourceModel, CallbackContext> hoopProgressEvent,
                                    final Logger logger) {
            HoursOfOperationOverrideDriftState overrideDriftState = new HoursOfOperationOverrideDriftState(existingOverrides);

            final List<software.amazon.connect.hoursofoperation.HoursOfOperationOverride> desiredOverridesList = Optional.ofNullable(desiredStateModel
                    .getHoursOfOperationOverrides()).orElse(new ArrayList<>());

            List<OverrideRequest> overrideRequests = new ArrayList<>();
            overrideRequests.addAll(desiredOverridesList
                    .stream()
                    .map(desiredOverride -> overrideDriftState.generateUpsertOperation(desiredStateModel, desiredOverride))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()));

            overrideRequests.addAll(overrideDriftState.generateDeleteOperation(desiredStateModel));

            // sort based on the api-verb, order of preference delete > update > create
            overrideRequests.sort(Comparator.comparingInt(req -> req.getRequestType().getValue()));

            // Execute Requests
            overrideRequests
                    .forEach(request -> processOverrideRequest(proxy, proxyClient, callbackContext, desiredStateModel,
                            request, hoopProgressEvent, logger));
        }

        public void processOverrideRequest(final AmazonWebServicesClientProxy proxy,
                                           final ProxyClient<ConnectClient> proxyClient,
                                           final CallbackContext callbackContext,
                                           final ResourceModel desiredStateModel,
                                           OverrideRequest request,
                                           ProgressEvent<ResourceModel, CallbackContext> hoopProgressEvent,
                                           Logger logger) {
            ConnectRequest req = request.getRequest();
            switch (request.getRequestType()) {
                case CREATE:
                    hoopProgressEvent.then(progress -> createHoursOfOperationOverride(
                            proxy, proxyClient, desiredStateModel, callbackContext, (CreateHoursOfOperationOverrideRequest) req, logger));
                    break;
                case UPDATE:
                    hoopProgressEvent.then(progress -> updateHoursOfOperationOverride(
                            proxy, proxyClient, desiredStateModel, callbackContext, (UpdateHoursOfOperationOverrideRequest) req, logger));
                    break;
                case DELETE:
                    hoopProgressEvent.then(progress -> deleteHoursOfOperationOverride(
                            proxy, proxyClient, desiredStateModel, callbackContext, (DeleteHoursOfOperationOverrideRequest) req, logger));
                    break;
            }
        }

        private ProgressEvent<ResourceModel, CallbackContext> updateHoursOfOperationOverride(final AmazonWebServicesClientProxy proxy,
                                                                                             final ProxyClient<ConnectClient> proxyClient,
                                                                                             final ResourceModel desiredStateModel,
                                                                                             final CallbackContext callbackContext,
                                                                                             final UpdateHoursOfOperationOverrideRequest updateRequest,
                                                                                             final Logger logger) {
            logger.log(String.format("Calling UpdateHoursOfOperationOverride API for HoursOfOperation: %s, HoursOfOperationOverride: %s",
                    desiredStateModel.getHoursOfOperationArn(), updateRequest.hoursOfOperationOverrideId()));
            return proxy.initiate("connect::updateHoursOfOperationOverride", proxyClient, desiredStateModel, callbackContext)
                    .translateToServiceRequest(resourceModel -> updateRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateHoursOfOperationOverride, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel,callbackContext));
        }

        private ProgressEvent<ResourceModel, CallbackContext> deleteHoursOfOperationOverride(final AmazonWebServicesClientProxy proxy,
                                                                                             final ProxyClient<ConnectClient> proxyClient,
                                                                                             final ResourceModel desiredStateModel,
                                                                                             final CallbackContext callbackContext,
                                                                                             final DeleteHoursOfOperationOverrideRequest deleteRequest,
                                                                                             final Logger logger) {
            logger.log(String.format("Calling DeleteHoursOfOperationOverride API for HoursOfOperation: %s, HoursOfOperationOverride: %s",
                    desiredStateModel.getHoursOfOperationArn(), desiredStateModel.getHoursOfOperationOverrides()));
            return proxy.initiate("connect::deleteHoursOfOperationOverride", proxyClient, desiredStateModel, callbackContext)
                    .translateToServiceRequest(resourceModel -> deleteRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deleteHoursOfOperationOverride, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel,callbackContext));
        }

        private ProgressEvent<ResourceModel, CallbackContext> createHoursOfOperationOverride(final AmazonWebServicesClientProxy proxy,
                                                                                             final ProxyClient<ConnectClient> proxyClient,
                                                                                             final ResourceModel desiredStateModel,
                                                                                             final CallbackContext callbackContext,
                                                                                             final CreateHoursOfOperationOverrideRequest createOverrideRequest,
                                                                                             final Logger logger) {
            logger.log(String.format("Calling CreateHoursOfOperationOverride API for HoursOfOperation: %s",
                    desiredStateModel.getHoursOfOperationArn()));
            return proxy.initiate("connect::createHoursOfOperationOverride", proxyClient, desiredStateModel, callbackContext)
                    .translateToServiceRequest(resourceModel -> createOverrideRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createHoursOfOperationOverride, logger))
                    .done(response -> ProgressEvent.progress(desiredStateModel, callbackContext));
        }
    }

    static class HoursOfOperationOverrideDriftState {
        Set<HoursOfOperationOverride> overridesRequiringChanges;
        Map<String, software.amazon.awssdk.services.connect.model.HoursOfOperationOverride> existingOverridesIdMap = new HashMap<>();
        Map<String, software.amazon.awssdk.services.connect.model.HoursOfOperationOverride> existingOverridesNameMap = new HashMap<>();

        public HoursOfOperationOverrideDriftState(List<software.amazon.awssdk.services.connect.model.HoursOfOperationOverride> existingOverrides) {
            overridesRequiringChanges = new HashSet<>(existingOverrides);
            existingOverridesIdMap = overridesRequiringChanges.stream()
                    .collect(Collectors.toMap(HoursOfOperationOverride::hoursOfOperationOverrideId, override -> override));
            existingOverridesNameMap = overridesRequiringChanges.stream()
                    .collect(Collectors.toMap(HoursOfOperationOverride::name, override -> override));
        }

        public Optional<OverrideRequest> generateUpsertOperation(final ResourceModel desiredStateModel,
                                                                 final software.amazon.connect.hoursofoperation.HoursOfOperationOverride desiredOverride) {
            Optional<String> overrideId = Optional.ofNullable(desiredOverride.getHoursOfOperationOverrideId());

            if (overrideId.isPresent()
                    && existingOverridesIdMap.containsKey(overrideId.get())
                    && overridesRequiringChanges.contains(existingOverridesIdMap.get(overrideId.get()))) {
                HoursOfOperationOverride updatedOverride = existingOverridesIdMap.remove(overrideId.get());
                overridesRequiringChanges.remove(updatedOverride);

                if (noDriftDetectedInOverrides(desiredOverride, translateFromSingleHOOPOverrideModel(updatedOverride))) {
                    return Optional.empty();
                }
                return Optional.of(new OverrideRequest(buildUpdateHoursOfOperationOverrideRequest(desiredStateModel, desiredOverride),
                        OverrideRequestType.UPDATE));
            }

            if (!overrideId.isPresent()
                    && existingOverridesNameMap.containsKey(desiredOverride.getOverrideName())
                    && overridesRequiringChanges.contains(existingOverridesNameMap.get(desiredOverride.getOverrideName()))) {
                HoursOfOperationOverride updatedOverride = existingOverridesNameMap.remove(desiredOverride.getOverrideName());
                overridesRequiringChanges.remove(updatedOverride);

                desiredOverride.setHoursOfOperationOverrideId(updatedOverride.hoursOfOperationOverrideId());
                if (noDriftDetectedInOverrides(desiredOverride, translateFromSingleHOOPOverrideModel(updatedOverride))) {
                    return Optional.empty();
                }
                return Optional.of(new OverrideRequest(buildUpdateHoursOfOperationOverrideRequest(desiredStateModel, desiredOverride),
                        OverrideRequestType.UPDATE));
            }

            return Optional.of(new OverrideRequest(buildCreateHoursOfOperationOverrideRequest(desiredStateModel, desiredOverride),
                    OverrideRequestType.CREATE));
        }

        public List<OverrideRequest> generateDeleteOperation(final ResourceModel desiredStateModel) {
            return overridesRequiringChanges.stream()
                    .map(overrideToBeDeleted -> buildDeleteHoursOfOperationOverrideRequest(desiredStateModel, overrideToBeDeleted))
                    .map(connectRequest -> new OverrideRequest(connectRequest, OverrideRequestType.DELETE))
                    .collect(Collectors.toList());
        }

        private UpdateHoursOfOperationOverrideRequest buildUpdateHoursOfOperationOverrideRequest(final ResourceModel desiredStateModel,
                                                                                                 software.amazon.connect.hoursofoperation.HoursOfOperationOverride desiredOverride) {
            return UpdateHoursOfOperationOverrideRequest.builder()
                    .instanceId(desiredStateModel.getInstanceArn())
                    .hoursOfOperationId(desiredStateModel.getHoursOfOperationArn())
                    .hoursOfOperationOverrideId(desiredOverride.getHoursOfOperationOverrideId())
                    .name(desiredOverride.getOverrideName())
                    .description(desiredOverride.getOverrideDescription())
                    .effectiveFrom(desiredOverride.getEffectiveFrom())
                    .effectiveTill(desiredOverride.getEffectiveTill())
                    .config(translateToHOOPOverrideConfigModel(desiredOverride))
                    .build();
        }

        private CreateHoursOfOperationOverrideRequest buildCreateHoursOfOperationOverrideRequest(final ResourceModel desiredStateModel,
                                                                                                 software.amazon.connect.hoursofoperation.HoursOfOperationOverride desiredOverride) {
            // If request has OverrideId, set the OverrideId in header
            // If request does not have OverrideId, set empty header so sync-service does not set HOOPId in header
            Map<String, List<String>> overrideHeader = new HashMap<>();
            overrideHeader.put(RESOURCE_ID_HEADER, new ArrayList<>(Collections.singletonList("")));
            if (desiredOverride.getHoursOfOperationOverrideId() != null) {
                overrideHeader.put(RESOURCE_ID_HEADER, new ArrayList<>(Collections.singletonList
                        (desiredOverride.getHoursOfOperationOverrideId())));
            }

            return CreateHoursOfOperationOverrideRequest.builder()
                    .instanceId(desiredStateModel.getInstanceArn())
                    .hoursOfOperationId(desiredStateModel.getHoursOfOperationArn())
                    .name(desiredOverride.getOverrideName())
                    .description(desiredOverride.getOverrideDescription())
                    .effectiveFrom(desiredOverride.getEffectiveFrom())
                    .effectiveTill(desiredOverride.getEffectiveTill())
                    .config(translateToHOOPOverrideConfigModel(desiredOverride))
                    .overrideConfiguration(AwsRequestOverrideConfiguration.builder()
                            .headers(overrideHeader).build())
                    .build();
        }

        private ConnectRequest buildDeleteHoursOfOperationOverrideRequest(final ResourceModel model, HoursOfOperationOverride override) {
            return DeleteHoursOfOperationOverrideRequest.builder()
                    .instanceId(model.getInstanceArn())
                    .hoursOfOperationId(model.getHoursOfOperationArn())
                    .hoursOfOperationOverrideId(override.hoursOfOperationOverrideId())
                    .build();
        }

        private Boolean noDriftDetectedInOverrides(software.amazon.connect.hoursofoperation.HoursOfOperationOverride desiredOverride,
                                                   software.amazon.connect.hoursofoperation.HoursOfOperationOverride existingOverride) {
            return (Sets.difference(existingOverride.getOverrideConfig(), desiredOverride.getOverrideConfig()).isEmpty()
                    && StringUtils.equals(desiredOverride.getHoursOfOperationOverrideId(), existingOverride.getHoursOfOperationOverrideId())
                    && StringUtils.equals(desiredOverride.getOverrideName(), existingOverride.getOverrideName())
                    && StringUtils.equals(desiredOverride.getOverrideDescription(), existingOverride.getOverrideDescription())
                    && StringUtils.equals(desiredOverride.getEffectiveFrom(), existingOverride.getEffectiveFrom())
                    && StringUtils.equals(desiredOverride.getEffectiveTill(), existingOverride.getEffectiveTill()));
        }
    }
}