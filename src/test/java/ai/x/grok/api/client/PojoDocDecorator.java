package ai.x.grok.api.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for decorating POJO fields with Javadoc comments based on
 * schema descriptions. Now supports fields annotated with
 *
 * @JsonProperty("json_name").
 */
public class PojoDocDecorator {

  private static final Pattern JSON_PROPERTY_PATTERN = Pattern.compile(
    "@JsonProperty\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");

  /**
   * Reads the Java source file line-by-line, detects field declarations (with
   * or without @JsonProperty), and inserts Javadoc using the schema-provided
   * description — matched by either the Java field name or the @JsonProperty
   * value.
   *
   * @param inputPath             path to original .java file
   * @param outputPath            where to write the decorated version
   * @param jsonNameToDescription map: JSON property name → description (from
   *                              OpenAPI schema)
   * @throws IOException on file I/O error
   */
  public static void decorateFields(Path inputPath, Path outputPath, Map<String, String> jsonNameToDescription) throws IOException {

    List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);

    List<String> newLines = new ArrayList<>();
    String previousNonEmptyLine = null;
    boolean inClass = false;

    for (String line : lines) {
      String trimmed = line.trim();

      if (trimmed.isEmpty()) {
        newLines.add(line);
        continue;
      }

      // Detect class start (very basic heuristic)
      if (!inClass && (trimmed.startsWith("public class ")
        || trimmed.startsWith("class ")
        || trimmed.startsWith("public abstract class ")
        || trimmed.startsWith("abstract class "))) {
        inClass = true;
      }

      if (!inClass) {
        newLines.add(line);
        previousNonEmptyLine = trimmed;
        continue;
      }

      // Check if this line looks like a field declaration
      if (isLikelyFieldDeclaration(trimmed)) {
        String jsonName = extractJsonPropertyName(previousNonEmptyLine);
        String javaFieldName = extractJavaFieldName(trimmed);

        String lookupKey = (jsonName != null) ? jsonName : javaFieldName;

        if (lookupKey != null && jsonNameToDescription.containsKey(lookupKey)) {
          String description = jsonNameToDescription.get(lookupKey);
          String indent = getIndent(line);
          newLines.addAll(generateJavadocLines(description, indent));
        }
      }

      newLines.add(line);
      previousNonEmptyLine = trimmed;
    }

    Files.write(outputPath, newLines, StandardCharsets.UTF_8);
  }

  private static boolean isLikelyFieldDeclaration(String trimmed) {
    return trimmed.endsWith(";")
      && !trimmed.contains("(")
      && // not a method
      !trimmed.startsWith("import ")
      && !trimmed.startsWith("package ")
      && !trimmed.startsWith("@")
      && // this line itself is not an annotation
      trimmed.matches(".*\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*(=.*)?;");
  }

  /**
   * Tries to extract the string inside @JsonProperty("...")
   */
  private static String extractJsonPropertyName(String previousLine) {
    if (previousLine == null) {
      return null;
    }
    Matcher matcher = JSON_PROPERTY_PATTERN.matcher(previousLine);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  /**
   * Extracts the Java field name from a field declaration line
   */
  private static String extractJavaFieldName(String trimmed) {
    String withoutSemi = trimmed.substring(0, trimmed.length() - 1).trim();

    // Remove initializer if present
    int eqIndex = withoutSemi.lastIndexOf('=');
    if (eqIndex != -1) {
      withoutSemi = withoutSemi.substring(0, eqIndex).trim();
    }

    // Last "word" before optional = or ;
    String[] parts = withoutSemi.split("\\s+");
    if (parts.length < 2) {
      return null;
    }

    String candidate = parts[parts.length - 1];
    if (candidate.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
      return candidate;
    }
    return null;
  }

  private static String getIndent(String line) {
    int i = 0;
    while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
      i++;
    }
    return line.substring(0, i);
  }

  private static List<String> generateJavadocLines(String description, String indent) {
    List<String> javadoc = new ArrayList<>();
    javadoc.add(indent + "/**");
    javadoc.add(indent + " * " + description.trim());
    javadoc.add(indent + " */");
    return javadoc;
  }

  // ───────────────────────────────────────────────
  // Example usage
  // ───────────────────────────────────────────────
  public static void main(String[] args) throws IOException {
    Path input = Path.of("src/main/java/com/example/ChatCompletionRequest.java");
    Path output = Path.of("target/ChatCompletionRequest.decorated.java");

    // This map should come from your OpenAPI schema parsing
    Map<String, String> descriptions = Map.of(
      "top_logprobs", "Number of top logprobs to return (0–8)",
      "temperature", "Sampling temperature (0.0–2.0)",
      "model", "ID of the model to use"
    );

    decorateFields(input, output, descriptions);
    System.out.println("Decorated file written to: " + output);
  }
}
