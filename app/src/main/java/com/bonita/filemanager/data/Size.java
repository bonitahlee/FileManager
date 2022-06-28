package com.bonita.filemanager.data;

import android.os.Build;
import android.os.Build.VERSION;
import android.os.StatFs;
import android.util.Pair;

import java.io.File;

/**
 * ??
 *
 * @author bonita
 * @date 2021-11-19
 */
public class Size extends Pair<Long, Long> {

    private Size(Long free, Long size) {
        super(free, size);
    }

    public static Size getSpace(File file) {
        if (file == null || !file.exists()) {
            return new Size(0L, 0L);
        }

        if (VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return new Size(file.getUsableSpace(), file.getTotalSpace());

        } else {
            StatFs fs = new StatFs(file.getAbsolutePath());
            return new Size(fs.getAvailableBlocksLong() * fs.getBlockSizeLong(), fs.getBlockCountLong() * fs.getBlockSizeLong());
        }
    }

}
