import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FieldMapperUtil {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final Map<Class<?>, Map<String, MethodHandle>> GETTER_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, MethodHandle>> SETTER_CACHE = new ConcurrentHashMap<>();

    private FieldMapperUtil() {}

    public static <S, T> void mapFields(
            S source,
            T target,
            Map<String, String> fieldMapping
    ) {
        if (source == null || target == null || fieldMapping == null || fieldMapping.isEmpty()) {
            return;
        }

        Map<String, MethodHandle> sourceGetters = getGetters(source.getClass());
        Map<String, MethodHandle> targetSetters = getSetters(target.getClass());

        fieldMapping.forEach((targetField, sourceField) -> {
            try {
                MethodHandle getter = sourceGetters.get(sourceField);
                MethodHandle setter = targetSetters.get(targetField);

                if (getter != null && setter != null) {
                    Object value = getter.invoke(source);
                    if (value != null) {
                        setter.invoke(target, value);
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException(
                        "Failed to map field '" + sourceField + "' to '" + targetField + "'", e);
            }
        });
    }

    private static Map<String, MethodHandle> getGetters(Class<?> clazz) {
        return GETTER_CACHE.computeIfAbsent(clazz, FieldMapperUtil::loadGetters);
    }

    private static Map<String, MethodHandle> getSetters(Class<?> clazz) {
        return SETTER_CACHE.computeIfAbsent(clazz, FieldMapperUtil::loadSetters);
    }

    private static Map<String, MethodHandle> loadGetters(Class<?> clazz) {
        return loadMethods(clazz, true);
    }

    private static Map<String, MethodHandle> loadSetters(Class<?> clazz) {
        return loadMethods(clazz, false);
    }

    private static Map<String, MethodHandle> loadMethods(Class<?> clazz, boolean getters) {
        try {
            Map<String, MethodHandle> map = new ConcurrentHashMap<>();
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (getters && pd.getReadMethod() != null) {
                    map.put(pd.getName(), LOOKUP.unreflect(pd.getReadMethod()));
                } else if (!getters && pd.getWriteMethod() != null) {
                    map.put(pd.getName(), LOOKUP.unreflect(pd.getWriteMethod()));
                }
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Failed to introspect class: " + clazz, e);
        }
    }
}







public enum UserFieldMapping {

    NAME(UserDTO::setName, UserEntity::getFirstName),
    EMAIL(UserDTO::setEmailId, UserEntity::getEmail),
    AGE(UserDTO::setAge, UserEntity::getAge);

    private final BiConsumer<UserDTO, Object> targetSetter;
    private final Function<UserEntity, Object> sourceGetter;

    <T> UserFieldMapping(
            BiConsumer<UserDTO, T> setter,
            Function<UserEntity, T> getter
    ) {
        this.targetSetter = (BiConsumer<UserDTO, Object>) setter;
        this.sourceGetter = (Function<UserEntity, Object>) getter;
    }

    public void apply(UserEntity source, UserDTO target) {
        Object value = sourceGetter.apply(source);
        if (value != null) {
            targetSetter.accept(target, value);
        }
    }
}




import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class FieldMappingHelper {

    private FieldMappingHelper() {}

    public static <E extends Enum<E> & FieldMappingEnum>
    Map<String, String> toPartialMap(
            Class<E> enumClass,
            List<String> requestedFields
    ) {

        if (requestedFields == null || requestedFields.isEmpty()) {
            return Map.of();
        }

        return requestedFields.stream()
                .map(field -> Enum.valueOf(enumClass, field))
                .collect(Collectors.toMap(
                        FieldMappingEnum::getTargetField,
                        FieldMappingEnum::getSourceField
                ));
    }
}







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