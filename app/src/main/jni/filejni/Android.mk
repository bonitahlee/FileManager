LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := HanFileJNI
LOCAL_SRC_FILES := FileJni.cpp
LOCAL_ARM_MODE := arm
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
