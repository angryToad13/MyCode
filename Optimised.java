public enum UserFieldMapping {

    NAME("name", "firstName"),
    EMAIL("emailId", "email"),
    AGE("age", "age");

    private final String targetField;
    private final String sourceField;

    UserFieldMapping(String targetField, String sourceField) {
        this.targetField = targetField;
        this.sourceField = sourceField;
    }

    public String getTargetField() {
        return targetField;
    }

    public String getSourceField() {
        return sourceField;
    }
}




import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class FieldMappingHelper {

    private FieldMappingHelper() {}

    public static <E extends Enum<E> & FieldMappingEnum>
    Map<String, String> toMap(Class<E> enumClass) {

        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(
                        FieldMappingEnum::getTargetField,
                        FieldMappingEnum::getSourceField
                ));
    }
}






public interface FieldMappingEnum {

    String getTargetField();

    String getSourceField();
}





public enum UserFieldMapping implements FieldMappingEnum {

    NAME("name", "firstName"),
    EMAIL("emailId", "email"),
    AGE("age", "age");

    private final String targetField;
    private final String sourceField;

    UserFieldMapping(String targetField, String sourceField) {
        this.targetField = targetField;
        this.sourceField = sourceField;
    }

    public String getTargetField() {
        return targetField;
    }

    public String getSourceField() {
        return sourceField;
    }
}





@Service
public class UserService {

    public UserDTO convert(UserEntity entity) {

        UserDTO dto = new UserDTO();

        Map<String, String> fieldMap =
                FieldMappingHelper.toMap(UserFieldMapping.class);

        FieldMapperUtil.mapFields(entity, dto, fieldMap);

        return dto;
    }
}



Map<String, String> partialMap =
        Stream.of(UserFieldMapping.NAME, UserFieldMapping.EMAIL)
              .collect(Collectors.toMap(
                      FieldMappingEnum::getTargetField,
                      FieldMappingEnum::getSourceField
              ));