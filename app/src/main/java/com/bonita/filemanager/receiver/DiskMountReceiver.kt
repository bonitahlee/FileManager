package com.bonita.filemanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.bonita.filemanager.R
import com.bonita.filemanager.constants.FileManagerConstants
import com.bonita.filemanager.util.StorageUtil

/**
 * 외장 메모리  mount / unmount receiver
 *
 * @author bonita
 * @date 2021-12-22
 */
class DiskMountReceiver(
    a_context: Context,
    private val diskMountListener: DiskMountListener
) : BroadcastReceiver() {

    private val mountedList = arrayListOf<String>()

    init {
        // Mount 되어있는 목록 update
        val sdCard = StorageUtil.getSdCardStorage(a_context)
        sdCard?.let {
            if (it.isAvailable == true) {
                mountedList.add(a_context.getString(R.string.FMS_CMD_EXTEND_SD))
            }
        }

        StorageUtil.getUsbStorage(a_context).forEach {
            if (it.isAvailable == true) {
                // [16.12.27][YKWON][BRAILLESNV-5353, 5385] usb관련 이슈 수정 (usb의 디스크 이름과 경로가 마운트됐을때와 달라서 맞춰줌)
                val name = it.name
                val num = name.substring(name.length - 1, name.length).toInt()

                //SWTEAM-1756 Julie 180604. 파일 탐색기 리스트 갱신 시 520B에서 USB6로 갱신하는 문제 수정을 위함.
                if (num == 1) {
                    mountedList.add(a_context.getString(R.string.FMS_CMD_EXTEND_SMD))
                } else if (num > 1) {
                    mountedList.add(a_context.getString(R.string.FMS_CMD_EXTEND_SMD).toString() + " " + num)
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val path = intent.data?.path

        val deviceName = getDeviceName(context, path)
        if (TextUtils.isEmpty(deviceName) == true) {
            return
        }

        if (action == Intent.ACTION_MEDIA_MOUNTED) {
            // 발생된 이벤트가 디스크 연결이면 동작한다.
            mountedList.add(deviceName)
            diskMountListener.onMountStateChanged(path, true)

        } else if (action == Intent.ACTION_MEDIA_UNMOUNTED) {
            // 발생된 이벤트가 디스크 해제이면 동작한다.

            // sd card 를 삽입 하면 unmount 와 mount 가 같이 날라오기 때문에 mount 된 장비 일때에만 알림 출력하도록 수정
            for (i in mountedList.indices) {
                if (deviceName == mountedList[i]) {
                    mountedList.removeAt(i)
                    diskMountListener.onMountStateChanged(path, false)
                }
            }
        }
    }

    /**
     * 외장 메모리의 이름 반환
     */
    private fun getDeviceName(a_context: Context, path: String?): String {
        var device = ""

        if (path?.startsWith(FileManagerConstants.STORAGE.PATH_USB, false) == true) {
            device = path.substring(FileManagerConstants.STORAGE.PATH_USB.length)
            var deviceNumber = try {
                device.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }

            deviceNumber++

            if (deviceNumber == 1) {
                device = a_context.getString(R.string.FMS_CMD_EXTEND_SMD)
            } else if (deviceNumber > 1) {
                device = a_context.getString(R.string.FMS_CMD_EXTEND_SMD) + " " + deviceNumber
            }

        } else if (path?.startsWith(FileManagerConstants.STORAGE.PATH_SD) == true) {
            device = a_context.getString(R.string.FMS_CMD_EXTEND_SD)
        }

        return device
    }

    interface DiskMountListener {
        fun onMountStateChanged(a_path: String?, a_isInserted: Boolean)
    }
}