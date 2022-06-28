package com.bonita.filemanager.util

import android.content.Context
import android.content.res.Configuration
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import com.bonita.filemanager.R
import com.bonita.filemanager.constants.FileManagerConstants
import java.io.*
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.math.log10
import kotlin.math.pow

/**
 * 파일 매니저에서 쓰이는 Util
 */
object FileManagerUtil {

    /**
     * 파일 size + 단위(B/KB/MB/GB/TB 등) 반환
     */
    fun getFileSize(a_size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val basis: Long = 1024
        if (a_size <= 0) {
            return "0" + units[0]
        }

        val unitPosition = (log10(a_size.toDouble()) / log10(basis.toDouble())).toInt()
        var resultStr = DecimalFormat("#,##0.##").format(a_size / basis.toDouble().pow(unitPosition.toDouble()))
        resultStr += units[unitPosition]
        return resultStr
    }

    /**
     * 특수문자 포함여부 확인
     */
    fun hasSpecialCharacter(a_text: String): Boolean {
        return (a_text.contains("|") || a_text.contains("\\")
                || a_text.contains("<") || a_text.contains("\"")
                || a_text.contains(">") || a_text.contains("?")
                || a_text.contains(":") || a_text.contains("*"))
    }


    /**
     * Storage 목록 반환
     */
    fun getStorages(a_context: Context): Map<String, String> {
        // Storage 목록 갱신을 위해 rescan
        StorageUtil.rescanDevices(a_context)

        val rootPath = LinkedHashMap<String, String>()

        // 1. FlashDisk
        StorageUtil.getInternalStorage(a_context)?.let {
            rootPath[a_context.getString(R.string.FMS_CMD_FLASH_DISK)] = it.path
        }

        // 2. SD card
        StorageUtil.getSdCardStorage(a_context)?.let {
            if (it.isAvailable == true) {
                rootPath[a_context.getString(R.string.FMS_CMD_EXTEND_SD)] = it.path
            }
        }

        // 3. USB
        StorageUtil.getUsbStorage(a_context)?.forEach {
            if (it.isAvailable == true) {
                val name = it.name
                val num = name.substring(name.length - 1).toInt()

                if (num == 1) {
                    rootPath[a_context.getString(R.string.FMS_CMD_EXTEND_SMD)] = it.path
                } else if (num > 1) {
                    rootPath[a_context.getString(R.string.FMS_CMD_EXTEND_SMD) + " " + num] = it.path
                }
            }
        }

        return rootPath
    }

    /**
     * SD card lock 여부 반환
     */
    fun isSdcardLocked(context: Context, path: String): Boolean {
        val diskMap: Map<String, String> = getStorages(context)
        val sdCard = context.getString(R.string.FMS_CMD_EXTEND_SD)
        if (diskMap.containsKey(sdCard) == false) {
            return false
        }

        val sdPath = diskMap[sdCard]
        if (path.startsWith(sdPath!!)) {
            var file: File? = null
            for (i in 0 until Int.MAX_VALUE) {
                file = File(sdPath, "test$i.txt")
                if (file.exists() == false) {
                    break
                }
            }

            try {
                FileOutputStream(file).use { fos -> fos.write(' '.code) }
            } catch (e: IOException) {
                return true
            }

            file?.delete()
        }
        return false
    }

    /**
     * 파일 시스템 반환
     */
    fun getFileSystem(a_path: String): String? {
        var path = a_path
        path = path.replace("/storage/", "")
        path = path.substring(0, path.indexOf('/'))

        try {
            val mount = Runtime.getRuntime().exec("mount")
            val reader = BufferedReader(InputStreamReader(mount.inputStream))
            mount.waitFor()

            var line: String
            while (reader.readLine().also { line = it } != null) {
                val split = line.split("\\s+").toTypedArray()
                for (i in 0 until split.size - 1) {
                    if (split[i] != "/" && split[i].endsWith(path) == true) {
                        return split[i + 1]
                    }
                }
            }
            reader.close()
            mount.destroy()

        } catch (e: IOException) {
            e.printStackTrace()
            Thread.currentThread().interrupt()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            Thread.currentThread().interrupt()
        }
        return null
    }

    /**
     * [17.01.12][YKWON][BRAILLESNV-6186] 더이상 디스크에 용량이 없으면 붙여넣기를 끝내기 위해 붙여넣기가 가능한지 확인
     *
     * @param srcFile 붙여넣기할 파일
     * @param dstPath 붙여넣기될 directory
     */
    fun hasRemainingSpace(srcFile: File, dstPath: String): Boolean {
        //20190115.JKH.내장 디스크 공간이 300mb 이하이면, 경고 문구 출력하도록 기능 추가.
        val path = Environment.getDataDirectory()
        if (dstPath.startsWith(FileManagerConstants.STORAGE.PATH_FLASHDISK) == true
            && StorageUtil.getAvailableMemorySize(path) < 1024 * 1024 * 300
        ) {
            return false
        }

        // 붙여넣기할 파일의 용량
        val srcFileSize = getDirSize(srcFile)

        // 붙여넣기할 directory 의 남은 용량
        val statFs = StatFs(dstPath)
        val dstDirSize = statFs.availableBytes
        return (dstDirSize - srcFileSize) > 0
    }

    /**
     * 남은 용량 확인
     */
    fun hasRemainingSpace(a_size: Long, a_folderPath: String): Boolean =
        (StatFs(a_folderPath).availableBytes - a_size) > 0

    fun isBlank(str: String): Boolean {
        if (TextUtils.isEmpty(str) == true) {
            return true
        }

        str.forEachIndexed { index, c ->
            if (Character.isWhitespace(c) == false) {
                return@isBlank false
            }
        }

        return true
    }

    // [17.02.03][YKWON] str 의 문자열에서 fromStr 을 toStr 로 가장 처음에 있는 하나만 바꿔줌, 정규표현식때문에 별도의 메소드 만들어서 한번에 관리함
    fun replaceFirst(a_str: String, a_fromStr: String, a_toStr: String, a_isIgnoreCase: Boolean): String? {
        var str = a_str
        var fromStr = a_fromStr
        var toStr = a_toStr

        if (a_isIgnoreCase) {
            fromStr = fromStr.lowercase(Locale.getDefault())
            toStr = toStr.lowercase(Locale.getDefault())
            str = str.lowercase(Locale.getDefault())
        }

        val result = try {
            if (str.indexOf(fromStr) != -1) {
                val temp = str.substring(fromStr.length, str.length)
                toStr + temp
            } else {
                str
            }
        } catch (e: java.lang.Exception) {
            ""
        }

        return if (a_isIgnoreCase) {
            if (result.equals(str, ignoreCase = true)) {
                null
            } else {
                result
            }
        } else {
            if (result == str) {
                null
            } else {
                result
            }
        }
    }

    /**
     * 임시 파일 생성
     */
    fun makeTempFile(sPath: String) {
        val sNewPath = sPath + System.currentTimeMillis()
        try {
            FileInputStream(sPath).use { inputStream ->
                FileOutputStream(sNewPath).use { outputStream ->
                    inputStream.channel.use { fcin ->
                        outputStream.channel.use { fcout ->
                            val size = fcin.size()
                            fcin.transferTo(0, size, fcout)
                            //BRAILLESNV-12982.20180129.JKH.USB/SD 제거 시, Sync 로 완전히 저장될 때까지 기다리도록 기능 추가함.
                            fcout.force(true)
                            outputStream.fd.sync()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            val tempFile = File(sNewPath)
            tempFile.delete()
            return
        }

        val original = File(sPath)
        original.delete()

        val trash = File(sNewPath)
        trash.renameTo(original)
        trash.delete()
    }

    /**
     * 동일한 이름의 파일이 존재할 경우 파일 이름에 prefix 를 붙여줌
     *
     * @param a_file 동일한 이름이 존재하는 파일
     * @return prefix 가 붙은 파일
     */
    fun getFileAddedPrefix(a_file: File, context: Context): File {
        val path = a_file.parent
        val name = a_file.name
        var srcFile = a_file
        val isSystemFile = name.startsWith(".")
        val resource = context.getString(R.string.FMS_MSG_COPY_DUPLICATE)

        for (i in 0 until Int.MAX_VALUE) {
            if (srcFile.exists() == false) {
                break
            }

            srcFile = if (isSystemFile == true) {
                File(path, ".$resource$i-" + name.substring(1))
            } else {
                File(path, "$resource$i-$name")
            }
        }
        return srcFile
    }

    // [17.02.02][YKWON] 실제 경로를 디스플레이용 경로로 변경해줌, 변경될 경로가 없다면 null 을 반환 (ex. /storage/usb1/ykwon -> USB/ykwon)
    fun getDisplayPath(context: Context, realPath: String, toEngRes: Boolean, ignoreCase: Boolean): String? {
        val availableDiskLinkedHashMap: Map<String, String> = getStorages(context)
        for ((key, value) in availableDiskLinkedHashMap) {
            if (realPath.startsWith(value) == true) {
                if (toEngRes == true) {
                    // [17.02.06][YKWON][BRAILLESNV-6647] 경로의 시작부분인 디스크명을 영어버전의 디스크명으로 통일시킴
                    val engKey: String = getEnglishName(context, key)
                    if (engKey.isEmpty() == false) {
                        return replaceFirst(realPath, value, engKey, ignoreCase)
                    }
                } else {
                    return replaceFirst(realPath, value, key, ignoreCase)
                }
            }
        }
        return null
    }

    // [20.02.04][bonita] 반복문에서 사용할 경우 성능저하로 인해 분리
    fun getDisplayPath(a_context: Context, a_availableDiskMap: Map<String, String>, a_realPath: String, a_toEngRes: Boolean, a_ignoreCase: Boolean): String? {
        val it = a_availableDiskMap.keys.iterator()
        while (it.hasNext() == true) {
            val key = it.next()
            val value = a_availableDiskMap[key]
            if (a_realPath.startsWith(value!!) == true) {
                if (a_toEngRes == true) {
                    // [17.02.06][YKWON][BRAILLESNV-6647] 경로의 시작부분인 디스크명을 영어버전의 디스크명으로 통일시킴
                    val engKey = getEnglishName(a_context, key)
                    if (engKey.isEmpty() == false) {
                        return replaceFirst(a_realPath, value, engKey, a_ignoreCase)
                    }
                } else {
                    return replaceFirst(a_realPath, value, key, a_ignoreCase)
                }
            }
        }
        return null
    }

    // [17.02.06][YKWON][BRAILLESNV-6647] 경로의 시작부분인 디스크명을 영어버전의 디스크명으로 통일시킴
    private fun getEnglishName(context: Context, key: String): String {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.ENGLISH)

        return when (key) {
            context.getString(R.string.FMS_CMD_FLASH_DISK) -> context.createConfigurationContext(config).getString(R.string.FMS_CMD_FLASH_DISK)
            context.getString(R.string.FMS_CMD_EXTEND_SD) -> context.createConfigurationContext(config).getString(R.string.FMS_CMD_EXTEND_SD)
            context.getString(R.string.FMS_CMD_EXTEND_SMD) -> context.createConfigurationContext(config).getString(R.string.FMS_CMD_EXTEND_SMD)
            context.getString(R.string.FMS_CMD_EXTEND_SMD2) -> context.createConfigurationContext(config).getString(R.string.FMS_CMD_EXTEND_SMD2)
            context.getString(R.string.FMS_CMD_EXTEND_SMD3) -> context.createConfigurationContext(config).getString(R.string.FMS_CMD_EXTEND_SMD3)
            context.getString(R.string.FMS_CMD_EXTEND_SMD4) -> context.createConfigurationContext(config).getString(R.string.FMS_CMD_EXTEND_SMD4)
            context.getString(R.string.FMS_CMD_EXTEND_SMD5) -> context.createConfigurationContext(config).getString(R.string.FMS_CMD_EXTEND_SMD5)
            context.getString(R.string.FMS_CMD_EXTEND_SMD6) -> context.createConfigurationContext(config).getString(R.string.FMS_CMD_EXTEND_SMD6)
            else -> ""
        }
    }

    /**
     * Directory 사이즈 반환
     */
    private fun getDirSize(file: File?): Long {
        var size = 0L
        if (file == null) {
            return size
        }

        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (f in files) {
                    size += getDirSize(f)
                }
            }
        } else {
            size += file.length()
        }
        return size
    }
}