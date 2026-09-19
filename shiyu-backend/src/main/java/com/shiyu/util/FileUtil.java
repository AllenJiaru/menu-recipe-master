package com.shiyu.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.shiyu.config.FileUploadConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class FileUtil {
    @Autowired
    private FileUploadConfig fileUploadConfig;

    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        
        String relativePath = subDir + "/" + datePath + "/" + newFilename;
        Path targetPath = Paths.get(fileUploadConfig.getUploadDir()).resolve(relativePath).toAbsolutePath();
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        return "/uploads/" + relativePath;
    }

    public String generateThumbnail(String filePath) {
        return filePath;
    }

    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;
        try {
            Path path = Paths.get(fileUploadConfig.getUploadDir()).resolve(filePath.replaceFirst("^/uploads/", ""));
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    public String getExtension(String filename) {
        if (filename == null) return "jpg";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "jpg";
    }
}
