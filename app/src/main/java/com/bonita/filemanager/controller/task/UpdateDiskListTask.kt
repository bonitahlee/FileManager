package com.bonita.filemanager.controller.task

import android.content.Context
import android.os.AsyncTask
import com.bonita.filemanager.data.FileItem
import com.bonita.filemanager.util.FileManagerUtil

/**
 * 디스크 목록 update
 *
 * @author bonita
 * @date 2021-11-19
 */
class UpdateDiskListTask(
    private val diskItems: ArrayList<FileItem>,
    private val callback: () -> Unit
) : AsyncTask<Context, Unit, ArrayList<FileItem>>() {

    override fun doInBackground(vararg a_context: Context): ArrayList<FileItem> {
        val diskList = arrayListOf<FileItem>()
        val diskMap = FileManagerUtil.getStorages(a_context[0])

        diskMap.forEach {
            val fileItem = FileItem().apply {
                fileName = it.key
                filePath = it.value
                type = FileItem.TYPE.DISK
            }
            diskList.add(fileItem)
        }

        return diskList
    }

    override fun onPostExecute(a_result: ArrayList<FileItem>) {
        super.onPostExecute(a_result)

        diskItems.clear()
        diskItems.addAll(a_result)

        callback()
    }
}