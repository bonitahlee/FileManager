package com.bonita.filemanager.data;

import android.os.Environment;

import com.bonita.filemanager.constants.FileManagerConstants;

import java.io.File;

/**
 * Storage
 *
 * @author bonita
 * @date 2021-11-19
 */
public class Storage {

    private final String mPath;
    private String mName;
    private boolean mAvailable;

    public Storage(String a_path) {
        // [21.12.01][bonita] sdcard path 임의 변경
        if (a_path.equals("/mnt/media_rw/SDCARD") == true) {
            a_path = FileManagerConstants.STORAGE.PATH_SD;
        }

        mPath = a_path;
        updateState();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Name : ").append(mName).append(" | ");
        sb.append("mMountPoint : ").append(mPath).append(" | ");
        sb.append("mAvailable : ").append(mAvailable).append(" | ");
        sb.append("mWriteAble : ").append(isWriteAble()).append(" |");
        return sb.toString();
    }

    public String getName() {
        return mName;
    }

    public final File getFile() {
        return new File(mPath);
    }

    public final Size getSize() {
        if (mAvailable) {
            return Size.getSpace(getFile());
        }

        return null;
    }

    public final String getPath() {
        return mPath;
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    public boolean isWriteAble() {
        if (mAvailable) {
            return getFile().canWrite();
        }

        return false;
    }

    public String getState() {
        File f = new File(mPath);
        return Environment.getExternalStorageState(f);
    }

    public void updateState() {
        File f = new File(mPath);
        mName = f.getName();

        boolean usbDevice = mPath.contains(FileManagerConstants.STORAGE.PATH_USB);
        if (usbDevice == true) {
            try {
                mName = "USB" + String.format("%d", Integer.parseInt(mPath.replace("/mnt/media_rw/", "")) + 1);
            } catch (Exception e) {
                e.printStackTrace();

                mName = "USB1";
            }
        } else {
            if (mName.equals("0")) {
                mName = FileManagerConstants.STORAGE.INTERNAL;
            }
        }

        final String mount = Environment.getExternalStorageState(f);
        mAvailable = Environment.MEDIA_MOUNTED.equals(mount);

        if (usbDevice == true) {
            mAvailable = true;
        }
    }
}