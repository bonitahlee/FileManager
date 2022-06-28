package com.bonita.filemanager.data

import java.io.Serializable

class FileItem : Serializable, Cloneable {

    /**
     * 디스크인지, 폴더인지, 파일인지 판단
     */
    enum class TYPE { DISK, DIRECTORY, FILE }

    /**
     * 폴더 종류
     */
    enum class FolderType { UNKNOWN, MEDIA, DAISY, BOOK_READER, MIXED }

    var type = TYPE.FILE
    var folderType = FolderType.UNKNOWN

    var filePath = ""
    var fileName = ""
    var fileDate = ""
    var fileSize = 0L
}