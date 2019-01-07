LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES += $(call all-java-files-under, java)

LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat \
                               android-support-v4 \
                               librecycle

LOCAL_PACKAGE_NAME := NetworkUsageData
LOCAL_CERTIFICATE := platform

LOCAL_MODULE_TAGS := optional
LOCAL_DEX_PREOPT:=true

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := librecycle:libs/android-support-v7-recyclerview.jar

include $(BUILD_MULTI_PREBUILT)
