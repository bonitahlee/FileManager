package com.bonita.filemanager.factory

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.bonita.filemanager.FileManagerFragment
import com.bonita.filemanager.R
import com.bonita.filemanager.constants.BlazeKey
import com.bonita.filemanager.constants.FileManagerConstants
import com.bonita.filemanager.controller.DiskController
import com.bonita.filemanager.data.FileItem
import com.bonita.filemanager.widget.FileItemAdapter


/**
 * Root 디스크 목록 Fragment
 *
 * @author bonita
 * @date 2021-11-19
 */
class RootDiskFragment : FileManagerFragment() {

    companion object {
        const val FRAGMENT_TAG = "RootDiskFragment"

        fun newInstance(a_targetPath: String, a_isFirst: Boolean): RootDiskFragment {
            return RootDiskFragment().apply {
                arguments = Bundle().apply {
                    putString(FileManagerConstants.FRAGMENT.TARGET_PATH, a_targetPath)
                    putBoolean(FileManagerConstants.FRAGMENT.IS_FIRST, a_isFirst)
                }
            }
        }
    }

    // 첫 시작인지 확인
    private var isFirstShowing = true

    // 디스크 목록
    private lateinit var diskListView: ListView

    private val diskController = DiskController()

    override fun onCreateView(a_inflater: LayoutInflater, a_container: ViewGroup?, a_savedInstanceState: Bundle?): View? {
        return a_inflater.inflate(R.layout.fragment_disk_list, a_container, false)
    }

    override fun onViewCreated(a_view: View, a_savedInstanceState: Bundle?) {
        super.onViewCreated(a_view, a_savedInstanceState)

        bindListView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        isFirstShowing = requireArguments().getBoolean(FileManagerConstants.FRAGMENT.IS_FIRST)

        // 디스크 목록 가져오기
        val diskName = requireArguments().getString(FileManagerConstants.FRAGMENT.TARGET_PATH) ?: ""
        updateDiskList(diskName)
    }

    override fun onBlazeKey(a_keyCode: Int, a_event: KeyEvent): Boolean {
        when (a_keyCode) {
            // 하위 목록 진입
            BlazeKey.NAVIGATION_OK,
            BlazeKey.NAVIGATION_RIGHT ->
                moveToSub(diskListView.selectedItemPosition)

            // 프로그램 종료
            BlazeKey.CONTROL_CANCEL -> {
                requireActivity().finish()
            }

            else -> return false
        }

        return true
    }

    override fun onDiskMountStateChanged(a_devicePath: String, a_isInserted: Boolean) {
        updateDiskList("")
    }

    private fun bindListView() {
        diskListView = requireActivity().findViewById<ListView>(R.id.lv_disk).apply {
            fileAdapter = FileItemAdapter(requireActivity(), android.R.layout.simple_list_item_1, diskController.diskItems, true)
            adapter = fileAdapter
            setOnItemClickListener { _, _, position, _ -> moveToSub(position) }
        }
    }

    /**
     * 디스크 목록 업데이트
     */
    private fun updateDiskList(a_focusDisk: String) {
        diskController.updateDiskList(requireContext()) {
            fileAdapter.notifyDataSetChanged()

            // 특정 디스크에 focus
            for (i: Int in 0 until diskController.diskItems.size) {
                if (diskController.diskItems[i].filePath.startsWith(a_focusDisk) == true) {
                    diskListView.setSelection(i)
                    break
                }
            }

            diskListView.requestFocus()
        }
    }

    /**
     * 디스크 하위 파일 목록 갱신
     */
    private fun moveToSub(a_position: Int) {
        val diskItem = diskListView.getItemAtPosition(a_position) as FileItem
        replaceFragmentListener.onReplaceFragment(RootFileFragment.FRAGMENT_TAG, diskItem.filePath, false)
    }
}