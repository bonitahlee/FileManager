package com.bonita.filemanager.constants

/**
 * 파일매니저에서 사용하는 상수 모음
 *
 * @author bonita
 * @date 2021-11-18
 */
object FileManagerConstants {

    const val INVALID_VALUE = -1

    // [TODO] no all select 인 경우 -> root. picker 에서는 있을까?
    object INTENT {
        const val NO_ALL_SELECT = "no_all_select"
    }

    /**
     * 프래그먼트 전달 인자 관련
     */
    object FRAGMENT {
        const val TARGET_PATH = "targetPath"
        const val PICKER_MODE = "picker_mode"
        const val COPY_CUT_MODE = "copy_cut_mode"
        const val USER_FILE_CATEGORY_INFO = "user_file_category_info"
        const val IS_FIRST = "listFragment_first"
    }

    /**
     * 확장자에 따른 파일 타입
     */
    object FILE_TYPE {
        val MEDIA_FILES = arrayListOf("mp3", "wav", "ogg", "m4a", "flac", "aac", "3gp", "asf", "avi", "mp4", "mpg", "mid", "wma", "wmv", "asx", "m3u")
        val DAISY_FILES = arrayListOf("smil", "opf", "ncx")
        val BOOK_READER_FILES = arrayListOf("hwp", "txt", "doc", "docx", "pdf", "rtf", "epub", "xml", "bes", "bet")
        val ZIP_FILES = arrayListOf("zip")
        val VBF_FILES = arrayListOf("vbf")
        const val ALL_TYPE = "*.*"
    }

    object STORAGE {
        const val INTERNAL = "internal"
        const val FLASHDISK = "flashdisk"
        const val USB = "usb"
        const val SDCARD = "sdcard"

        const val PATH_FLASHDISK = "/storage/emulated/0"
        const val PATH_USB = "/mnt/media_rw/"
        const val PATH_USB_DEFAULT = "/storage/usb1"
        const val PATH_SD = "/storage/SDCARD"
    }
}