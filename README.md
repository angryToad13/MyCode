@Service
public class CustomJsonService {

    // Regex pattern to capture custom key/value chunks.
    private static final Pattern CHUNK_PATTERN = Pattern.compile("(\\d+):\\{([^}]*)\\}");

    /**
     * Processes a large file that may have JSON blocks spanning multiple lines.
     */
    public void processLargeFile(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            StringBuilder blockBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // Append the line to the current block buffer.
                blockBuilder.append(line).append(" ");

                // Check if the accumulated block appears complete.
                // One simple approach is to compare the count of '{' and '}'.
                if (isBlockComplete(blockBuilder.toString())) {
                    String block = blockBuilder.toString();
                    Map<String, Object> result = parseCustomJsonBlock(block);
                    if (!result.isEmpty()) {
                        System.out.println("Parsed: " + result);
                        // Process or store the result as needed.
                    }
                    // Reset the buffer for the next block.
                    blockBuilder.setLength(0);
                }
            }
            
            // Optionally process any remaining block.
            if (blockBuilder.length() > 0) {
                String block = blockBuilder.toString();
                Map<String, Object> result = parseCustomJsonBlock(block);
                if (!result.isEmpty()) {
                    System.out.println("Parsed: " + result);
                }
            }
        }
    }

    /**
     * Checks whether the block has a balanced number of opening and closing curly braces.
     * This is a heuristic to decide if the JSON-like block is complete.
     */
    private boolean isBlockComplete(String block) {
        int openBraceCount = countOccurrences(block, '{');
        int closeBraceCount = countOccurrences(block, '}');
        return openBraceCount > 0 && openBraceCount == closeBraceCount;
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    /**
     * Parses a block of text for custom key/value pairs using the regex pattern.
     */
    private Map<String, Object> parseCustomJsonBlock(String block) {
        Map<String, Object> parsed = new LinkedHashMap<>();
        Matcher matcher = CHUNK_PATTERN.matcher(block);
        while (matcher.find()) {
            String outerKey = matcher.group(1);   // e.g. "1", "3", "451"
            String innerValue = matcher.group(2);   // e.g. "F21BPAHKH0AOXX7750S132" or "177:2501071854"
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