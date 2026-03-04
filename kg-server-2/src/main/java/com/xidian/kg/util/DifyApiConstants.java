package com.xidian.kg.util;

/**
 * Shared Dify API endpoint constants to keep base URLs centralized.
 */
public final class DifyApiConstants {

    private DifyApiConstants() {
        // Utility class
    }

    public static final String DIFY_API_BASE_URL = "http://39.106.248.163:8081/v1";
    public static final String FILES_UPLOAD_URL = DIFY_API_BASE_URL + "/files/upload";
    public static final String WORKFLOWS_RUN_URL = DIFY_API_BASE_URL + "/workflows/run";
    public static final String DATASETS_BASE_URL = DIFY_API_BASE_URL + "/datasets";
}
