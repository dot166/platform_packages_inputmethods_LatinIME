package com.google.android.apps.inputmethod.libs.mozc.session;

public final class MozcJNI {

    static {
        System.loadLibrary("mozc");
    }

    private MozcJNI() {
        // Prevent instantiation
    }

    public static native boolean initialize();

    public static native byte[] evalCommand(byte[] commandBytes);

    public static native boolean onPostLoad(
            String userProfileDirectoryPath,
            String dataFilePath);

    public static native String getDataVersion();
}