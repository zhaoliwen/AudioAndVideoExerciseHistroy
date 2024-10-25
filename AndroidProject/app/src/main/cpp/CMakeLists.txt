cmake_minimum_required(VERSION 3.4.1)

add_library(my_native_lib SHARED my_native_lib.cpp)

find_library(log-lib log)

target_link_libraries(my_native_lib ${log-lib})
