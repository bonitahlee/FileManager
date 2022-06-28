package com.bonita.filemanager.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bonita.filemanager.data.FileItem

/**
 * FileItem 을 보여주기 위한 adapter
 *
 * @author bonita
 * @date 2021-11-19
 */
class FileItemAdapter(
    a_context: Context, private var resource: Int,
    a_fileItems: List<FileItem>,
    private val isCountable: Boolean
) : ArrayAdapter<FileItem>(a_context, resource, a_fileItems) {

    override fun getView(a_position: Int, a_convertView: View?, a_parent: ViewGroup): View {
        val convertView: View
        val fileItemTextView: TextView

        if (a_convertView == null) {
            convertView = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(resource, null)
            fileItemTextView = convertView.findViewById(android.R.id.text1) as TextView
            convertView.tag = fileItemTextView
        } else {
            convertView = a_convertView
            fileItemTextView = convertView.tag as TextView
        }

        // 항목 정보 setting
        fileItemTextView.text = getItem(a_position)!!.fileName

        // Description setting
        convertView.contentDescription = StringBuilder().apply {
            append(fileItemTextView.text.toString())

            // counting 해줘야하는 지 판단
            // -> 디스크 목록에서는 필요,
            // -> 파일 목록에서는 파일 리스트뷰에서 카운팅 처리 (파일/폴더 종류 읽어주기 위해..)
            if (isCountable == true) {
                append(' ')
                append(a_position + 1)
                append('/')
                append(count)
            }
        }

        return convertView
    }
}