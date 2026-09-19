package com.shiyu.dto.request;

import java.util.List;

public class SyncRequest {
    private String deviceId;
    private Long coupleId;
    private Long lastSyncTime;
    private List<RecipeSyncData> recipes;
    private List<OrderSyncData> orders;
    private List<GallerySyncData> gallery;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getCoupleId() {
        return coupleId;
    }

    public void setCoupleId(Long coupleId) {
        this.coupleId = coupleId;
    }

    public Long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public List<RecipeSyncData> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<RecipeSyncData> recipes) {
        this.recipes = recipes;
    }

    public List<OrderSyncData> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderSyncData> orders) {
        this.orders = orders;
    }

    public List<GallerySyncData> getGallery() {
        return gallery;
    }

    public void setGallery(List<GallerySyncData> gallery) {
        this.gallery = gallery;
    }

    public static class RecipeSyncData {
        private Long id;
        private String syncId;
        private Long syncTime;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSyncId() {
            return syncId;
        }

        public void setSyncId(String syncId) {
            this.syncId = syncId;
        }

        public Long getSyncTime() {
            return syncTime;
        }

        public void setSyncTime(Long syncTime) {
            this.syncTime = syncTime;
        }
    }

    public static class OrderSyncData {
        private Long id;
        private String syncId;
        private Long syncTime;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSyncId() {
            return syncId;
        }

        public void setSyncId(String syncId) {
            this.syncId = syncId;
        }

        public Long getSyncTime() {
            return syncTime;
        }

        public void setSyncTime(Long syncTime) {
            this.syncTime = syncTime;
        }
    }

    public static class GallerySyncData {
        private Long id;
        private String syncId;
        private Long syncTime;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSyncId() {
            return syncId;
        }

        public void setSyncId(String syncId) {
            this.syncId = syncId;
        }

        public Long getSyncTime() {
            return syncTime;
        }

        public void setSyncTime(Long syncTime) {
            this.syncTime = syncTime;
        }
    }
}