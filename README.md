@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User updateUserDynamically(DynamicUpdateDTO dto) {
        User user = userRepository.findById(dto.getId())
                                  .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> fields = dto.getFields();
        Class<?> clazz = user.getClass();

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            try {
                Field field = clazz.getDeclaredField(entry.getKey());
                field.setAccessible(true);

                // Type conversion if needed
                Object value = convertValue(entry.getValue(), field.getType());
                field.set(user, value);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Error updating field: " + entry.getKey(), e);
            }
        }

        return userRepository.save(user);
    }

    public List<User> updateUsersDynamically(List<DynamicUpdateDTO> dtoList) {
        List<User> updated = new ArrayList<>();
        for (DynamicUpdateDTO dto : dtoList) {
            updated.add(updateUserDynamically(dto));
        }
        return updated;
    }

    // Helper: Basic conversion (optional enhancement)
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;

        if (targetType == Boolean.class || targetType == boolean.class)
            return Boolean.parseBoolean(value.toString());
        if (targetType == Long.class || targetType == long.class)
            return Long.parseLong(value.toString());
        if (targetType == Integer.class || targetType == int.class)
            return Integer.parseInt(value.toString());
        if (targetType == String.class)
            return value.toString();

        throw new IllegalArgumentException("Unsupported type conversion for: " + targetType);
    }
}