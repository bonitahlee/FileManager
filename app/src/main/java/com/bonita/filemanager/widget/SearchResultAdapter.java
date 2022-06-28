package com.bonita.filemanager.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bonita.filemanager.data.FileItem;
import com.bonita.filemanager.util.FileManagerUtil;

import java.util.List;
import java.util.Map;

/**
 * 검색 결과를 보여주는 Adapter
 *
 * @author bonita
 * @date 2021-11-22
 */
public class SearchResultAdapter extends ArrayAdapter<FileItem> {

    private final int mId;
    private final List<FileItem> mItemList;

    public SearchResultAdapter(Context a_context, int a_resource, List<FileItem> a_objects) {
        super(a_context, a_resource, a_objects);

        mId = a_resource;
        mItemList = a_objects;
    }

    @Override
    public long getItemId(int a_position) {
        return a_position;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public View getView(int a_position, View a_convertView, ViewGroup a_parent) {
        final LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final TextView tv;

        if (a_convertView == null) {
            a_convertView = li.inflate(mId, null);
            tv = a_convertView.findViewById(android.R.id.text1);
            a_convertView.setTag(tv);
        } else {
            tv = (TextView) a_convertView.getTag();
        }

        // 검색 결과 보여주는 방식
        // - T70ET.db-journal /Flashdisk/Database/T70ET.db-journal 1 슬래시 233
        final FileItem item = getItem(a_position);
        final String displayPath = getDisplayPath(item);
        tv.setText(displayPath);      // 현재 파일의 display path

        final String description = displayPath + " " + (a_position + 1) + "/" + getCount();
        a_convertView.setContentDescription(description);
        return a_convertView;
    }

    private String getDisplayPath(final FileItem a_fileItem) {
        String displayPath = a_fileItem.getFilePath();
        final Map<String, String> diskMap = FileManagerUtil.INSTANCE.getStorages(getContext());
        for (Map.Entry<String, String> entry : diskMap.entrySet()) {
            String key = entry.getKey();
            final String p = entry.getValue();
            if (a_fileItem.getFilePath().startsWith(p) == true) {
                displayPath = FileManagerUtil.INSTANCE.replaceFirst(a_fileItem.getFilePath(), p, key, false);
                break;
            }
        }

        return displayPath;
    }
}