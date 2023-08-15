package software.amazon.connect.trafficdistributiongroup;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TagHelper {
    public static Map<String, String> convertToMap(final Set<Tag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyMap();
        }
        return tags.stream()
                .filter(tag -> tag.getValue() != null)
                .collect(Collectors.toMap(
                        Tag::getKey,
                        Tag::getValue,
                        (oldValue, newValue) -> newValue));
    }

    public static Set<Tag> convertToSet(final Map<String, String> tagMap) {
        if (MapUtils.isEmpty(tagMap)) {
            return Collections.emptySet();
        }
        return tagMap.entrySet().stream()
                .filter(tag -> tag.getValue() != null)
                .map(tag -> Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toSet());
    }

    /**
     * Determines whether user defined tags have been changed during update.
     */
    public static boolean shouldUpdateTags(final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final Map<String, String> previousTags = getPreviouslyAttachedTags(handlerRequest);
        final Map<String, String> desiredTags = getNewDesiredTags(handlerRequest);
        return ObjectUtils.notEqual(previousTags, desiredTags);
    }

    /**
     * If stack tags and resource tags are not merged together in Configuration class,
     * we will get previous attached user defined tags from both handlerRequest.getPreviousResourceTags (stack tags)
     * and handlerRequest.getPreviousResourceState (resource tags).
     */
    public static Map<String, String> getPreviouslyAttachedTags(final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        return new HashMap<>(handlerRequest.getPreviousResourceTags());
    }

    /**
     * If stack tags and resource tags are not merged together in Configuration class,
     * we will get new user defined tags from both resource model and previous stack tags.
     */
    public static  Map<String, String> getNewDesiredTags(final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        return new HashMap<>(handlerRequest.getDesiredResourceTags());
    }

    /**
     * Determines the tags the customer desired to define or redefine.
     */
    public static Map<String, String> generateTagsToAdd(final Map<String, String> previousTags,
                                                        final Map<String, String> desiredTags) {
        return desiredTags.entrySet().stream()
                .filter(e -> !previousTags.containsKey(e.getKey()) || !Objects.equals(previousTags.get(e.getKey()), e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    /**
     * Determines the tags the customer desired to remove from the function.
     */
    public static Map<String, String> generateTagsToRemove(final Map<String, String> previousTags,
                                                           final Map<String, String> desiredTags) {
        final Set<String> desiredTagNames = desiredTags.keySet();

        return previousTags.entrySet().stream()
                .filter(entry -> !desiredTagNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
