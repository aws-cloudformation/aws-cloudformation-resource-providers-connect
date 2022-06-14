package software.amazon.connect.tasktemplate;

import org.apache.commons.collections4.CollectionUtils;
import software.amazon.awssdk.services.connect.model.TaskTemplateConstraints;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskTemplateMapper {
    public static software.amazon.awssdk.services.connect.model.TaskTemplateConstraints toTaskTemplateConstraints(Constraints constraints){

        if(Objects.isNull(constraints)){
            return null;
        }
        List<software.amazon.awssdk.services.connect.model.InvisibleFieldInfo> invisibleFieldInfoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(constraints.getInvisibleFields())){
            for(InvisibleFieldInfo invisibleFieldInfo : constraints.getInvisibleFields()){
                software.amazon.awssdk.services.connect.model.InvisibleFieldInfo info = software.amazon.awssdk.services.connect.model.InvisibleFieldInfo.builder()
                        .id(software.amazon.awssdk.services.connect.model.TaskTemplateFieldIdentifier.builder()
                                .name(invisibleFieldInfo.getId().getName())
                                .build())
                        .build();
                invisibleFieldInfoList.add(info);
            }
        }

        List<software.amazon.awssdk.services.connect.model.ReadOnlyFieldInfo> readOnlyFieldInfoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(constraints.getReadOnlyFields())){
            for(ReadOnlyFieldInfo readOnlyFieldInfo : constraints.getReadOnlyFields()){
                software.amazon.awssdk.services.connect.model.ReadOnlyFieldInfo info = software.amazon.awssdk.services.connect.model.ReadOnlyFieldInfo.builder()
                        .id(software.amazon.awssdk.services.connect.model.TaskTemplateFieldIdentifier.builder()
                                .name(readOnlyFieldInfo.getId().getName())
                                .build())
                        .build();
                readOnlyFieldInfoList.add(info);
            }
        }

        List<software.amazon.awssdk.services.connect.model.RequiredFieldInfo> requiredFieldInfoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(constraints.getRequiredFields())){

            for(RequiredFieldInfo requiredFieldInfo : constraints.getRequiredFields()){
                software.amazon.awssdk.services.connect.model.RequiredFieldInfo info = software.amazon.awssdk.services.connect.model.RequiredFieldInfo.builder()
                        .id(software.amazon.awssdk.services.connect.model.TaskTemplateFieldIdentifier.builder()
                                .name(requiredFieldInfo.getId().getName())
                                .build())
                        .build();
                requiredFieldInfoList.add(info);
            }
        }

        TaskTemplateConstraints.Builder builder = software.amazon.awssdk.services.connect.model.TaskTemplateConstraints.builder();
        if(CollectionUtils.isNotEmpty(invisibleFieldInfoList)){
            builder.invisibleFields(invisibleFieldInfoList);
        }
        if(CollectionUtils.isNotEmpty(readOnlyFieldInfoList)){
            builder.readOnlyFields(readOnlyFieldInfoList);
        }
        if(CollectionUtils.isNotEmpty(requiredFieldInfoList)){
            builder.requiredFields(requiredFieldInfoList);
        }
        return builder.build();
    }

    public static software.amazon.awssdk.services.connect.model.TaskTemplateDefaults toTaskTemplateDefaults(List<DefaultFieldValue> defaults){
        List<software.amazon.awssdk.services.connect.model.TaskTemplateDefaultFieldValue> taskTemplateDefaultFieldValueList =  new ArrayList<>();

        for(DefaultFieldValue fieldValue: defaults){
            software.amazon.awssdk.services.connect.model.TaskTemplateDefaultFieldValue value = software.amazon.awssdk.services.connect.model.TaskTemplateDefaultFieldValue.builder()
                    .defaultValue(fieldValue.getDefaultValue())
                    .id(software.amazon.awssdk.services.connect.model.TaskTemplateFieldIdentifier.builder().name(fieldValue.getId().getName()).build())
                    .build();
            taskTemplateDefaultFieldValueList.add(value);
        }

        return software.amazon.awssdk.services.connect.model.TaskTemplateDefaults.builder()
                .defaultFieldValues(taskTemplateDefaultFieldValueList)
                .build();
    }

    public static List<software.amazon.awssdk.services.connect.model.TaskTemplateField> toTaskTemplateFields(List<Field> fields){
        List<software.amazon.awssdk.services.connect.model.TaskTemplateField> taskTemplateFieldList =  new ArrayList<>();
        for(Field field: fields){
            software.amazon.awssdk.services.connect.model.TaskTemplateField info = software.amazon.awssdk.services.connect.model.TaskTemplateField.builder()
                    .description(field.getDescription())
                    .type(field.getType())
                    .id(software.amazon.awssdk.services.connect.model.TaskTemplateFieldIdentifier.builder().name(field.getId().getName()).build())
                    .singleSelectOptions(field.getSingleSelectOptions())
                    .build();
            taskTemplateFieldList.add(info);
        }
        return taskTemplateFieldList;
    }
}