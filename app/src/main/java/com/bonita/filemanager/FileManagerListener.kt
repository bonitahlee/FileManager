package com.bonita.filemanager

import android.content.Intent
import com.bonita.filemanager.data.FileItem

/**
 * 파일 매니저에서 사용하는 리스너
 *
 * @author bonita
 * @date 2021-11-22
 */
class FileManagerListener {

    interface FragmentListener {
        fun onReplaceFragment(a_fragmentTag: String, a_targetPath: String, a_isFirst: Boolean)
    }

    /**
     * File List Update 완료됨을 알려주는 리스너
     */
    interface FileListUpdateListener {
        fun onSuccess(a_focusPosition: Int, a_focusFileName: String?)
        fun onFail(a_errorMsg: Int)
    }

    /**
     * Function 작업 후 처리 리스너
     */
    interface FunctionFinishedListener {
        fun onSuccess(a_message: Int)
        fun onFail()
    }

    /**
     * 프롬포트 다이얼로그에서 선택한 결과값에 따른 작업을 위한 리스너
     */
    interface PromptResultListener {
        fun onYes()
        fun onNo()
    }

    /**
     * Cancel 에 따른 작업을 위한 리스너
     */
    interface ResultListener {
        fun onCancel()
    }

    /**
     * Focus 이동 리스너
     */
    interface MoveFocusListener {
        fun moveTo(a_position: Int)
        fun onError(a_errorResId: Int)
    }
}