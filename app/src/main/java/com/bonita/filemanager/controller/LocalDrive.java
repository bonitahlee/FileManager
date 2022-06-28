package com.bonita.filemanager.controller;

import android.app.Activity;

import com.bonita.filemanager.FileManagerListener;
import com.bonita.filemanager.R;
import com.bonita.filemanager.constants.FileManagerConstants;
import com.bonita.filemanager.controller.task.UpdateFileListTask;
import com.bonita.filemanager.data.FileItem;
import com.bonita.filemanager.data.Storage;
import com.bonita.filemanager.factory.RootDiskFragment;
import com.bonita.filemanager.util.StorageUtil;
import com.filemanager.utils.HanFileJNI;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

/**
 * 로컬 파일 리스트 처리를 위한 함수 class
 *
 * @author Bonita, Julie
 * @date 2019-12-12
 */
public class LocalDrive implements DriveInterface {

    private final List<FileItem> mFileItems;

    // 현재 파일 형식 타입
    private final String mFileType;
    private final String[] mFileTypeArray;

    // 현재 리스트 뷰 타입(폴더, 파일)
    private final boolean mIsFolderShow;

    // 이동할 경로
    private String mTargetDir;
    // 루트 경로
    private String mRootDir;

    private FileManagerListener.FileListUpdateListener mFileListUpdateListener;

    public LocalDrive(final List<FileItem> a_fileItems, final String a_targetPath,
                      final String a_fileType, final String[] a_fileTypeArray,
                      final boolean a_isFolderShow) {

        mFileItems = a_fileItems;
        mTargetDir = a_targetPath;
        mFileType = a_fileType;
        mFileTypeArray = a_fileTypeArray;
        mIsFolderShow = a_isFolderShow;
    }

    @Override
    public void initialize(Activity a_activity) {
        final List<Storage> storageList = StorageUtil.getAvailableStorage(a_activity);
        for (Storage storage : storageList) {
            if (mTargetDir.startsWith(storage.getPath()) == true) {
                // 이동할 경로(mTargetDir) 에 따른 루트 경로를 세팅한다.
                // ex> Target 경로가 Flash/Documents 라면 루트 경로를 Flash 로 지정한다.
                mRootDir = storage.getPath();

                // 타겟 경로 세팅
                final File file = new File(mTargetDir);
                if (file.exists() == false) {
                    // 전달 받은 경로가 존재하지 않으면, 루트 경로로 타겟 경로 세팅
                    mTargetDir = mRootDir;
                }
                return;
            }
        }

        mFileListUpdateListener.onFail(R.string.FMS_ERR_NO_DISK);
    }

    @Override
    public void updateList(final String a_selectFileName) {
        // 목록 update
        final UpdateFileListTask task = buildTask();
        task.setSelectFileName(a_selectFileName);
        task.setListUpdateListener(mFileListUpdateListener);
        task.execute(mTargetDir);
    }

    @Override
    public void updateList(final String a_selectFileName, final FileManagerListener.FileListUpdateListener a_listener) {
        final UpdateFileListTask task = buildTask();
        task.setSelectFileName(a_selectFileName);
        task.setListUpdateListener(a_listener);
        task.execute(mTargetDir);
    }

    @Override
    public void moveToUpper(Activity a_activity, final FileManagerListener.FragmentListener a_replaceFragmentListener) {
        // 상위 경로가 루트이면 디스크 프래그먼트로 교체
        if (mTargetDir.equals(mRootDir) == true) {
            moveToDisk(a_activity, a_replaceFragmentListener);
        } else {
            // 상위 경로로 이동
            final File targetDir = new File(mTargetDir);
            final String targetFolder = targetDir.getName();
            mTargetDir = targetDir.getParent();
            updateList(targetFolder);
        }
    }

    @Override
    public void moveToSub(Activity a_activity, final FileItem a_item) {
        // 하위 경로로 이동
        mTargetDir = a_item.getFilePath();

        updateList(null);
    }

    @Override
    public void moveToDisk(Activity a_activity, final FileManagerListener.FragmentListener a_replaceFragmentListener) {
        a_replaceFragmentListener.onReplaceFragment(RootDiskFragment.FRAGMENT_TAG, mRootDir, false);
    }

    @Override
    public void openFile(Activity a_activity, List<FileItem> a_checkedItems, FileItem a_focusedItem) {
        final String ext = FilenameUtils.getExtension(a_focusedItem.getFileName()).toLowerCase();
        final String filePath = a_focusedItem.getFilePath();
        final File file = new File(filePath);

        // 현재 포커스된 파일 위주로 파일 열기를 수행할 것이므로, 현재 포커스된 파일은 항상 체크되어있다고 판단함
        if (a_checkedItems.contains(a_focusedItem) == false) {
            a_checkedItems.add(a_focusedItem);
        }

        // [todo] mime type 에 따라 파일 열기
    }

    @Override
    public int getFilePosition(final String a_fileName) {
        for (int i = 0; i < mFileItems.size(); i++) {
            if (mFileItems.get(i).getFileName().equalsIgnoreCase(a_fileName) == true) {
                return i;
            }
        }

        return FileManagerConstants.INVALID_VALUE;
    }

    @Override
    public void setFileListUpdateListener(final FileManagerListener.FileListUpdateListener a_refreshCompleteListener) {
        mFileListUpdateListener = a_refreshCompleteListener;
    }

    private UpdateFileListTask buildTask() {
        return new UpdateFileListTask.Builder()
                .setFileItems(mFileItems)
                .setExt(mFileTypeArray)
                .setSortType(HanFileJNI.SORT_BY_NAME)                 // sort by name
                .setFolderShow(mIsFolderShow)
                .setSystemFolderShow(false)
                .build();
    }
}