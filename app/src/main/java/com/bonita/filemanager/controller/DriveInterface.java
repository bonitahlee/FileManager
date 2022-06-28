package com.bonita.filemanager.controller;

import android.app.Activity;

import com.bonita.filemanager.FileManagerListener;
import com.bonita.filemanager.data.FileItem;

import java.util.List;

/**
 * 드라이브에서 사용하는 Function 을 묶은 인터페이스
 *
 * @author Bonita, Julie
 * @date 2020-01-14
 */

public interface DriveInterface {

    /* 파일 리스트 초기화 */
    void initialize(Activity a_activity);

    /* 파일 리스트 갱신 - 서버에서 파일 목록 받아옴 */
    void updateList(String a_selectFilePath);

    /* 파일 리스트 갱신 - custom update listener */
    void updateList(final String a_selectFileName, final FileManagerListener.FileListUpdateListener a_listener);

    /* 상위 경로로 이동 */
    void moveToUpper(Activity a_activity, FileManagerListener.FragmentListener a_replaceFragmentListener);

    /* 하위 경로로 이동 */
    void moveToSub(Activity a_activity, FileItem a_item);

    /* 디스크 프래그먼트로 이동 */
    void moveToDisk(Activity a_activity, FileManagerListener.FragmentListener a_replaceFragmentListener);

    /* 파일 열기 */
    void openFile(Activity a_activity, List<FileItem> a_checkedItems, FileItem a_focusedItem);

    /* 파일이름으로 position 찾기 */
    int getFilePosition(String a_fileName);

    /* 파일 리스트 갱신을 알려주는 리스너 세팅 */
    void setFileListUpdateListener(FileManagerListener.FileListUpdateListener a_refreshCompleteListener);
}