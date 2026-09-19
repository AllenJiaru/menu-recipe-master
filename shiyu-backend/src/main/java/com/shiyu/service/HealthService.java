package com.shiyu.service;

import java.util.Map;

public interface HealthService {
    Map<String, Object> getSystemHealth();
    Map<String, Object> getDatabaseHealth();
    Map<String, Object> getDiskHealth();
    Map<String, Object> getCacheHealth();
    void clearCache();
    void optimizeDatabase();
    Map<String, Object> getBackupList();
    void createBackup();
    void restoreBackup(String backupName);
}
