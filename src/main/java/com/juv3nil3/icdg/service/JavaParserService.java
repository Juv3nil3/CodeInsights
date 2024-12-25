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
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
        } catch (Exception e) {
            logger.error("Error parsing and saving file: repoName={}, filePath={}", repoName, filePath, e);
            throw e;
        }
    }

    /**
     * Saves the parsed FileData, including its associated classes, fields, and methods.
     *
     * @param fileData The parsed FileData to save.
     */
    private void saveFileData(FileData fileData) {
        logger.info("Saving FileData: {}", fileData);

        FileData savedFileData = fileDataRepository.save(fileData);
        logger.info("Filedata saved: {}", savedFileData.getId());

        for (ClassData parsedClass : fileData.getClasses()) {
            // Ensure the association with FileData is set before saving
            parsedClass.setFileData(savedFileData);
            logger.info("Saving ClassData: {}", parsedClass);

            ClassData savedClassData = classDataRepository.save(parsedClass);
            logger.info("ClassData saved: {}", savedClassData.getId());

            // Check if ClassData ID is set properly
            if (savedClassData.getId() == null) {
                logger.error("ClassData ID is null for class: {}", parsedClass.getName());
                // Handle the situation where the class ID is not generated
            }

            saveMethodData(parsedClass.getMethods(),savedClassData);
            saveFieldData(parsedClass.getFields(),savedClassData);
        }
    }


    /**
     * Saves FieldData to the database.
     *
     * @param fields      The list of FieldData to save.
     * @param classData   The associated ClassData.
     */
    private void saveFieldData(List<FieldData> fields, ClassData classData) {
        for (FieldData field : fields) {
            // Ensure classData is set for each FieldData
            field.setClassData(classData);
            logger.debug("Saving FieldData: {} for ClassData ID: {}", field.getName(), classData.getId());
            fieldDataRepository.save(field);
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
            // Ensure classData is set for each MethodData
            method.setClassData(classData);
            logger.debug("Saving MethodData: {} for ClassData ID: {}", method.getName(), classData.getId());
            methodDataRepository.save(method);
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
