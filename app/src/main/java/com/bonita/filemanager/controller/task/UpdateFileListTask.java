package com.bonita.filemanager.controller.task;

import android.os.AsyncTask;

import com.bonita.filemanager.FileManagerListener;
import com.bonita.filemanager.data.FileItem;
import com.filemanager.utils.HanFileJNI;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 현재 경로에 대해 로컬 드라이브 파일 리스트 가져오는 클래스
 *
 * @author Bonita, Julie
 * @date 2019-12-12
 */
public class UpdateFileListTask extends AsyncTask<String, Void, ArrayList<FileItem>> {

    private List<FileItem> mFileItems;

    // update 시 필요한 데이터
    private String[] mExt;
    private int mSortType;
    private boolean mIsSystemFolderShow;
    private boolean mIsFolderShow;

    // refresh 완료 후 파일 리스트에서 select 할 파일 이름
    private String mSelectFileName;

    // refresh 완료 후 파일 리스트에서 select 할 위치
    private int mSelectPosition = -1;

    // Refresh 후 결과 보내기 위한 리스너
    private FileManagerListener.FileListUpdateListener mListUpdateListener;

    public static class Builder {
        private List<FileItem> mFileItems;

        private String[] mExt;
        private int mSortType;
        private boolean mIsSystemFolderShow;
        private boolean mIsFolderShow;

        public Builder setFileItems(List<FileItem> a_fileItems) {
            mFileItems = a_fileItems;
            return this;
        }

        public Builder setExt(String[] a_ext) {
            mExt = a_ext;
            return this;
        }

        public Builder setFolderShow(boolean a_isFolderShow) {
            mIsFolderShow = a_isFolderShow;
            return this;
        }

        public Builder setSystemFolderShow(boolean a_isSystemFolderShow) {
            mIsSystemFolderShow = a_isSystemFolderShow;
            return this;
        }

        public Builder setSortType(int a_sortType) {
            mSortType = a_sortType;
            return this;
        }

        public UpdateFileListTask build() {
            final UpdateFileListTask localDriveRefreshAsyncTask = new UpdateFileListTask();
            localDriveRefreshAsyncTask.mFileItems = mFileItems;
            localDriveRefreshAsyncTask.mExt = mExt;
            localDriveRefreshAsyncTask.mSortType = mSortType;
            localDriveRefreshAsyncTask.mIsSystemFolderShow = mIsSystemFolderShow;
            localDriveRefreshAsyncTask.mIsFolderShow = mIsFolderShow;
            return localDriveRefreshAsyncTask;
        }
    }

    @Override
    protected ArrayList<FileItem> doInBackground(String... strings) {
        File currentDir = null;

        if (strings[0] != null) {
            currentDir = new File(strings[0]);
        }

        if (currentDir == null || currentDir.exists() == false) {
            return new ArrayList(Collections.emptyList());
        }

        /*
         * PickerMode 에 따라 리스트 반환
         * - Folder Open : 폴더 리스트
         * - File, Save Open : 파일 리스트
         */
        final File[] files;
        if (mIsFolderShow == true) {
            files = HanFileJNI.getDirList(currentDir.getAbsolutePath(), mIsSystemFolderShow);
        } else {
            files = HanFileJNI.getFileList(currentDir.getAbsolutePath(), mSortType, mIsSystemFolderShow, mExt);
        }

        if (files == null || files.length == 0) {
            return new ArrayList(Collections.emptyList());
        }

        return addFileList(files);
    }

    @Override
    protected void onPostExecute(ArrayList<FileItem> a_fileItemArrayList) {
        super.onPostExecute(a_fileItemArrayList);

        mFileItems.clear();
        mFileItems.addAll(a_fileItemArrayList);

        // Task 수행 성공 알림
        mListUpdateListener.onSuccess(mSelectPosition, mSelectFileName);
    }

    public void setListUpdateListener(FileManagerListener.FileListUpdateListener a_listener) {
        mListUpdateListener = a_listener;
    }

    public void setSelectFileName(String a_selectFileName) {
        mSelectFileName = a_selectFileName;
    }

    public void setSelectPosition(int a_selectPosition) {
        mSelectPosition = a_selectPosition;
    }

    /**
     * 파일 항목들에 대해 파일 정보를 저장하기 위한 함수
     *
     * @param a_files 현재 폴더의 항목 리스트
     * @return 파일 리스트
     */
    private ArrayList<FileItem> addFileList(File[] a_files) {
        ArrayList<FileItem> items = new ArrayList<>();

        // 리스트 아이템 추가
        for (File file : a_files) {
            final FileItem item = new FileItem();
            if (file.isDirectory() == true) {
                item.setType(FileItem.TYPE.DIRECTORY);
            } else {              // 파일일 경우 파일 size 저장
                item.setFileSize(file.length());
            }
            item.setFilePath(file.getAbsolutePath());
            item.setFileName(file.getName());
            item.setFileDate(String.valueOf(file.lastModified()));
            items.add(item);
        }

        return items;
    }
}