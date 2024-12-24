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
        // Initialize FileData object
        FileData fileData = new FileData();

        // Parse the Java file content using JavaParser
        CompilationUnit compilationUnit = parseCompilationUnit(inputStream);

        // Extract class data and populate FileData
        extractClassData(compilationUnit, fileData);

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
        JavaParser parser = new JavaParser();
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        ParseResult<CompilationUnit> parseResult = parser.parse(content);

        return parseResult.getResult().orElseThrow(() -> new IllegalArgumentException("Unable to parse the provided Java content"));
    }

    /**
     * Extracts class data from a CompilationUnit and populates FileData.
     *
     * @param compilationUnit The parsed CompilationUnit.
     * @param fileData        The FileData object to populate.
     */
    private void extractClassData(CompilationUnit compilationUnit, FileData fileData) {
        compilationUnit
            .findAll(ClassOrInterfaceDeclaration.class)
            .forEach(clazz -> {
                ClassData classData = new ClassData();

                // Set class name and annotations
                classData.setName(clazz.getNameAsString());
                classData.setAnnotations(extractAnnotations(clazz));

                // Set class comment (if available)
                clazz.getComment().ifPresent(comment -> classData.setComment(comment.getContent()));

                // Extract fields and methods
                clazz.getFields().forEach(field -> classData.getFields().add(extractFieldData(field)));
                clazz.getMethods().forEach(method -> classData.getMethods().add(extractMethodData(method)));

                fileData.getClasses().add(classData);
            });
    }

    /**
     * Extracts annotations from a node.
     *
     * @param node The node to extract annotations from.
     * @return A list of annotation names.
     */
    private List<String> extractAnnotations(NodeWithAnnotations<?> node) {
        return node.getAnnotations().stream().map(annotation -> annotation.getNameAsString()).collect(Collectors.toList());
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
        field.getVariables().stream().findFirst().ifPresent(variable -> fieldData.setName(variable.getNameAsString()));

        // Extract annotations and comments
        fieldData.setAnnotations(extractAnnotations(field));
        field.getComment().ifPresent(comment -> fieldData.setComment(comment.getContent()));

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
        methodData.setAnnotations(extractAnnotations(method));
        method.getComment().ifPresent(comment -> methodData.setComment(comment.getContent()));

        return methodData;
    }
}
