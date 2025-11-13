cmake_minimum_required(VERSION 3.22.1)
project("edgeviewer")

set(OpenCV_DIR "C:/Users/Lenovo/Downloads/opencv-4.12.0-android-sdk/OpenCV-android-sdk/sdk/native/jni")

find_package(OpenCV REQUIRED)

add_library(
        edgeproc
        SHARED
        native-lib.cpp
)

target_include_directories(edgeproc PRIVATE
        ${OpenCV_INCLUDE_DIRS}
)

find_library(log-lib log)

target_link_libraries(edgeproc
        ${OpenCV_LIBS}
        ${log-lib})