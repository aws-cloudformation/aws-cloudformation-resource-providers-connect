package software.amazon.connect.userhierarchygroup;

import software.amazon.awssdk.services.connect.model.HierarchyGroup;
import software.amazon.awssdk.services.connect.model.HierarchyGroupSummary;
import software.amazon.awssdk.services.connect.model.HierarchyPath;

public class UserHierarchyGroupTestDataProvider {
    protected static final String USER_HIERARCHY_GROUP_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent-group/userhierarchygroupId";
    protected static final String USER_HIERARCHY_GROUP_ID = "userhierarchygroupId";
    protected static final String USER_HIERARCHY_GROUP_NAME = "userhierarchygroupName";
    protected static final String USER_HIERARCHY_GROUP_UPDATED_NAME = "userhierarchygroupUpdatedName";
    protected static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId";
    protected static final String PARENT_HIERARCHY_GROUP_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent-group/parenthierarchygroupId";
    protected static final String LEVEL_ONE_HIERARCHY_GROUP_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent-group/levelonehierarchygroupId";
    protected static final String LEVEL_ONE_HIERARCHY_GROUP_ID = "leveltwohierarchygroupId";
    protected static final String LEVEL_ONE_HIERARCHY_GROUP_NAME = "leveltwohierarchygroupName";
    protected static final String LEVEL_TWO_HIERARCHY_GROUP_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent-group/leveltwohierarchygroupId";
    protected static final String LEVEL_TWO_HIERARCHY_GROUP_ID = "leveltwohierarchygroupId";
    protected static final String LEVEL_TWO_HIERARCHY_GROUP_NAME = "leveltwohierarchygroupName";
    protected static final String LEVEL_THREE_HIERARCHY_GROUP_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent-group/levelthreehierarchygroupId";
    protected static final String LEVEL_THREE_HIERARCHY_GROUP_ID = "levelthreehierarchygroupId";
    protected static final String LEVEL_THREE_HIERARCHY_GROUP_NAME = "levelthreehierarchygroupName";
    protected static final String LEVEL_FOUR_HIERARCHY_GROUP_ARN = "arn:aws:connect:us-west-2:111111111111:instance/instanceId/agent-group/levelfourhierarchygroupId";
    protected static final String LEVEL_FOUR_HIERARCHY_GROUP_ID = "levelfourhierarchygroupId";
    protected static final String LEVEL_FOUR_HIERARCHY_GROUP_NAME = "levelfourhierarchygroupName";
    protected static final String LEVEL_ID_FIVE = "5";
    protected static final String LEVEL_ID_FOUR = "4";
    protected static final String LEVEL_ID_THREE = "3";
    protected static final String LEVEL_ID_TWO = "2";
    protected static final String LEVEL_ID_ONE = "1";

    protected static ResourceModel buildUserHierarchyGroupResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .name(USER_HIERARCHY_GROUP_NAME)
                .parentGroupArn(PARENT_HIERARCHY_GROUP_ARN)
                .userHierarchyGroupArn(USER_HIERARCHY_GROUP_ARN)
                .build();
    }

    protected static ResourceModel buildUserHierarchyGroupNameResourceModel() {
        return ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .name(USER_HIERARCHY_GROUP_UPDATED_NAME)
                .parentGroupArn(PARENT_HIERARCHY_GROUP_ARN)
                .userHierarchyGroupArn(USER_HIERARCHY_GROUP_ARN)
                .build();
    }

    protected static HierarchyGroup generateGroupBasedOnLevelId(String levelId) {
        HierarchyGroupSummary levelOne = HierarchyGroupSummary.builder()
                .id(LEVEL_ONE_HIERARCHY_GROUP_ID)
                .arn(LEVEL_ONE_HIERARCHY_GROUP_ARN)
                .name(LEVEL_ONE_HIERARCHY_GROUP_NAME)
                .build();
        HierarchyGroupSummary levelTwo = HierarchyGroupSummary.builder()
                .id(LEVEL_TWO_HIERARCHY_GROUP_ID)
                .arn(LEVEL_TWO_HIERARCHY_GROUP_ARN)
                .name(LEVEL_TWO_HIERARCHY_GROUP_NAME)
                .build();
        HierarchyGroupSummary levelThree = HierarchyGroupSummary.builder()
                .id(LEVEL_THREE_HIERARCHY_GROUP_ID)
                .arn(LEVEL_THREE_HIERARCHY_GROUP_ARN)
                .name(LEVEL_THREE_HIERARCHY_GROUP_NAME)
                .build();
        HierarchyGroupSummary levelFour = HierarchyGroupSummary.builder()
                .id(LEVEL_FOUR_HIERARCHY_GROUP_ID)
                .arn(LEVEL_FOUR_HIERARCHY_GROUP_ARN)
                .name(LEVEL_FOUR_HIERARCHY_GROUP_NAME)
                .build();
        HierarchyGroupSummary levelCurrent = HierarchyGroupSummary.builder()
                .id(USER_HIERARCHY_GROUP_ID)
                .arn(USER_HIERARCHY_GROUP_ARN)
                .name(USER_HIERARCHY_GROUP_NAME)
                .build();

        int level = Integer.parseInt(levelId);
        switch (level) {
            case 1: {
                HierarchyPath path = HierarchyPath.builder()
                        .levelOne(levelCurrent)
                        .build();
                return HierarchyGroup.builder()
                        .hierarchyPath(path)
                        .arn(USER_HIERARCHY_GROUP_ARN)
                        .id(USER_HIERARCHY_GROUP_ID)
                        .name(USER_HIERARCHY_GROUP_NAME)
                        .levelId(levelId)
                        .build();
            }
            case 2: {
                HierarchyPath path = HierarchyPath.builder()
                        .levelOne(levelOne)
                        .levelTwo(levelCurrent)
                        .build();
                return HierarchyGroup.builder()
                        .hierarchyPath(path)
                        .arn(USER_HIERARCHY_GROUP_ARN)
                        .id(USER_HIERARCHY_GROUP_ID)
                        .name(USER_HIERARCHY_GROUP_NAME)
                        .levelId(levelId)
                        .build();
            }
            case 3: {
                HierarchyPath path = HierarchyPath.builder()
                        .levelOne(levelOne)
                        .levelTwo(levelTwo)
                        .levelThree(levelCurrent)
                        .build();
                return HierarchyGroup.builder()
                        .hierarchyPath(path)
                        .arn(USER_HIERARCHY_GROUP_ARN)
                        .id(USER_HIERARCHY_GROUP_ID)
                        .name(USER_HIERARCHY_GROUP_NAME)
                        .levelId(levelId)
                        .build();
            }
            case 4: {
                HierarchyPath path = HierarchyPath.builder()
                        .levelOne(levelOne)
                        .levelTwo(levelTwo)
                        .levelThree(levelThree)
                        .levelFour(levelCurrent)
                        .build();
                return HierarchyGroup.builder()
                        .hierarchyPath(path)
                        .arn(USER_HIERARCHY_GROUP_ARN)
                        .id(USER_HIERARCHY_GROUP_ID)
                        .name(USER_HIERARCHY_GROUP_NAME)
                        .levelId(levelId)
                        .build();
            }
            case 5: {
                HierarchyPath path = HierarchyPath.builder()
                        .levelOne(levelOne)
                        .levelTwo(levelTwo)
                        .levelThree(levelThree)
                        .levelFour(levelFour)
                        .levelFive(levelCurrent)
                        .build();
                return HierarchyGroup.builder()
                        .hierarchyPath(path)
                        .arn(USER_HIERARCHY_GROUP_ARN)
                        .id(USER_HIERARCHY_GROUP_ID)
                        .name(USER_HIERARCHY_GROUP_NAME)
                        .levelId(levelId)
                        .build();
            }
        }
        return null;
    }
}
