package software.amazon.connect.tasktemplate;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceModelMapper {
    public static Constraints toResourceModelConstraint(software.amazon.awssdk.services.connect.model.TaskTemplateConstraints constraints){
        List<InvisibleFieldInfo> invisibleFieldInfoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(constraints.invisibleFields())){
            for(software.amazon.awssdk.services.connect.model.InvisibleFieldInfo invisibleFieldInfo : constraints.invisibleFields()){
                InvisibleFieldInfo info = InvisibleFieldInfo.builder()
                        .id(FieldIdentifier.builder()
                                .name(invisibleFieldInfo.id().name())
                                .build())
                        .build();
                invisibleFieldInfoList.add(info);
            }


        }

        List<ReadOnlyFieldInfo> readOnlyFieldInfoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(constraints.readOnlyFields())){
            for(software.amazon.awssdk.services.connect.model.ReadOnlyFieldInfo readOnlyFieldInfo : constraints.readOnlyFields()){
                ReadOnlyFieldInfo info = ReadOnlyFieldInfo.builder()
                        .id(FieldIdentifier.builder()
                                .name(readOnlyFieldInfo.id().name())
                                .build())
                        .build();
                readOnlyFieldInfoList.add(info);
            }
        }

        List<RequiredFieldInfo> requiredFieldInfoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(constraints.requiredFields())){

            for(software.amazon.awssdk.services.connect.model.RequiredFieldInfo requiredFieldInfo : constraints.requiredFields()){
                RequiredFieldInfo info = RequiredFieldInfo.builder()
                        .id(FieldIdentifier.builder()
                                .name(requiredFieldInfo.id().name())
                                .build())
                        .build();
                requiredFieldInfoList.add(info);
            }
        }

        return Constraints.builder()
                .invisibleFields(invisibleFieldInfoList)
                .readOnlyFields(readOnlyFieldInfoList)
                .requiredFields(requiredFieldInfoList)
                .build();
    }

    public static List<DefaultFieldValue> toResourceModelDefaults(software.amazon.awssdk.services.connect.model.TaskTemplateDefaults defaults){
        List<DefaultFieldValue> taskTemplateDefaultFieldValueList =  new ArrayList<>();
        if(!Objects.isNull(defaults)){
            for(software.amazon.awssdk.services.connect.model.TaskTemplateDefaultFieldValue fieldValue: defaults.defaultFieldValues()){
                DefaultFieldValue value = DefaultFieldValue.builder()
                        .defaultValue(fieldValue.defaultValue())
                        .id(FieldIdentifier.builder().name(fieldValue.id().name()).build())
                        .build();
                taskTemplateDefaultFieldValueList.add(value);
            }
        }
        return taskTemplateDefaultFieldValueList;
    }

    public static List<Field> toResourceModelFields(List<software.amazon.awssdk.services.connect.model.TaskTemplateField> fields){
        List<Field> taskTemplateFieldList =  new ArrayList<>();
        for(software.amazon.awssdk.services.connect.model.TaskTemplateField field: fields){
            Field info = Field.builder()
                    .description(field.description())
                    .type(field.type().name())
                    .id(FieldIdentifier.builder().name(field.id().name()).build())
                    .singleSelectOptions(field.singleSelectOptions())
                    .build();
            taskTemplateFieldList.add(info);
        }
        return taskTemplateFieldList;
    }

}

