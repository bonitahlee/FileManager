package com.bonita.filemanager.factory;

import com.bonita.filemanager.FileManagerFragment;

/**
 * FileManagerFragment 를 반환하는 factory
 *
 * @author bonita
 * @date 2021-07-23
 */
public class FileManagerFactory {

    private FileManagerFactory() {/* Nothing to do */}

    /**
     * Target Path 에 따라 fragment 생성하여 반환
     */
    public static FileManagerFragment getInstance(final String a_fragmentTag,
                                                  final String a_targetPath, final boolean a_isFirst) {

        final FileManagerFragment fragment;
        if (RootDiskFragment.FRAGMENT_TAG.equals(a_fragmentTag)) {
            // Root -> Disk
            fragment = RootDiskFragment.Companion.newInstance(a_targetPath, a_isFirst);
        } else {
            // Root -> File
            fragment = RootFileFragment.Companion.newInstance(a_targetPath);
        }

        return fragment;
    }
}