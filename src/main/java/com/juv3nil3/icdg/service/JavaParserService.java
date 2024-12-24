package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JavaParserService {

    private final FileDataRepository fileDataRepository;

    private final ClassDataRepository classDataRepository;

    private final MethodDataRepository methodDataRepository;

    private final PackageDataRepository packageDataRepository;

    private final FieldDataRepository fieldDataRepository;

    private final JavaCodeParser javaCodeParser;

    private static final Logger logger = LoggerFactory.getLogger(JavaParserService.class);

    @Autowired
    public JavaParserService(
        FileDataRepository fileDataRepository,
        ClassDataRepository classDataRepository,
        MethodDataRepository methodDataRepository,
        PackageDataRepository packageDataRepository,
        FieldDataRepository fieldDataRepository
    ) {
        this.fileDataRepository = fileDataRepository;
        this.classDataRepository = classDataRepository;
        this.methodDataRepository = methodDataRepository;
        this.packageDataRepository = packageDataRepository;
        this.fieldDataRepository = fieldDataRepository;
        this.javaCodeParser = new JavaCodeParser();
    }

    public void parseAndSaveFileFromContent(String repoName, String filePath, String content) throws Exception {
        // Extract package name from the file content
        String packageName = extractPackageName(content);

        // Fetch or create the package in the database
        PackageData packageData = getOrCreatePackage(repoName, packageName);

        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            // Parse the Java file content
            FileData fileData = javaCodeParser.parseJavaFile(inputStream);

            // Populate file metadata
            fileData.setRepoName(repoName);
            fileData.setFilePath(filePath);
            fileData.setPackageData(packageData);

            // Save the FileData and related entities
            saveFileData(fileData);
        }
    }

    /**
     * Saves the parsed FileData, including its associated classes, fields, and methods.
     *
     * @param fileData The parsed FileData to save.
     */
    private void saveFileData(FileData fileData) {
        // Log the entire fileData object
        logger.info("Saving FileData: {}", fileData);

        FileData savedFileData = fileDataRepository.save(fileData);

        // Iterate over associated ClassData objects
        for (ClassData parsedClass : fileData.getClasses()) {
            // Ensure the association with FileData is set before saving
            parsedClass.setFileData(savedFileData);

            // Log the classData before saving
            logger.info("Saving ClassData: {}", parsedClass);

            // Save ClassData with the file association
            ClassData savedClassData = classDataRepository.save(parsedClass);

            // Save associated fields and methods for the ClassData
            saveFieldData(parsedClass.getFields(), savedClassData);
            saveMethodData(parsedClass.getMethods(), savedClassData);
        }
    }

    /**
     * Saves ClassData to the database.
     *
     * @param classData   The ClassData to save.
     * @param fileData    The associated FileData.
     * @return The saved ClassData entity.
     */
    private ClassData saveClassData(ClassData classData, FileData fileData) {
        ClassData newClassData = new ClassData(classData.getName(), classData.getAnnotations(), classData.getComment(), fileData);
        return classDataRepository.save(newClassData);
    }

    /**
     * Saves FieldData to the database.
     *
     * @param fields      The list of FieldData to save.
     * @param classData   The associated ClassData.
     */
    private void saveFieldData(List<FieldData> fields, ClassData classData) {
        for (FieldData field : fields) {
            FieldData newFieldData = new FieldData(field.getName(), field.getAnnotations(), field.getComment(), classData);
            fieldDataRepository.save(newFieldData);
        }
    }

    /**
     * Saves MethodData to the database.
     *
     * @param methods     The list of MethodData to save.
     * @param classData   The associated ClassData.
     */
    private void saveMethodData(List<MethodData> methods, ClassData classData) {
        for (MethodData method : methods) {
            MethodData newMethodData = new MethodData(method.getName(), method.getAnnotations(), method.getComment(), classData);
            methodDataRepository.save(newMethodData);
        }
    }

    /**
     * Extracts the package name from the provided content.
     *
     * @param content The content of the Java file.
     * @return The extracted package name, or "default" if none found.
     */
    private String extractPackageName(String content) {
        // Validate content
        if (content == null || content.isBlank()) {
            return "default"; // Default package if content is empty or null
        }

        // Use regex to robustly extract the package declaration
        Pattern packagePattern = Pattern.compile("^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;\\s*$", Pattern.MULTILINE);
        Matcher matcher = packagePattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim(); // Extract and trim the package name
        }

        return "default"; // Default package if no package declaration found
    }

    /**
     * Retrieves or creates a PackageData entity in the database.
     *
     * @param repoName    The name of the repository.
     * @param packageName The name of the package.
     * @return The existing or newly created PackageData.
     */
    private synchronized PackageData getOrCreatePackage(String repoName, String packageName) {
        // Validate input parameters
        if (repoName == null || repoName.isBlank()) {
            throw new IllegalArgumentException("Repository name cannot be null or blank");
        }

        // Avoid reassigning the input parameter, use a new variable instead
        String effectivePackageName = (packageName == null || packageName.isBlank()) ? "default" : packageName;

        // Check for existing package scoped by repoName
        PackageData existingPackage = packageDataRepository.findByRepoNameAndPackageName(repoName, effectivePackageName);
        if (existingPackage != null) {
            return existingPackage;
        }

        // Extract the parent package name if available
        String parentPackageName = extractParentPackage(effectivePackageName);

        // Create the parent package if it exists and set the parent package
        PackageData parentPackage = null;
        if (parentPackageName != null) {
            parentPackage = getOrCreatePackage(repoName, parentPackageName); // Call recursively to get or create parent package
        }

        // Create and save the current package
        PackageData newPackage = new PackageData();
        newPackage.setRepoName(repoName);
        newPackage.setPackageName(effectivePackageName);

        // Set the parent package if available
        if (parentPackage != null) {
            newPackage.setParentPackage(parentPackage);
        }

        return packageDataRepository.save(newPackage);
    }

    private String extractParentPackage(String packageName) {
        // Split the package name by '.'
        String[] packageParts = packageName.split("\\.");

        // If the package has more than one part, return the parent (all but the last part)
        if (packageParts.length > 1) {
            StringBuilder parentPackage = new StringBuilder();
            for (int i = 0; i < packageParts.length - 1; i++) {
                if (i > 0) {
                    parentPackage.append(".");
                }
                parentPackage.append(packageParts[i]);
            }
            return parentPackage.toString();
        }

        // If there's no parent (it's a top-level package), return null or "default"
        return null; // or "default" if you want to return a default package name
    }
}
