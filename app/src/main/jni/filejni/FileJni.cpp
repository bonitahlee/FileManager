#define LOG_TAG "FileList"

#include "jni.h"
#include "android/log.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include <ctype.h>
#include <fcntl.h>

#ifndef ALOGE
#define ALOGE(...) \
    __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#endif
#ifndef ALOGI
#define ALOGI(...) \
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#endif

#ifndef NULL
#define NULL 0
#endif

struct FileEntry {
    char *name;
    char *ext;
    off_t size;
    time_t mtime;
};

#define FILE_LIST_DEFAULT_CAPICITY  4096
#define FILE_LIST_DEFAULT_INCREMENT 4096
class FileList {
public:
    FileList();
    ~FileList();

    static const int SORT_NONE       = 0;
    static const int SORT_BY_NAME    = 1;
    static const int SORT_BY_EXT     = 2;
    static const int SORT_BY_SIZE    = 3;
    static const int SORT_BY_MTIME   = 4;

    void load(const char *baseDir, bool loadDir, bool loadFile);
    void clear();

    char* getBaseDir() { return mBaseDir; };

    char* getDirName(int index) {
        if (index < 0 || index >= mDirOffset) return NULL;
        return mDirs[index].name;
    }
    int getDirSize() { return mDirOffset; }
    char* getFileName(int index) {
        if (index < 0 || index >= mFileOffset) return NULL;
        return mFiles[index].name;
    }
    int getFileSize() { return mFileOffset; }

    // sort
    void setSortOrder(int sortOrder) { mSortOrder = sortOrder; };
    // filter
    void setDirFilter(bool showAllDir, char **excludeDirs, int size);
    void setFileFilter(bool showHiddenFile, char **includeFileExts, int size);

    void getExt(struct FileEntry *entry);
    void stat(struct FileEntry *entry);
private:
    void add(struct FileEntry* &files, int &offset, int &size, char *name);
    void addDir(char *name);
    void addFile(char *name);

    char *mBaseDir;
    struct FileEntry *mDirs;
    int mDirOffset, mDirSize;
    struct FileEntry *mFiles;
    int mFileOffset, mFileSize;

    bool mShowAllDir;
    char **mExcludeDirNames;
    int mExcludeDirNameSize;

    bool mShowHiddenFile;
    char **mIncludeFileExts;
    int mIncludeFileExtSize;

    int mSortOrder;
};

static void* compareArg = NULL;
static int compareName(const void *p1, const void *p2)
{
    struct FileEntry *f1 = (struct FileEntry*)p1;
    struct FileEntry *f2 = (struct FileEntry*)p2;

    if (isdigit(f1->name[0]) && isdigit(f2->name[0])) {
        unsigned long int v1, v2;
        char *endptr;
        v1 = strtoul(f1->name, &endptr, 10);
        v2 = strtoul(f2->name, &endptr, 10);
        if (v1 != ULONG_MAX && v2 != ULONG_MAX) {
            int v = (int)(v1 - v2);
            if (v != 0) return v;
        }
    }


//[22.02.25][JANGKH][BRAILLESB-2063] 파일 이름 정렬 시, 숫자가 끝에 오는 경우, 정렬 개선.
    int result = strcasecmp(f1->name, f2->name);
    if (result > 1 && strlen(f1->name) < strlen(f2->name)) {
        int length = strlen(f1->name);
        for (int find = 0; find < length; find++) {
            if (f1->name[find] != f2->name[find]) {
                if (isdigit(f1->name[find]) == true && isdigit(f2->name[find]) == true && isdigit(f2->name[find + 1]) == true && (find + 1 == length || f1->name[find + 1] == '.')) {
                    //__android_log_print(ANDROID_LOG_INFO, "FILEINI", "Case 1 f1->name=[%s]", f1->name);
                    //__android_log_print(ANDROID_LOG_INFO, "FILEINI", "Case 1 f2->name=[%s]", f2->name);
                    return -1;
                }
                if (isdigit(f1->name[find]) == true && isdigit(f2->name[find]) == true) {
                    continue;
                }
                break;
            }
        }
    }

    //[22.02.25][JANGKH][BRAILLESB-2063] 파일 이름 정렬 시, 숫자가 끝에 오는 경우, 정렬 개선.
    if (result < 0 && strlen(f1->name) > strlen(f2->name)) {
        int length = strlen(f2->name);
        for (int find = 0; find < length; find++) {
            if (f1->name[find] != f2->name[find]) {
                if (isdigit(f1->name[find]) == true && isdigit(f2->name[find]) == true && isdigit(f1->name[find + 1]) == true && (find + 1 == length || f2->name[find + 1] == '.')) {
                    //__android_log_print(ANDROID_LOG_INFO, "FILEINI", "Case 2 f1->name=[%s]", f1->name);
                    //__android_log_print(ANDROID_LOG_INFO, "FILEINI", "Case 2 f2->name=[%s]", f2->name);
                    return 1;
                }
                if (isdigit(f1->name[find]) == true && isdigit(f2->name[find]) == true) {
                    continue;
                }
                break;
            }
        }
    }

       /*
    __android_log_print(ANDROID_LOG_INFO, "FILEINI", "Case 3 f1->name=[%s]", f1->name);
    __android_log_print(ANDROID_LOG_INFO, "FILEINI", "Case 3 f2->name=[%s]", f2->name);
    __android_log_print(ANDROID_LOG_INFO, "FILEINI", "Case 3 result=[%d]", result);
    */
    return result;
}

static int compareExt(const void *p1, const void *p2)
{
    struct FileEntry *f1 = (struct FileEntry*)p1;
    struct FileEntry *f2 = (struct FileEntry*)p2;
    FileList *fileList = (FileList*)compareArg;
    int v;

    fileList->getExt(f1);
    fileList->getExt(f2);
    v = strcasecmp(f1->ext, f2->ext);
    if (v == 0) return compareName(p1, p2);
    return v;
}

static int compareSize(const void *p1, const void *p2)
{
    struct FileEntry *f1 = (struct FileEntry*)p1;
    struct FileEntry *f2 = (struct FileEntry*)p2;
    FileList *fileList = (FileList*)compareArg;
    int v;
    fileList->stat(f1);
    fileList->stat(f2);
    v = f1->size - f2->size;
    if (v == 0) return compareName(p1, p2);
    return v;
}

static int compareMtime(const void *p1, const void *p2)
{
    struct FileEntry *f1 = (struct FileEntry*)p1;
    struct FileEntry *f2 = (struct FileEntry*)p2;
    FileList *fileList = (FileList*)compareArg;
    int v;
    fileList->stat(f1);
    fileList->stat(f2);
    v = f1->mtime - f2->mtime;
    if (v == 0) return compareName(p1, p2);
    return v;
}

FileList::FileList()
{
    mBaseDir = NULL;
    mDirOffset = 0;
    mDirSize = 0;
    mDirs = NULL;
    mFileOffset = 0;
    mFileSize = 0;
    mFiles = NULL;

    mShowAllDir = true;
    mExcludeDirNames = NULL;
    mExcludeDirNameSize = 0;

    mShowHiddenFile = true;
    mIncludeFileExts = NULL;
    mIncludeFileExtSize = 0;

    mSortOrder = SORT_NONE;
}

FileList::~FileList()
{
    clear();
}

void FileList::load(const char *baseDir, bool loadDir, bool loadFile)
{
    clear();

    if (!loadDir && !loadFile) return;

    if (baseDir[strlen(baseDir) - 1] != '/') {
        char path[512];
        strcpy(path, baseDir);
        strcat(path, "/");
        mBaseDir = strdup(path);
    } else {
        mBaseDir = strdup(baseDir);
    }

    DIR *dir;
    struct dirent *ent;
    dir = opendir(baseDir);
    if (dir == NULL) return;

    while (1) {
        ent = readdir(dir);
        if (ent == NULL) break;
        if (ent->d_type == DT_DIR) {
            if (loadDir) addDir(ent->d_name);
        } else {
            if (loadFile) addFile(ent->d_name);
        }
    }
    closedir(dir);

    int (*compFunc)(const void*, const void*) = NULL;
    switch (mSortOrder) {
    case SORT_BY_NAME:  compFunc = compareName;  break;
    case SORT_BY_EXT:   compFunc = compareExt;   break;
    case SORT_BY_SIZE:  compFunc = compareSize;  break;
    case SORT_BY_MTIME: compFunc = compareMtime; break;
    }
    if (compFunc == NULL) return;

    // FIXME: compareArg is global var
    compareArg = (void*)this;
    if (loadDir) {
        qsort(mDirs, mDirOffset, sizeof(struct FileEntry), compFunc);
    }
    if (loadFile) {
        qsort(mFiles, mFileOffset, sizeof(struct FileEntry), compFunc);
    }
}

void FileList::clear()
{
    int i;

    if (mBaseDir) free(mBaseDir);
    mBaseDir = NULL;

    for (i = 0; i < mDirOffset; i++) free(mDirs[i].name);
    if (mDirs != NULL) free(mDirs);
    mDirOffset = 0;
    mDirSize = 0;
    mDirs = NULL;

    for (i = 0; i < mFileOffset; i++) free(mFiles[i].name);
    if (mFiles != NULL) free(mFiles);
    mFileOffset = 0;
    mFileSize = 0;
    mFiles = NULL;
}

void FileList::setDirFilter(bool showAllDir, char **excludeDirs, int size)
{
    mShowAllDir = showAllDir;
    if (mShowAllDir) return;

    if (excludeDirs == NULL || size == 0) {
        mExcludeDirNames = NULL;
        mExcludeDirNameSize = 0;
        return;
    }
    mExcludeDirNames = excludeDirs;
    mExcludeDirNameSize = size;
    /*
    mExcludeDirNames = (char**)malloc(sizeof(char*) * size);
    mExcludeDirNameSize = size;
    for (int i = 0; i < size; i++) {
        mExcludeDirNames[i] = strdup(excludeDirs[i]);
    }
    */
}

void FileList::setFileFilter(bool showHiddenFile, char **includeFileExts, int size)
{
    mShowHiddenFile = showHiddenFile;

    if (includeFileExts == NULL || size == 0) {
        mIncludeFileExts = NULL;
        mIncludeFileExtSize = 0;
        return;
    }
    mIncludeFileExts = includeFileExts;
    mIncludeFileExtSize = size;
    /*
    mIncludeFileExts = (char**)malloc(sizeof(char*) * size);
    mIncludeFileExtSize = size;
    for (int i = 0; i < size; i++) {
        mIncludeFileExts[i] = strdup(includeFileExts[i]);
    }
    */
}

void FileList::getExt(struct FileEntry *entry)
{
    if (entry->ext) return;

    char *ext = strrchr(entry->name, '.');
    if (ext != NULL && strrchr(entry->name, '/') < ext) entry->ext = ext + 1;
    else entry->ext = (char*)"";
}

void FileList::stat(struct FileEntry *entry)
{
    if (entry->size >= 0) return;

    struct stat buf;
    char path[512];

    entry->size = 0;
    entry->mtime = 0;   
    strcpy(path, mBaseDir);
    strcat(path, entry->name);
    ::stat(path, &buf);
    entry->size = buf.st_size;
    entry->mtime = buf.st_mtime;
}

void FileList::add(struct FileEntry* &files, int &offset, int &size, char *name)
{
    if (files == NULL || offset >= size) {
        if (files == NULL) size = FILE_LIST_DEFAULT_CAPICITY;
        else size += FILE_LIST_DEFAULT_INCREMENT;
        files = (struct FileEntry*)realloc(files,
                                           sizeof(struct FileEntry) * size);
    }
    struct FileEntry *entry = &files[offset++];
    entry->name  = strdup(name);
    entry->ext   = NULL;
    entry->size  = -1;
    entry->mtime = -1;
    //if (mSortOrder == SORT_BY_SIZE ||
    //mSortOrder == SORT_BY_MTIME) stat(entry);
}

void FileList::addDir(char *name)
{
    // remove current & parent dir
    if (!strcmp(name, ".") || !strcmp(name, "..")) return;

    if (!mShowAllDir) {
        char fullPath[512];
        if (name[0] == '.') return; // hidden dir
        strcpy(fullPath, mBaseDir);
        strcat(fullPath, name);
        for (int i = 0; i < mExcludeDirNameSize; i++) {
            if (!strcmp(mExcludeDirNames[i], fullPath)) return;
        }
    }
    add(mDirs, mDirOffset, mDirSize, name);
}

void FileList::addFile(char *name)
{
    if (!mShowHiddenFile) {
        if (name[0] == '.') return; // hidden file
        // skip HanMediaPlayer db file
        else if (strstr(name, "mediaInfoList.db") == name ||
                 strstr(name, "mediaMarkInfo.db") == name) return;
    }

    if (mIncludeFileExts == NULL || mIncludeFileExtSize == 0) {
        add(mFiles, mFileOffset, mFileSize, name);
    } else {
        char *ext = strrchr(name, '.');
        if (ext != NULL && strrchr(name, '/') < ext) ext = ext + 1;
        else ext = NULL;

        if (ext != NULL) {
            for (int i = 0; i < mIncludeFileExtSize; i++) {
                /* PYW 20180903, 파일 확장자를 대소문자 구분 없이 비교하도록 수정 */
                if (!strcasecmp(mIncludeFileExts[i], ext)) {
                //if (!stricmp(mIncludeFileExts[i], ext)) {
                    add(mFiles, mFileOffset, mFileSize, name);
                    return;
                }
            }
        }
    }
}

static jclass mFileClass;
static jmethodID mFileCtor;

static char **mSystemFolders = NULL;
static int mSystemFolderSize = 0;

static void initSystemFolders(JNIEnv *env)
{
    if (mSystemFolders != NULL) return;

    jclass clazz;
    jfieldID fieldId;
    jobjectArray systemFolders;

    clazz = env->FindClass("com/jawon/han/filemanager/utils/HanFileJNI");
    fieldId = env->GetStaticFieldID(clazz, "SYSTEM_FOLDERS",
                                    "[Ljava/lang/String;");
    systemFolders = (jobjectArray)env->GetStaticObjectField(clazz, fieldId);

    if (systemFolders != NULL) {
        int i, size = env->GetArrayLength(systemFolders);
        mSystemFolders = (char**)malloc(sizeof(char*) * size);
        mSystemFolderSize = size;
        for (i = 0; i < size; i++) {
            jstring str = (jstring)env->GetObjectArrayElement(systemFolders, i);
            const char *folder = env->GetStringUTFChars(str, NULL);
            mSystemFolders[i] = strdup(folder);
            //ALOGI("mSystemFolders[%d]: %s", i, folder);
            env->ReleaseStringUTFChars(str, folder);
        }
    }
}

static jobject nativeGetDirList(JNIEnv *env, jobject obj,
                                jstring jDir, jboolean showSystemFolder)
{
    FileList fileList;

    fileList.setSortOrder(FileList::SORT_BY_NAME);
    fileList.setDirFilter(showSystemFolder, mSystemFolders, mSystemFolderSize);

    const char *baseDir = env->GetStringUTFChars(jDir, NULL);
    fileList.load(baseDir, true, false);
    env->ReleaseStringUTFChars(jDir, baseDir);

    jobjectArray fileArray;
    fileArray = env->NewObjectArray(fileList.getDirSize(), mFileClass, NULL);

    int i, size;
    size = fileList.getDirSize();
    for (i = 0; i < size; i++) {
        jstring path = env->NewStringUTF(fileList.getDirName(i));
        //ALOGI("getDirName(%d): %s", i, fileList.getDirName(i));
        jobject fileObj = env->NewObject(mFileClass, mFileCtor, jDir, path);
        env->SetObjectArrayElement(fileArray, i, fileObj);

        env->DeleteLocalRef(path);
        env->DeleteLocalRef(fileObj);
    }

    return fileArray;
}

static jobject nativeGetFileList(JNIEnv *env, jobject obj,
                                 jstring jDir, int sortOrder, jboolean showSystemFolder, jobjectArray jExts)
{
    FileList fileList;
    char **includeExts = NULL;
    int includeExtSize = 0;

    fileList.setSortOrder(sortOrder);
    fileList.setDirFilter(showSystemFolder, mSystemFolders, mSystemFolderSize);
    if (jExts != NULL) {
        int i;
        includeExtSize = env->GetArrayLength(jExts);
        includeExts = (char**)malloc(sizeof(char*) * includeExtSize);
        for (i = 0; i < includeExtSize; i++) {
            jstring str = (jstring)env->GetObjectArrayElement(jExts, i);
            const char *ext = env->GetStringUTFChars(str, NULL);
            includeExts[i] = strdup(ext);
            //ALOGI("jExts[%d]: %s", i, ext);
            env->ReleaseStringUTFChars(str, ext);
        }
    }
    fileList.setFileFilter(showSystemFolder, includeExts, includeExtSize);

    const char *baseDir = env->GetStringUTFChars(jDir, NULL);
    fileList.load(baseDir, true, true);
    env->ReleaseStringUTFChars(jDir, baseDir);

    // free includeExts after load()
    if (includeExts != NULL) {
        int i;
        for (i = 0; i < includeExtSize; i++) free(includeExts[i]);
        free(includeExts);
    }

    jobjectArray fileArray;
    fileArray = env->NewObjectArray(fileList.getDirSize() + fileList.getFileSize(), mFileClass, NULL);

    int i, size;
    size = fileList.getDirSize();
    for (i = 0; i < size; i++) {
        jstring path = env->NewStringUTF(fileList.getDirName(i));
        //ALOGI("getDirName(%d): %s", i, fileList.getDirName(i));
        jobject fileObj = env->NewObject(mFileClass, mFileCtor, jDir, path);
        env->SetObjectArrayElement(fileArray, i, fileObj);

        env->DeleteLocalRef(path);
        env->DeleteLocalRef(fileObj);
    }

    int after = size;
    size = fileList.getFileSize();
    for (i = 0; i < size; i++) {
        jstring path = env->NewStringUTF(fileList.getFileName(i));
        //ALOGI("getFileName(%d): %s", i, fileList.getFileName(i));
        jobject fileObj = env->NewObject(mFileClass, mFileCtor, jDir, path);
        env->SetObjectArrayElement(fileArray, after + i, fileObj);

        env->DeleteLocalRef(path);
        env->DeleteLocalRef(fileObj);
    }

    return fileArray;
}

static jobject nativeGetFileListWithoutDir(JNIEnv *env, jobject obj,
                                           jstring jDir, int sortOrder, jboolean showHiddenFile, jobjectArray jExts)
{
    FileList fileList;
    char **includeExts = NULL;
    int includeExtSize = 0;

    fileList.setSortOrder(sortOrder);
    //fileList.setDirFilter(showSystemFolder, mSystemFolders, mSystemFolderSize);
    if (jExts != NULL) {
        int i;
        includeExtSize = env->GetArrayLength(jExts);
        includeExts = (char**)malloc(sizeof(char*) * includeExtSize);
        for (i = 0; i < includeExtSize; i++) {
            jstring str = (jstring)env->GetObjectArrayElement(jExts, i);
            const char *ext = env->GetStringUTFChars(str, NULL);
            includeExts[i] = strdup(ext);
            //ALOGI("jExts[%d]: %s", i, ext);
            env->ReleaseStringUTFChars(str, ext);
        }
    }
    fileList.setFileFilter(showHiddenFile, includeExts, includeExtSize);

    const char *baseDir = env->GetStringUTFChars(jDir, NULL);
    fileList.load(baseDir, false, true);
    env->ReleaseStringUTFChars(jDir, baseDir);

    // free includeExts after load()
    if (includeExts != NULL) {
        int i;
        for (i = 0; i < includeExtSize; i++) free(includeExts[i]);
        free(includeExts);
    }

    jobjectArray fileArray;
    fileArray = env->NewObjectArray(fileList.getFileSize(), mFileClass, NULL);

    int i, size;
    size = fileList.getFileSize();
    for (i = 0; i < size; i++) {
        jstring path = env->NewStringUTF(fileList.getFileName(i));
        //ALOGI("getFileName(%d): %s", i, fileList.getFileName(i));
        jobject fileObj = env->NewObject(mFileClass, mFileCtor, jDir, path);
        env->SetObjectArrayElement(fileArray, i, fileObj);

        env->DeleteLocalRef(path);
        env->DeleteLocalRef(fileObj);
    }

    return fileArray;
}

static JNINativeMethod mMethodTable[] = {
    { "nativeGetDirList", "(Ljava/lang/String;Z)[Ljava/io/File;", (void*)nativeGetDirList },
    { "nativeGetFileList", "(Ljava/lang/String;IZ[Ljava/lang/String;)[Ljava/io/File;", (void*)nativeGetFileList },
    { "nativeGetFileListWithoutDir", "(Ljava/lang/String;IZ[Ljava/lang/String;)[Ljava/io/File;", (void*)nativeGetFileListWithoutDir },
};

extern "C" jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env = NULL;
    jint result = -1;
    jclass clazz;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("GetEnv failed!");
        return -1;
    }

    clazz = env->FindClass("java/io/File");
    if (clazz == NULL) {
        ALOGE("Cannot find java.io.File class");
        return -1;
    }
    mFileClass = (jclass)env->NewGlobalRef(clazz);
    mFileCtor = env->GetMethodID(clazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (mFileCtor == NULL) {
        ALOGE("Cannot find <init> method");
        return -1;
    }

    clazz = env->FindClass("com/jawon/han/filemanager/utils/HanFileJNI");
    result = env->RegisterNatives(clazz,
                         mMethodTable,
                         sizeof(mMethodTable) / sizeof(*mMethodTable));

    initSystemFolders(env);

    return JNI_VERSION_1_4;
}

/*
int main(int argc, char **argv) {
    static char *exts[] = { "2", "3" };
    FileList fileList;
    fileList.setSortOrder(FileList::SORT_BY_NAME);
    fileList.setFileFilter(false, exts, sizeof(exts) / sizeof(*exts));
    fileList.load("/usr/lib");
    fileList.print();
}
*/
