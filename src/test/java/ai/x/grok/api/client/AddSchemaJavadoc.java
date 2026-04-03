package ai.x.grok.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This utility is a one-off to apply schema documentation to the generated
 * DTOs.
 *
 * @author Key Bridge
 */
public class AddSchemaJavadoc {

  private final Logger LOG = Logger.getLogger(AddSchemaJavadoc.class.getName());

  public void addFieldJavadocFromSchema(Path javaFile) throws Exception {

    String code = Files.readString(javaFile);
    CompilationUnit cu = StaticJavaParser.parse(code);

    String className = extractClassName(javaFile);

    JsonNode classNode = schemas.get(className);
    if (classNode == null) {
      LOG.info("Schema doc skip for unknown class " + javaFile);
      return;
    } else if (classNode.get("enum") != null) {
      LOG.info("Schema doc skip for enum " + javaFile);
      return;
    }

    LOG.info("Schema doc for " + javaFile);
    // read the schema
    Map<String, String> jsonNameToDescription = getDescriptions(className);
    System.out.println("  apply " + className + " " + jsonNameToDescription);

    cu.findAll(FieldDeclaration.class).forEach(field -> {
      for (VariableDeclarator var : field.getVariables()) {
        String javaFieldName = var.getNameAsString();
        String lookupName = javaFieldName;

        // Try to read @JsonProperty value
        Optional<AnnotationExpr> jsonPropAnnoOpt = field.getAnnotationByName("JsonProperty");
        if (jsonPropAnnoOpt.isPresent()) {
          AnnotationExpr anno = jsonPropAnnoOpt.get();

          if (anno instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) anno;
            Expression value = sma.getMemberValue();
            if (value instanceof StringLiteralExpr) {
              lookupName = ((StringLiteralExpr) value).asString();
            }
          } // Handle the more verbose @JsonProperty(value = "…") form
          else if (anno instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr na = (NormalAnnotationExpr) anno;
            for (MemberValuePair pair : na.getPairs()) {
              if ("value".equals(pair.getNameAsString()) || "name".equals(pair.getNameAsString())) {
                Expression expr = pair.getValue();
                if (expr instanceof StringLiteralExpr) {
                  lookupName = ((StringLiteralExpr) expr).asString();
                  break;
                }
              }
            }
          }
        }

        String desc = jsonNameToDescription.get(lookupName);
        if (desc == null || desc.trim().isEmpty()) {
          return;
        }

        // Skip if already has Javadoc
        if (field.getJavadoc().isPresent()) {
          return;
        }

        String cleanedDesc = desc.trim().replace("\n", "\n * ");
        JavadocComment comment = new JavadocComment(" * " + cleanedDesc + "\n");
        field.setJavadocComment(comment);
      }
    });

    // Overwrite original file (or write to new path)
    Files.writeString(javaFile, cu.toString());
  }

  public void addJavadoc(Path javaFile) throws Exception {

    String code = Files.readString(javaFile);
    CompilationUnit cu = StaticJavaParser.parse(code);

    String className = extractClassName(javaFile);

    JsonNode classNode = schemas.get(className);
    if (classNode == null) {
      LOG.info("Schema doc skip for unknown class " + javaFile);
      return;
    } else if (classNode.get("enum") != null) {
      LOG.info("Schema doc skip for enum " + javaFile);
      return;
    }

    LOG.info("Schema doc for " + javaFile);
    // read the schema
    Map<String, String> jsonNameToDescription = getDescriptions(className);
    System.out.println("  apply " + className + " " + jsonNameToDescription);

    // Process fields (your existing code)
    cu.findAll(FieldDeclaration.class).forEach(field -> {
      for (VariableDeclarator var : field.getVariables()) {
        String javaFieldName = var.getNameAsString();
        String lookupName = getJsonPropertyName(field).orElse(javaFieldName);

        String desc = jsonNameToDescription.get(lookupName);
        if (desc == null || desc.trim().isEmpty()) {
          return;
        }

        if (field.getJavadoc().isPresent()) {
          return; // skip if already documented
        }
        String cleaned = desc.trim().replace("\n", "\n * ");
        field.setJavadocComment(new JavadocComment(" * " + cleaned + "\n"));
      }
    });

    // ───────────────────────────────────────────────
    // NEW: Process getters and setters
    // ───────────────────────────────────────────────
    // ───────────────────────────────────────────────
// Process getters and setters with full @param / @return
// ───────────────────────────────────────────────
    cu.findAll(MethodDeclaration.class).forEach(method -> {
      String methodName = method.getNameAsString();

      if (!isTypicalGetter(method) && !isTypicalSetter(method)) {
        return;
      }

      String fieldName = extractFieldNameFromAccessor(methodName);
      if (fieldName == null) {
        return;
      }

      // Find matching description (prefer json name match, fallback to java field name)
      String lookupName = jsonNameToDescription.keySet().stream()
        .filter(k -> k.equalsIgnoreCase(fieldName) || camelToSnake(k).equalsIgnoreCase(fieldName))
        .findFirst()
        .orElse(fieldName);

      String baseDesc = jsonNameToDescription.get(lookupName);
      if (baseDesc == null || baseDesc.trim().isEmpty()) {
        return;
      }

      if (method.getJavadoc().isPresent()) {
        return; // skip if already documented
      }

      // Build Javadoc
      String cleanedDesc = baseDesc.trim().replace("\n", "\n * ");
//      JavadocComment comment = new JavadocComment(" * " + cleanedDesc + "\n");
//      com.github.javaparser.ast.comments.Javadoc javadoc = new com.github.javaparser.ast.comments.Javadoc();
      if (isTypicalGetter(method)) {
        // Inside method loop (for getter):
//        String cleanedDesc = baseDesc.trim().replace("\n", "\n * ");

        StringBuilder jb = new StringBuilder();
        jb.append(" * Returns ").append(cleanedDesc).append("\n");
        jb.append(" *\n");
        jb.append(" * @return the ").append(fieldName).append("\n");

        JavadocComment comment = new JavadocComment(jb.toString());
        method.setJavadocComment(comment);

//        javadoc.addBlockTag("return", "the " + fieldName);
//        javadoc.setDescription("Returns " + cleanedDesc);
      } else { // setter
        // For setter:
        StringBuilder jb = new StringBuilder();
        jb.append(" * Sets ").append(cleanedDesc).append("\n");
        jb.append(" *\n");
        String paramName = method.getParameter(0).getNameAsString();
        jb.append(" * @param ").append(paramName).append(" the ").append(fieldName).append("\n");

        JavadocComment comment = new JavadocComment(jb.toString());
        method.setJavadocComment(comment);
//        javadoc.addBlockTag("param", method.getParameter(0).getNameAsString(), "the " + fieldName);
//        javadoc.setDescription("Sets " + cleanedDesc);
      }

//      method.setJavadocComment(javadoc);
    });

    Files.writeString(javaFile, cu.toString());
  }

  /**
   * Extracts the string value from @JsonProperty annotation on a field. Returns
   * null if no @JsonProperty is present or value cannot be extracted.
   * <p>
   * Fully compatible with Java 11 (no pattern matching for instanceof).
   */
  private Optional<String> getJsonPropertyName(FieldDeclaration field) {
    Optional<AnnotationExpr> annoOpt = field.getAnnotationByName("JsonProperty");
    if (!annoOpt.isPresent()) {
      return Optional.empty();
    }

    AnnotationExpr anno = annoOpt.get();
    String jsonName = null;

    // Form 1: @JsonProperty("literal")
    if (anno instanceof SingleMemberAnnotationExpr) {
      SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) anno;
      if (sma.getMemberValue() instanceof StringLiteralExpr) {
        jsonName = ((StringLiteralExpr) sma.getMemberValue()).getValue();
      }
    } // Form 2: @JsonProperty(value = "literal") or @JsonProperty(name = "literal")
    else if (anno instanceof NormalAnnotationExpr) {
      NormalAnnotationExpr na = (NormalAnnotationExpr) anno;
      for (var pair : na.getPairs()) {
        String memberName = pair.getNameAsString();
        if ("value".equals(memberName) || "name".equals(memberName)) {
          if (pair.getValue() instanceof StringLiteralExpr) {
            jsonName = ((StringLiteralExpr) pair.getValue()).getValue();
            break;
          }
        }
      }
    }

    return (jsonName != null && !jsonName.isEmpty())
           ? Optional.of(jsonName)
           : Optional.empty();
  }

  private String getJsonPropertyValue(FieldDeclaration field) {
    Optional<AnnotationExpr> annoOpt = field.getAnnotationByName("JsonProperty");
    if (!annoOpt.isPresent()) {
      return null;
    }

    AnnotationExpr anno = annoOpt.get();
    String jsonName = null;

    // Case 1: @JsonProperty("some_name")
    if (anno instanceof SingleMemberAnnotationExpr) {
      SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) anno;
      Expression value = sma.getMemberValue();
      if (value instanceof StringLiteralExpr) {
        jsonName = ((StringLiteralExpr) value).getValue();
      }
    } // Case 2: @JsonProperty(value = "some_name")  or  @JsonProperty(name = "some_name")
    else if (anno instanceof NormalAnnotationExpr) {
      NormalAnnotationExpr na = (NormalAnnotationExpr) anno;
      for (MemberValuePair pair : na.getPairs()) {
        String memberName = pair.getNameAsString();
        if ("value".equals(memberName) || "name".equals(memberName)) {
          Expression expr = pair.getValue();
          if (expr instanceof StringLiteralExpr) {
            jsonName = ((StringLiteralExpr) expr).getValue();
            break;
          }
        }
      }
    }

    return jsonName;
  }

  private boolean isTypicalGetter(MethodDeclaration m) {
    return m.getParameters().isEmpty()
      && !m.getType().isVoidType()
      && m.getNameAsString().startsWith("get")
      && m.getBody().isPresent();
  }

  private boolean isTypicalSetter(MethodDeclaration m) {
    return m.getParameters().size() == 1
      && m.getType().isVoidType()
      && m.getNameAsString().startsWith("set")
      && m.getBody().isPresent();
  }

  private String extractFieldNameFromAccessor(String methodName) {
    if (methodName.startsWith("get") || methodName.startsWith("set")) {
      String base = methodName.substring(3);
      if (base.isEmpty()) {
        return null;
      }
      return Character.toLowerCase(base.charAt(0)) + base.substring(1);
    }
    return null;
  }

  private String camelToSnake(String camel) {
    return camel.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
  }

  /**
   * Extracts the simple class name from a .java file path. Examples:
   * /tmp/foo/Bar.java → "Bar" src/main/java/User.java → "User" Bar.java → "Bar"
   * /project/FooBar.java → "FooBar"
   * <p>
   * Returns null if the path doesn't end with ".java" or has no file name.
   *
   * @param javaFile path to the .java file
   * @return the simple class name, or null if invalid
   */
  public String extractClassName(Path javaFile) {
    if (javaFile == null) {
      return null;
    }

    Path fileNamePath = javaFile.getFileName();
    if (fileNamePath == null) {
      return null;
    }

    String fileName = fileNamePath.toString();
    if (!fileName.endsWith(".java")) {
      return null;
    }

    // Remove the ".java" suffix (5 characters)
    return fileName.substring(0, fileName.length() - 5);
  }

  private final ObjectMapper mapper = new ObjectMapper();
  private JsonNode schemas;

  public void readSchema() throws Exception {
    URL url = this.getClass().getClassLoader().getResource("META-INF/json/schema/openapi.json");
    Path path = Paths.get(url.toURI());
    JsonNode rootNode = mapper.readTree(path.toFile());
    JsonNode components = rootNode.get("components");
    schemas = components.get("schemas");
  }

  private Map<String, String> getDescriptions(String className) throws Exception {
    if (schemas == null) {
      readSchema();
    }

    Map<String, String> nameDescriptionMap = new HashMap<>();
    JsonNode classNode = schemas.get(className);
    JsonNode properties = classNode.get("properties");
    if (properties == null) {
      return nameDescriptionMap; // probably enum
    }
    List<String> propertyNames = new ArrayList<>();
    properties.fieldNames().forEachRemaining(propertyNames::add);
    for (String propertyName : propertyNames) {
      JsonNode property = properties.get(propertyName);
//      nameDescriptionMap.put(propertyName,                             property.get("description").asText());
      nameDescriptionMap.put(propertyName,
                             findFirstDescription(property));
    }
    return nameDescriptionMap;
  }

  /**
   * Recursively searches a JsonNode (schema property) for the first
   * "description" value. Especially useful for oneOf / allOf / anyOf unions
   * where description is attached to the second branch (real type), not the
   * null/type branch.
   * <p>
   * Returns null if no description is found anywhere in the subtree.
   */
  private String findFirstDescription(JsonNode schemaNode) {
    if (schemaNode == null || schemaNode.isMissingNode()) {
      return null;
    }

    // Direct description at this level?
    if (schemaNode.has("description")) {
      String desc = schemaNode.path("description").asText(null);
      if (desc != null && !desc.trim().isEmpty()) {
        return desc;
      }
    }

    // Check oneOf / anyOf / allOf arrays
    String[] unionKeys = {"oneOf", "anyOf", "allOf"};
    for (String key : unionKeys) {
      JsonNode unionNode = schemaNode.path(key);
      if (unionNode.isArray()) {
        for (JsonNode item : unionNode) {
          String desc = findFirstDescription(item);
          if (desc != null) {
            return desc;
          }
        }
      }
    }

    // Recurse into object fields (fallback / future-proof)
    if (schemaNode.isObject()) {
      for (JsonNode child : schemaNode) {
        String desc = findFirstDescription(child);
        if (desc != null) {
          return desc;
        }
      }
    }

    // Arrays of items, etc. (rare in property schemas but harmless)
    if (schemaNode.isArray()) {
      for (JsonNode item : schemaNode) {
        String desc = findFirstDescription(item);
        if (desc != null) {
          return desc;
        }
      }
    }

    return null;
  }

  public List<Path> findAllJavaFiles(Path startDir) throws IOException {
    try (var stream = Files.walk(startDir)) {
      return stream
        .filter(Files::isRegularFile)
        .filter(p -> p.getFileName().toString().endsWith(".java"))
        .map(Path::toAbsolutePath)
        .collect(Collectors.toList());
    }
  }

  public void main(String[] args) throws Exception {
    AddSchemaJavadoc add = new AddSchemaJavadoc();
    add.readSchema();

    Path rootDir = Paths.get("~/Workspace/lib/ai/lib-xai-api/src/main/java");
    List<Path> candidateFiles = add.findAllJavaFiles(rootDir);

    int iterator = 0;

    for (Path candidateFile : candidateFiles) {
      LOG.info("TRY " + candidateFile);
//      add.addFieldJavadocFromSchema(candidateFile);
      add.addJavadoc(candidateFile);
      iterator++;
//      if (iterator > 10) {        break;      }
    }

    LOG.info("Done");

  }

}
