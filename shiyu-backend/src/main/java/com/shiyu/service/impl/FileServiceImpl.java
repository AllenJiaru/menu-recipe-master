package com.shiyu.service.impl;

import com.shiyu.common.BusinessException;
import com.shiyu.config.FileUploadConfig;
import com.shiyu.service.FileService;
import com.shiyu.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Override
    public String uploadFile(MultipartFile file, String type) {
        try {
            String subDir = type != null ? type : "common";
            return fileUtil.uploadFile(file, subDir);
        } catch (Exception e) {
            throw new BusinessException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String filename) {
        return fileUtil.deleteFile(filename);
    }

    @Override
    public String getFilePath(String filename) {
        String relative = filename.replaceFirst("^/uploads/", "").replaceFirst("^uploads/", "");
        return fileUploadConfig.getUploadDir() + "/" + relative;
    }
}
