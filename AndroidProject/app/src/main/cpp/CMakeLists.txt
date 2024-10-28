cmake_minimum_required(VERSION 3.4.1)
include_directories(include)

#添加系统环境变量
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}")
add_library(my_native_lib SHARED my_native_lib.cpp)

find_library(log-lib log)

target_link_libraries(my_native_lib
        avcodec
        avdevice
        avfilter
        avformat
        avutil
        swresample
        swscale
        # Links the target library to the log library
        # included in the NDK.
        android
        ${log-lib})
