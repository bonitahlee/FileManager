package com.bonita.filemanager.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.bonita.filemanager.constants.FileManagerConstants;
import com.bonita.filemanager.data.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Storage util
 * [TODO] updateDevices 를 매번 호출하는 방식에서 broadcast 로 처리하도록 수정 필요
 *
 * @author bonita
 * @date 2021-11-19
 */
public class StorageUtil {

    private static final String NO_SIZE = "None ";
    private static final ArrayList<Storage> sStorageList = new ArrayList<>();

    private StorageUtil() {
        throw new IllegalStateException("BlazeStorageUtil class");
    }

    /**
     * 외부 저장장치가 mount 되었는지 확인
     * - sdcard, usb
     */
    public static boolean isMountedExternalStorage(Context a_context) {
        updateDevices(a_context);

        String strName;
        for (Storage storage : sStorageList) {
            strName = storage.getName().toLowerCase();
            if (strName.contains(FileManagerConstants.STORAGE.SDCARD) || strName.contains(FileManagerConstants.STORAGE.USB)) {
                return true;
            }
        }
        return false;
    }

    /**
     * USB path 반환
     */
    public static String getUSBPath() {
        final String usbPath = changeUSBCardDirectory(FileManagerConstants.STORAGE.USB);

        // '/mnt/media_rw/' 뒤의 숫자를 붙여서 반환
        int usb = 0;
        try {
            usb = Integer.parseInt(String.valueOf(FileManagerConstants.STORAGE.PATH_USB_DEFAULT.replace(usbPath, "").charAt(0)));
        } catch (Exception e) { /* Nothing to do */ }

        return usbPath + usb + "/";
    }

    public static Storage getInternalStorage(Context a_context) {
        updateDevices(a_context);

        for (Storage storage : sStorageList) {
            if (TextUtils.equals(storage.getName(), FileManagerConstants.STORAGE.INTERNAL)) {
                return storage;
            }
        }

        return null;
    }

    public static Storage getSdCardStorage(Context a_context) {
        updateDevices(a_context);

        for (Storage storage : sStorageList) {
            if (storage.getName().contains("SDCARD")) {
                storage.updateState();
                return storage;
            }
        }

        return null;
    }


    public static List<Storage> getUsbStorage(Context a_Context) {
        updateDevices(a_Context);

        final List<Storage> storageList = new ArrayList<>();

        for (Storage storage : sStorageList) {
            if (storage.getName().contains("USB") == true) {
                storageList.add(storage);
            }
        }

        return storageList;
    }

    public static List<Storage> getAvailableStorage(Context a_Context) {
        updateDevices(a_Context);

        final List<Storage> storageList = new ArrayList<>();

        for (Storage storage : sStorageList) {
            if (storage.isAvailable()) {
                storageList.add(storage);
            }
        }

        return storageList;
    }

    public static List<Storage> getNotAvailableStorage(Context a_Context) {
        updateDevices(a_Context);

        final List<Storage> storageList = new ArrayList<>();

        for (Storage storage : sStorageList) {
            if (!storage.isAvailable()) {
                storageList.add(storage);
            }
        }

        return storageList;
    }

    public static void updateDevices(Context a_context) {
        if (sStorageList.isEmpty() == true) {
            rescanDevices(a_context);
        }

        for (Storage storage : sStorageList) {
            storage.updateState();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void rescanDevices(Context a_context) {
        sStorageList.clear();

        final StorageManager storageMgr = (StorageManager) a_context.getSystemService(Context.STORAGE_SERVICE);
        final List<StorageVolume> storageVolumes = storageMgr.getStorageVolumes();
        if (storageVolumes == null || storageVolumes.size() == 0) {
            return;
        }

        updateStorageList();
    }

    public static void updateStorageList() {
        String def_path = Environment.getExternalStorageDirectory().getPath();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);

        HashSet<String> paths = new HashSet<>();

        if (def_path_available) {
            paths.add(def_path);
            sStorageList.add(new Storage(def_path));
        }

        try (BufferedReader buf_reader = new BufferedReader(new FileReader("/proc/mounts"))) {
            String line;
            while ((line = buf_reader.readLine()) != null) {
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    tokens.nextToken(); // device. Oo not delete this code to get file system below.
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    tokens.nextToken(); // file system
                    tokens.nextToken(); // flags
                    //List<String> flags = Arrays.asList(tokens.nextToken().split(","));
                    //boolean readonly = flags.contains("ro");

                    if (line.contains("/dev/block/vold") && !line.contains("/mnt/secure")
                            && !line.contains("/mnt/asec")
                            && !line.contains("/mnt/obb")
                            && !line.contains("/dev/mapper")
                            && !line.contains("tmpfs")) {
                        paths.add(mount_point);
                        sStorageList.add(new Storage(mount_point));
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String changeSDCardDirectory(String path) {
        if (path.equals("sdcard0") == true || path.equals("sdcard") == true) {
            return "SDCARD";
        }

        if (path.equals("/storage/sdcard") == true) {
            return "/storage/SDCARD";
        }

        return path.replace("/storage/sdcard0", "/storage/SDCARD");
    }

    public static String changeUSBCardDirectory(String path) {
        if (path.equals("usb")) {
            return "/mnt/media_rw/";
        }

        if (path.equals("/storage/usb6")) {
            return "/mnt/media_rw/5";
        }

        if (path.equals("/storage/usb")) {
            return "/mnt/media_rw/";
        }

        return path.replace("/storage/usb1", "/mnt/media_rw/0");
    }

    /**
     * BRAILLESNV-6388 170204 CSW
     * 남은 메모리 용량 구하기
     * (하위 폴더를 경로로 넘겨도 해당 disk 용량 출력)
     *
     * @param file 경로 (File)
     * @return 여유 공간 (byte)
     */
    public static long getAvailableMemorySize(File file) {
        StatFs stat = new StatFs(file.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    /**
     * 전체 메모리 사이즈 반환
     */
    public static String getTotalMemorySize(Context a_context, String path) {
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();

        if (totalBlocks == 0) return NO_SIZE;
        return formatSize(a_context, totalBlocks * blockSize);
    }

    /**
     * 남은 용량 사이즈
     */
    public static String getFreeMemorySize(Context a_context, String path) {
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return formatSize(a_context, availableBlocks * blockSize);
    }

    /**
     * 디스크 사이즈의 용량 구하기
     */
    private static String formatSize(Context a_context, long size) {
        String suffix = "Byte";
        String suffixLast = "B";

        double totalSize;
        if (size >= 1024) {
            suffix = "K" + suffixLast;
            totalSize = (size / (double) 1024);
            if (totalSize >= 1024) {
                suffix = "M" + suffixLast;
                totalSize /= 1024;
                if (totalSize >= 1024) {
                    suffix = "G" + suffixLast;
                    totalSize /= 1024;
                }
            }
        } else {
            totalSize = (double) size;
        }

        String sSize = String.format(Locale.US, "%.2f", totalSize);
        if (sSize.indexOf(',') > -1) {
            sSize = sSize.replace("", ",");
        }

        return Double.parseDouble(sSize) + suffix;
    }

    /**
     * Flashdisk 에 기본 폴더를 생성한다.
     */
    public static void makeDefaultFolder(Context context) {
        // MTP
        String base = Environment.getExternalStorageDirectory().getAbsolutePath();
        String[] paths = {
                Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_PODCASTS,
                Environment.DIRECTORY_RINGTONES,
                Environment.DIRECTORY_DOCUMENTS,
                "Database",
                "Daisy",
                Environment.DIRECTORY_MUSIC + "/radio",
                "Upload"};

        for (String path : paths) {
            makeFolder(path, base, context);
        }

        makeFolder(Environment.DIRECTORY_MUSIC + "/record", base, context);
        makeFolder("Favorite", base, context);
    }

    /*
     * 만들 고자 하는 디렉토리의 상위 디렉토리가 존재하지 않는 경우, 상위 디렉토리 까지 생성 하는 메소드
     * @param name 디렉토리 경로
     */
    private static void makeFolder(String path, String base, Context context) {
        File dir = new File(base + "/" + path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}