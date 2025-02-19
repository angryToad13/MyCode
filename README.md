# MyCode

@Service
public class CustomJsonService {

    // Regex: (digits):{(any non-} chars)}
    private static final Pattern CHUNK_PATTERN = Pattern.compile("(\\d+):\\{([^}]*)\\}");

    /**
     * Process a large file line by line.
     */
    public void processLargeFile(Path filePath) throws IOException {
        // Use a try-with-resources so the file stream closes automatically
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach(line -> {
                // Optionally check if line has the pattern you care about (e.g., "Text (")
                if (line.contains("Text (")) {
                    // Parse the line
                    Map<String, Object> result = parseCustomJsonLine(line);

                    // TODO: handle 'result' 
                    // e.g., store in a database, log it, or add to a global list
                    System.out.println("Parsed: " + result);
                }
            });
        }
    }

    /**
     * Parse a single line of custom "JSON".
     * Example line:
     *   Text (1:{F21BPAHKH0AOXX7750S132} 3:{177:2501071854} 451:{108:7J1CVO9B0HK0B6XX})
     */
    private Map<String, Object> parseCustomJsonLine(String line) {
        Map<String, Object> parsed = new LinkedHashMap<>();
        Matcher matcher = CHUNK_PATTERN.matcher(line);

        while (matcher.find()) {
            String outerKey = matcher.group(1);   // e.g. "1", "3", "451"
            String innerValue = matcher.group(2); // e.g. "F21BPAHKH0AOXX7750S132" or "177:2501071854"

            // If innerValue has a colon, split it into subkey/subvalue
            if (innerValue.contains(":")) {
                String[] parts = innerValue.split(":", 2);
                if (parts.length == 2) {
                    Map<String, String> nested = new LinkedHashMap<>();
                    nested.put(parts[0], parts[1]);
                    parsed.put(outerKey, nested);
                } else {
                    parsed.put(outerKey, innerValue);
                }
            } else {
                parsed.put(outerKey, innerValue);
            }
        }
        return parsed;
    }
}
