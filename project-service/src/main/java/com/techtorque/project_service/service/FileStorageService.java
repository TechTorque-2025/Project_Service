package com.techtorque.project_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    String storeFile(MultipartFile file, String serviceId);
    List<String> storeFiles(MultipartFile[] files, String serviceId);
    void deleteFile(String fileUrl);
}
