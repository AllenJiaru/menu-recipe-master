package com.shiyu.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadFile(MultipartFile file, String type);
    boolean deleteFile(String filename);
    String getFilePath(String filename);
}
