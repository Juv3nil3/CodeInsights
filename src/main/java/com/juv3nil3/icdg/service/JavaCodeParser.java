package com.juv3nil3.icdg.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.juv3nil3.icdg.domain.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JavaCodeParser {

    private static final Logger logger = LoggerFactory.getLogger(JavaCodeParser.class);

    /**
     * Parses a Java file and extracts class, method, annotation, and comment details.
     *
     * @param inputStream The Java file to parse.
     * @return A ParsedFile object containing structured data.
     * @throws Exception if the file cannot be parsed.
     */
    public FileData parseJavaFile(InputStream inputStream) throws Exception {
        logger.info("Starting Java file parsing...");

        // Initialize FileData object
        FileData fileData = new FileData();

        try {
            // Parse the Java file content using JavaParser
            CompilationUnit compilationUnit = parseCompilationUnit(inputStream);
            logger.debug("Parsed CompilationUnit successfully.");

            // Extract class data and populate FileData
            extractClassData(compilationUnit, fileData);
            logger.info("Finished extracting class data.");

        } catch (Exception e) {
            logger.error("Error during Java file parsing: {}", e.getMessage(), e);
            throw e;
        }

        logger.info("Java file parsing completed.");
        return fileData;
    }

    /**
     * Parses the input stream into a CompilationUnit.
     *
     * @param inputStream Input stream containing Java source code.
     * @return CompilationUnit representing the parsed Java source.
     * @throws Exception If parsing fails.
     */
    private CompilationUnit parseCompilationUnit(InputStream inputStream) throws Exception {
        logger.debug("Parsing input stream into CompilationUnit...");
        JavaParser parser = new JavaParser();

        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        logger.debug("Java file content read successfully.");

        ParseResult<CompilationUnit> parseResult = parser.parse(content);
        if (!parseResult.isSuccessful()) {
            logger.warn("ParseResult contains errors: {}", parseResult.getProblems());
        }

        return parseResult.getResult()
            .orElseThrow(() -> new IllegalArgumentException("Unable to parse the provided Java content"));
    }

    /**
     * Extracts class data from a CompilationUnit and populates FileData.
     *
     * @param compilationUnit The parsed CompilationUnit.
     * @param fileData        The FileData object to populate.
     */
    private void extractClassData(CompilationUnit compilationUnit, FileData fileData) {
        if (fileData == null) {
            throw new IllegalArgumentException("FileData cannot be null");
        }

        logger.debug("Extracting class data from CompilationUnit...");

        compilationUnit
            .findAll(ClassOrInterfaceDeclaration.class)
            .forEach(clazz -> {
                try {
                    ClassData classData = new ClassData();

                    // Set class name and annotations
                    classData.setName(clazz.getNameAsString());
                    classData.setAnnotations(extractAnnotations(clazz));
                    logger.debug("Extracted class: {} with annotations: {}", classData.getName(), classData.getAnnotations());

                    // Set class comment (if available)
                    clazz.getComment().ifPresent(comment -> {
                        classData.setComment(comment.getContent());
                        logger.debug("Class comment: {}", comment.getContent());
                    });

                    // Extract fields
                    clazz.getFields().forEach(field -> {
                        FieldData fieldData = extractFieldData(field);
                        if (fieldData != null) {
                            logger.debug("Extracted field: {}", fieldData);
                            classData.getFields().add(fieldData);
                        } else {
                            logger.warn("Skipped null field for class: {}", classData.getName());
                        }
                    });

                    // Extract methods
                    clazz.getMethods().forEach(method -> {
                        MethodData methodData = extractMethodData(method);
                        if (methodData != null) {
                            logger.debug("Extracted method: {}", methodData);
                            classData.getMethods().add(methodData);
                        } else {
                            logger.warn("Skipped null method for class: {}", classData.getName());
                        }
                    });

                    // Check if the class has any fields or methods
                    if (classData.getFields().isEmpty() && classData.getMethods().isEmpty()) {
                        logger.warn("Class {} has no fields or methods. Skipping addition to fileData.", classData.getName());
                        return; // Skip this class
                    }

                    // Set the fileData reference in the classData
                    classData.setFileData(fileData); // Important: associate the class with the file

                    // Avoid duplicate classes in fileData
                    if (fileData.getClasses().stream().noneMatch(existingClass -> existingClass.getName().equals(classData.getName()))) {
                        fileData.getClasses().add(classData);
                    } else {
                        logger.warn("Duplicate class {} found. Skipping addition to fileData.", classData.getName());
                    }

                } catch (Exception e) {
                    logger.error("Error extracting class data for class in file: {}", fileData.getFilePath(), e);
                }
            });

        logger.debug("Completed extracting class data for file: {}", fileData.getFilePath());
    }



    /**
     * Extracts annotations from a node.
     *
     * @param node The node to extract annotations from.
     * @return A list of annotation names.
     */
    private List<String> extractAnnotations(NodeWithAnnotations<?> node) {
        List<String> annotations = node.getAnnotations().stream()
            .map(annotation -> annotation.getNameAsString())
            .collect(Collectors.toList());
        logger.debug("Extracted annotations: {}", annotations);
        return annotations;
    }

    /**
     * Extracts data for a field, including annotations and comments.
     *
     * @param field The field to extract data from.
     * @return A FieldData object containing field information.
     */
    private FieldData extractFieldData(FieldDeclaration field) {
        FieldData fieldData = new FieldData();

        // Extract field name
        field.getVariables().stream().findFirst().ifPresent(variable -> {
            fieldData.setName(variable.getNameAsString());
            logger.debug("Field name: {}", fieldData.getName());
        });

        // Extract annotations and comments
        fieldData.setAnnotations(extractAnnotations(field));
        field.getComment().ifPresent(comment -> {
            fieldData.setComment(comment.getContent());
            logger.debug("Field comment: {}", comment.getContent());
        });

        return fieldData;
    }

    /**
     * Extracts data for a method, including annotations, comments, and name.
     *
     * @param method The method to extract data from.
     * @return A MethodData object containing method information.
     */
    private MethodData extractMethodData(MethodDeclaration method) {
        MethodData methodData = new MethodData();

        // Extract method name, annotations, and comments
        methodData.setName(method.getNameAsString());
        logger.debug("Method name: {}", methodData.getName());

        methodData.setAnnotations(extractAnnotations(method));
        method.getComment().ifPresent(comment -> {
            methodData.setComment(comment.getContent());
            logger.debug("Method comment: {}", comment.getContent());
        });

        return methodData;
    }
}
