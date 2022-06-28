package com.filemanager.utils;

import com.filemanager.HanFileConstants;

import java.io.File;
import java.util.ArrayList;

public class HanFileJNI {
    static String[] SYSTEM_FOLDERS;

    static {
        final ArrayList<String> folders = new ArrayList<>();
        folders.add(HanFileConstants.PATH_ANDROID_FOLDER);
        folders.add(HanFileConstants.PATH_ALARMS_FOLDER);
        folders.add(HanFileConstants.PATH_NOTIFICATIONS_FOLDER);
        folders.add(HanFileConstants.PATH_RINGTONES_FOLDER);
        folders.add(HanFileConstants.PATH_DCIM_FOLDER);
        folders.add(HanFileConstants.PATH_MOVIES_FOLDER);
        folders.add(HanFileConstants.PATH_PICTURES_FOLDER);
        folders.add(HanFileConstants.PATH_DOCUMENT_MARK_FOLDER);
        folders.add(HanFileConstants.PATH_DAISY_MEMO_FOLDER);
        SYSTEM_FOLDERS = new String[folders.size()];
        folders.toArray(SYSTEM_FOLDERS);

        System.loadLibrary("HanFileJNI");
    }

    private HanFileJNI() {
        throw new IllegalStateException("HanFileJNI class");
    }

    public static boolean isSystemFolder(File checkFile) {
        if (checkFile.getName().charAt(0) == '.') return true;

        String path = checkFile.getAbsolutePath();
        int i;
        int len = SYSTEM_FOLDERS.length;
        for (i = 0; i < len; i++) {
            if (path.equalsIgnoreCase(SYSTEM_FOLDERS[i])) return true;
        }

        return false;
    }

    public static final int SORT_NONE = 0;
    public static final int SORT_BY_NAME = 1;
    public static final int SORT_BY_EXT = 2;
    public static final int SORT_BY_SIZE = 3;
    public static final int SORT_BY_MTIME = 4;

    public static File[] getDirList(String dir, boolean showSystemFolder) {
        return nativeGetDirList(dir, showSystemFolder);
    }

    public static File[] getFileList(String dir, int sortOrder, boolean showSystemFolder, String[] exts) {
        return nativeGetFileList(dir, sortOrder, showSystemFolder, exts);
    }

    public static File[] getFileListWithoutDir(String dir, int sortOrder, boolean showHiddenFile, String[] exts) {
        return nativeGetFileListWithoutDir(dir, sortOrder, showHiddenFile, exts);
    }

    private static native File[] nativeGetDirList(String dir, boolean showSystemFolder);

    private static native File[] nativeGetFileList(String dir, int sortOrder, boolean showSystemFolder, String[] exts);

    private static native File[] nativeGetFileListWithoutDir(String dir, int sortOrder, boolean showHiddenFile, String[] exts);
}