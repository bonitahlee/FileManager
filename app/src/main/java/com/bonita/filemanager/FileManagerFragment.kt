package com.bonita.filemanager

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.bonita.filemanager.data.FileItem
import com.bonita.filemanager.receiver.DiskMountReceiver
import com.bonita.filemanager.widget.FileItemAdapter

/**
 * File Manager Fragment
 *
 * @author bonita
 * @date 2021-11-17
 */
abstract class FileManagerFragment : Fragment() {

    lateinit var replaceFragmentListener: FileManagerListener.FragmentListener

    internal lateinit var fileAdapter: FileItemAdapter

    private lateinit var diskMountReceiver: DiskMountReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerDiskReceiver()
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(diskMountReceiver)

        super.onDestroy()
    }

    /**
     * 파일 리스트에서 체크한 항목 리스트를 가져옴
     */
    protected open fun getCheckedFileItems(a_listView: ListView, a_fileItems: List<FileItem>): ArrayList<FileItem> {
        val checkedFileItems = ArrayList<FileItem>()
        val checkedPositions = a_listView.checkedItemPositions

        a_fileItems.forEachIndexed { index, fileItem ->
            if (checkedPositions.get(index) == true) {
                checkedFileItems.add(fileItem)
            }
        }

        if (a_fileItems.isEmpty() == false && checkedFileItems.isEmpty() == true) {
            // 선택된 항목이 없으면 현재 리스트뷰 위치한 항목을 추가
            checkedFileItems.add(a_fileItems[a_listView.selectedItemPosition])
        }

        return checkedFileItems
    }

    /**
     * Blaze key 처리
     */
    abstract fun onBlazeKey(a_keyCode: Int, a_event: KeyEvent): Boolean

    /**
     * 외장 메모리 mount / unmount 상태 변경 시 처리
     */
    abstract fun onDiskMountStateChanged(a_devicePath: String, a_isInserted: Boolean)

    /**
     * 외장 메모리  mount / unmount receiver 등록
     */
    private fun registerDiskReceiver() {
        diskMountReceiver = DiskMountReceiver(requireActivity(), object : DiskMountReceiver.DiskMountListener {
            override fun onMountStateChanged(a_path: String?, a_isInserted: Boolean) {
                if (a_path == null) return
                onDiskMountStateChanged(a_path, a_isInserted)
            }
        })

        val memoryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addDataScheme("file")
        }

        requireActivity().registerReceiver(diskMountReceiver, memoryFilter)
    }
}