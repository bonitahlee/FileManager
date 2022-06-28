package com.bonita.filemanager.factory

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.bonita.filemanager.FileManagerFragment
import com.bonita.filemanager.FileManagerListener
import com.bonita.filemanager.R
import com.bonita.filemanager.constants.BlazeKey
import com.bonita.filemanager.constants.FileManagerConstants
import com.bonita.filemanager.controller.FileController
import com.bonita.filemanager.data.FileItem
import com.bonita.filemanager.widget.FileItemAdapter

/**
 * Root 파일 목록 Fragment
 *
 * @author bonita
 * @date 2021-11-19
 */
class RootFileFragment : FileManagerFragment() {

    companion object {
        const val FRAGMENT_TAG = "RootFileFragment"

        fun newInstance(a_targetPath: String): RootFileFragment {
            return RootFileFragment().apply {
                arguments = Bundle().apply {
                    putString(FileManagerConstants.FRAGMENT.TARGET_PATH, a_targetPath)
                }
            }
        }
    }

    // 파일 목록
    private lateinit var fileListView: ListView

    private val fileController by lazy {
        val targetPath = requireArguments().getString(FileManagerConstants.FRAGMENT.TARGET_PATH)
        val fileType = getString(R.string.FMS_MSG_TYPE_ALL_FILES)

        FileController(targetPath, fileType, arrayOfNulls(0), false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileController.setFileListUpdateListener(object : FileManagerListener.FileListUpdateListener {
            override fun onSuccess(a_focusPosition: Int, a_focusFileName: String?) {
                setCheckAll(false)
                fileAdapter.notifyDataSetChanged()

                if (fileAdapter.isEmpty == true) {
                    return
                }

                // 파일 리스트 갱신 후 selection 할 위치로 이동
                var targetPosition = if (a_focusPosition != -1) {
                    a_focusPosition
                } else {
                    fileController.getFocusPosition(a_focusFileName)
                }

                if (targetPosition == fileAdapter.count) {
                    targetPosition--
                }

                fileListView.setSelection(targetPosition)

                // update 전/후 position 이 같으면 dispatchPopulateAccessibilityEvent 를
                // 받지 못하므로 임의로 한번 더 읽어주도록 함
                fileListView.requestFocus()
            }

            override fun onFail(a_errorMsg: Int) {
                // 디스크 목록으로 이동
                replaceFragmentListener.onReplaceFragment(RootDiskFragment.FRAGMENT_TAG, FileManagerConstants.STORAGE.FLASHDISK, false)
            }
        })
    }

    override fun onCreateView(a_inflater: LayoutInflater, a_container: ViewGroup?, a_savedInstanceState: Bundle?): View? {
        return a_inflater.inflate(R.layout.fragment_file_list, a_container, false)
    }

    override fun onViewCreated(a_view: View, a_savedInstanceState: Bundle?) {
        super.onViewCreated(a_view, a_savedInstanceState)

        bindListView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 파일 목록 가져오기
        fileController.initialize(requireActivity())
        fileController.updateList("")
    }

    override fun onBlazeKey(a_keyCode: Int, a_event: KeyEvent): Boolean {
        when (a_keyCode) {
            // 프로그램 종료
            BlazeKey.CONTROL_CANCEL -> {
                if (fileListView.checkedItemCount == 0) {
                    requireActivity().finish()
                } else {
                    return false
                }
            }

            // 현재 파일/폴더 열기
            BlazeKey.NAVIGATION_OK -> {
                if (fileAdapter.isEmpty == false) {
                    open(fileListView.selectedItemPosition)
                }
            }

            // 상위 목록 진입
            BlazeKey.NAVIGATION_LEFT -> {
                moveToUpper()
            }

            // 하위 목록 진입
            BlazeKey.NAVIGATION_RIGHT -> {
                if (fileAdapter.isEmpty == false) {
                    moveToSub()
                }
            }

            else -> return false
        }

        return true
    }

    override fun onDiskMountStateChanged(a_devicePath: String, a_isInserted: Boolean) {
        val targetPath = requireArguments().getString(FileManagerConstants.FRAGMENT.TARGET_PATH) ?: ""
        if (a_isInserted == false && targetPath.startsWith(a_devicePath) == true) {
            // USB or SD unmount 시 -> disk fragment 로 이동
            replaceFragmentListener.onReplaceFragment(RootDiskFragment.FRAGMENT_TAG, targetPath, false)
        }
    }

    private fun bindListView() {
        fileListView = requireActivity().findViewById<ListView>(R.id.lv_file).apply {
            choiceMode = ListView.CHOICE_MODE_MULTIPLE
            fileAdapter = FileItemAdapter(requireActivity(), android.R.layout.simple_list_item_multiple_choice, fileController.fileItems, false)
            adapter = fileAdapter
            setOnItemClickListener { _, _, position, _ -> open(position) }
        }
    }

    /**
     * 폴더/파일 열기
     */
    private fun open(a_position: Int) {
        val focusedItem = fileAdapter.getItem(a_position)!!
        if (focusedItem.type == FileItem.TYPE.FILE) {
            fileController.openFile(requireActivity(), getCheckedFileItems(fileListView, fileController.fileItems), focusedItem)
        }

        setCheckAll(false)
    }

    /**
     * 상위 항목으로 이동
     */
    private fun moveToUpper() {
        fileController.moveToUpper(requireActivity(), replaceFragmentListener)
    }

    /**
     * 하위 항목으로 이동
     */
    private fun moveToSub() {
        val focusedItem = fileListView.selectedItem as FileItem
        if (focusedItem.type == FileItem.TYPE.DIRECTORY) {
            // 하위 폴더로 이동

            fileController.moveToSub(requireActivity(), focusedItem)
        }
    }

    /**
     * 모든 리스트 아이템들의 선택/체크 상태를 set 한다.
     */
    private fun setCheckAll(a_isCheck: Boolean) {
        fileListView.run {
            clearChoices()
            val count: Int = getCount()
            for (cnt in 0 until count) {
                setItemChecked(cnt, a_isCheck)
            }

            // [TODO][parkho] 추후 T90 에서 확인 필요
            // [BRAILLE SB-69][20.10.28][julie.h.jo] 리스트 블럭 선택 후 취소시 체크가 없어지지 않는 문제 수정
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                invalidateViews()
            }
        }
    }
}