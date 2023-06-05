package software.amazon.connect.tasktemplate;

import com.google.common.collect.ImmutableMap;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mock;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.GetTaskTemplateResponse;
import software.amazon.awssdk.services.connect.model.TaskTemplateConstraints;
import software.amazon.awssdk.services.connect.model.TaskTemplateDefaultFieldValue;
import software.amazon.awssdk.services.connect.model.TaskTemplateDefaults;
import software.amazon.awssdk.services.connect.model.TaskTemplateField;
import software.amazon.awssdk.services.connect.model.TaskTemplateFieldIdentifier;
import software.amazon.awssdk.services.connect.model.TaskTemplateFieldType;
import software.amazon.awssdk.services.connect.model.TaskTemplateStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class BaseTest {

    public static final String TASK_TEMPLATE_ARN = "arn:aws:connect:us-west-2:364337219280:instance/d152de7f-d3fc-427e-a19c-12e08fcff97d/task-template/8512f989-440a-4abd-b968-ad1d65e06206";
    public static final String INVALID_FORMAT_TASK_TEMPLATE_ARN = "arn:aws:connect:us-west-2:364337219280:instance/d152de7f-d3fc-427e-a19c-12e08fcff97d/task-template/8512f989-440a-4abd-b968-zd1d65e06206";
    public static final String INVALID_FORMAT_TASK_TEMPLATE_ARN_2 = "arn:aws:connect:us-west-2:364337219280:instance/d152de7f-d3fc-427e-a19c-12e08fcff97d/task-template/";

    public static final String TASK_TEMPLATE_ID = "8512f989-440a-4abd-b968-ad1d65e06206";
    public static final String INSTANCE_ID = "d152de7f-d3fc-427e-a19c-12e08fcff97d";
    public static final String CONTACT_FLOW_ARN = "arn:aws:connect:us-west-2:364337219280:instance/d152de7f-d3fc-427e-a19c-12e08fcff97d/contact-flow/8512f989-440a-4abd-b968-ad1d65e06206";
    public static final String INVALID_TASK_TEMPLATE_ARN = "aws:connect:us-west-2:235454366653:instance/2b1f77d7-2460-460c-a7e9-be2b69e95ab4/task-template/8512f989-440a-4abd-b968-ad1d65e06206";

    public static final String INSTANCE_ARN = "arn:aws:connect:us-west-2:364337219280:instance/d152de7f-d3fc-427e-a19c-12e08fcff97d";
    public static final String INSTANCE_ARN_2 = "arn:aws:connect:us-west-2:364337219280:instance/d152de7f-d3fc-427e-a19c-12e08fcff97e";
    public static final String TASK_TEMPLATE_NAME = "task template name";
    public static final String TASK_TEMPLATE_DESCRIPTION = "task template description";
    public static final String FIELD_1 = "field_1";
    protected static final Map<String, String> TAGS_ONE = ImmutableMap.of("key1", "value1");
    protected static final Map<String, String> TAGS_TWO = ImmutableMap.of("key2","value2");

    protected AmazonWebServicesClientProxy proxy;
    protected ProxyClient<ConnectClient> proxyClient;
    protected LoggerProxy logger;

    @Mock
    protected ConnectClient connectClient;

    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    protected static ResourceModel buildTaskTemplateDesiredStateResourceModel() {
        return buildTaskTemplateDesiredStateResourceModel(null);
    }
    protected static ResourceModel buildTaskTemplateDesiredStateResourceModel(String arn) {
        Constraints constraints = Constraints.builder()
                .invisibleFields(Arrays.asList(InvisibleFieldInfo.builder().id(FieldIdentifier.builder().name(FIELD_1).build()).build()))
                .readOnlyFields(Arrays.asList(ReadOnlyFieldInfo.builder().id(FieldIdentifier.builder().name(FIELD_1).build()).build()))
                .requiredFields(Arrays.asList(RequiredFieldInfo.builder().id(FieldIdentifier.builder().name(FIELD_1).build()).build()))
                .build();
        List<DefaultFieldValue> defaults = Arrays.asList(DefaultFieldValue.builder().id(FieldIdentifier.builder().name(FIELD_1).build()).defaultValue("value").build());
        Field field1 = Field.builder()
                .id(FieldIdentifier.builder().name(FIELD_1).build())
                .type("Name")
                .build();
        Tag tag = Tag.builder().key("devName").value("hoain").build();
        Set<Tag> tags = new HashSet<>();
        tags.add(tag);

        ResourceModel model =  ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .name(TASK_TEMPLATE_NAME)
                .description(TASK_TEMPLATE_DESCRIPTION)
                .contactFlowArn(CONTACT_FLOW_ARN)
                .constraints(constraints)
                .defaults(defaults)
                .fields(Arrays.asList(field1))
                .status("Active")
                .tags(tags)
                .build();
        if(StringUtils.isNotBlank(arn)){
            model.setArn(arn);
        }
        return model;
    }

    protected static GetTaskTemplateResponse buildGetTaskTemplateResponse(){

        TaskTemplateConstraints constraints = TaskTemplateConstraints.builder()
                .invisibleFields(Arrays.asList(software.amazon.awssdk.services.connect.model.InvisibleFieldInfo.builder()
                                .id(TaskTemplateFieldIdentifier.builder().name(FIELD_1).build()).build()))
                .readOnlyFields(Arrays.asList(software.amazon.awssdk.services.connect.model.ReadOnlyFieldInfo.builder()
                        .id(TaskTemplateFieldIdentifier.builder().name(FIELD_1).build()).build()))
                .requiredFields(Arrays.asList(software.amazon.awssdk.services.connect.model.RequiredFieldInfo.builder()
                        .id(TaskTemplateFieldIdentifier.builder().name(FIELD_1).build()).build()))
                .build();

        TaskTemplateDefaults defaults = TaskTemplateDefaults.builder()
                .defaultFieldValues(Arrays.asList(TaskTemplateDefaultFieldValue.builder()
                        .id(TaskTemplateFieldIdentifier.builder().name(FIELD_1).build()).defaultValue("value")
                        .build()))
                .build();


        TaskTemplateField field = TaskTemplateField.builder()
                .type(TaskTemplateFieldType.NAME)
                .id(TaskTemplateFieldIdentifier.builder().name(FIELD_1).build())
                .build();

        GetTaskTemplateResponse response = GetTaskTemplateResponse.builder()
                .id(TASK_TEMPLATE_ID)
                .arn(TASK_TEMPLATE_ARN)
                .instanceId(INSTANCE_ARN)
                .name(TASK_TEMPLATE_NAME)
                .description(TASK_TEMPLATE_DESCRIPTION)
                .constraints(constraints)
                .defaults(defaults)
                .fields(Arrays.asList(field))
                .status(TaskTemplateStatus.ACTIVE)
                .build();
        return response;
    }
}
