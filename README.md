# MyCode

@Service
public class JsonExtractorService {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, String>> extractJsonKeyValuePairs(Path filePath) throws IOException {
        String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        Pattern pattern = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        List<Map<String, String>> results = new ArrayList<>();
        while (matcher.find()) {
            String jsonBlock = matcher.group();
            try {
                JsonNode rootNode = mapper.readTree(jsonBlock);
                Map<String, String> keyValueMap = new HashMap<>();
                extractKeyValuePairs(rootNode, keyValueMap);
                results.add(keyValueMap);
            } catch (Exception e) {
                // Log and continue if JSON block parsing fails
                System.err.println("Error parsing JSON block: " + e.getMessage());
            }
        }
        return results;
    }

    private void extractKeyValuePairs(JsonNode node, Map<String, String> keyValueMap) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode child = entry.getValue();
                if (child.isValueNode()) {
                    keyValueMap.put(entry.getKey(), child.asText());
                } else {
                    extractKeyValuePairs(child, keyValueMap);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                extractKeyValuePairs(arrayElement, keyValueMap);
            }
        }
    }
}
