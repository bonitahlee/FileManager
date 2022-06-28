package com.bonita.filemanager.controller;

import android.app.Activity;
import android.text.TextUtils;

import com.bonita.filemanager.FileManagerListener;
import com.bonita.filemanager.constants.FileManagerConstants;
import com.bonita.filemanager.data.FileItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 드라이브 종류에 따라 프래그먼트와 operation 연결을 위한 클래스
 *
 * @author Bonita, Julie
 * @date 2020-01-14
 */
public class FileController {

    // File items
    private final List<FileItem> mFileItems;

    // Drive interface
    private final DriveInterface mDriveInterface;

    public FileController(final String a_targetPath, final String a_fileType,
                          final String[] a_fileTypeArray, final boolean a_isFolderShow) {

        mFileItems = new ArrayList<>();
        mDriveInterface = new LocalDrive(mFileItems, a_targetPath, a_fileType, a_fileTypeArray, a_isFolderShow);
    }

    /**
     * 파일 리스트 초기화
     */
    public void initialize(Activity a_activity) {
        mDriveInterface.initialize(a_activity);
    }

    /**
     * 파일 리스트 갱신
     * - 서버에서 파일 목록 받아옴
     */
    public void updateList(final String a_selectFileName) {
        mDriveInterface.updateList(a_selectFileName);
    }

    /**
     * 상위 경로로 이동
     */
    public void moveToUpper(Activity a_activity, FileManagerListener.FragmentListener a_replaceFragmentListener) {
        mDriveInterface.moveToUpper(a_activity, a_replaceFragmentListener);
    }

    /**
     * 하위 경로로 이동
     */
    public void moveToSub(Activity a_activity, FileItem a_item) {
        mDriveInterface.moveToSub(a_activity, a_item);
    }

    /**
     * 파일 열기
     */
    public void openFile(Activity a_activity, List<FileItem> a_checkedItems, FileItem a_focusedItem) {
        mDriveInterface.openFile(a_activity, a_checkedItems, a_focusedItem);
    }

    /**
     * 파일 리스트 아이템 이름에 따른 파일 위치 반환
     *
     * @param a_selectFileName Select 할 파일 이름
     */
    public int getFocusPosition(String a_selectFileName) {
        if (TextUtils.isEmpty(a_selectFileName) == true) {
            return 0;
        }

        // Google Drive 는 중복된 이름이 존재하기 때문에 ID로 위치를 가져온다.
        int position = mDriveInterface.getFilePosition(a_selectFileName);
        if (position == FileManagerConstants.INVALID_VALUE) {
            position = 0;
        }

        return position;
    }

    public List<FileItem> getFileItems() {
        return mFileItems;
    }

    public void setFileListUpdateListener(FileManagerListener.FileListUpdateListener a_fileListUpdateListener) {
        mDriveInterface.setFileListUpdateListener(a_fileListUpdateListener);
    }
}