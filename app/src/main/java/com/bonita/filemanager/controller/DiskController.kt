package com.bonita.filemanager.controller

import android.content.Context
import com.bonita.filemanager.controller.task.UpdateDiskListTask
import com.bonita.filemanager.data.FileItem

/**
 * Disk Fragment 에서 사용하는 기능
 *
 * @author bonita
 * @date 2021-11-19
 */
class DiskController {

    // 디스크 목록
    val diskItems = arrayListOf<FileItem>()

    /**
     * 디스크 목록 update
     */
    fun updateDiskList(a_context: Context, a_callback: () -> Unit) {
        UpdateDiskListTask(diskItems, a_callback).execute(a_context)
    }
}