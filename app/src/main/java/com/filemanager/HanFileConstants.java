package com.filemanager;

import android.os.Environment;

public class HanFileConstants {

    private HanFileConstants() { /* do not use this constructor */ }

    // Root path
    public static final String PATH_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

    // show or hide folder path
    public static final String PATH_ANDROID_FOLDER = PATH_ROOT + "/Android";                    // 시스템 폴더 경로1
    public static final String PATH_DATABASE_FOLDER = PATH_ROOT + "/Database";                  // 시스템 폴더 경로2
    public static final String PATH_DAISY_FOLDER = PATH_ROOT + "/Online DAISY";                 // 데이지 폴더 경로
    public static final String PATH_ALARMS_FOLDER = PATH_ROOT + "/Alarms";                      // Alarms 폴더 경로
    public static final String PATH_NOTIFICATIONS_FOLDER = PATH_ROOT + "/Notifications";        // Notifications 폴더 경로
    public static final String PATH_RINGTONES_FOLDER = PATH_ROOT + "/Ringtones";                // Ringtones 폴더 경로
    public static final String PATH_DCIM_FOLDER = PATH_ROOT + "/DCIM";                          // DCIM 폴더 경로
    public static final String PATH_MOVIES_FOLDER = PATH_ROOT + "/Movies";                      // Movies 폴더 경로
    public static final String PATH_PICTURES_FOLDER = PATH_ROOT + "/Pictures";                  // Pictures 폴더 경로
    public static final String PATH_PODCASTS_FOLDER = PATH_ROOT + "/Podcasts";                  // Podcasts 폴더 경로
    // [18.02.04][jangkh] 아래 Folder program 에서 자체 생성하는 Folder 로 사용자에게 보여지지 않도록 수정
    public static final String PATH_DOCUMENT_MARK_FOLDER = PATH_ROOT + "/Documents/mark";       // Documents/mark 폴더 경로
    public static final String PATH_DAISY_MEMO_FOLDER = PATH_ROOT + "/Daisy/MEMO";              // Daisy/MEMO 폴더 경로
}