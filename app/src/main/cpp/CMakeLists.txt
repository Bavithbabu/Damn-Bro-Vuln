cmake_minimum_required(VERSION 3.22.1)
project(vuln LANGUAGES C)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -g")

add_library(vuln SHARED
        vuln_native.c)

find_library(log-lib log)

target_link_libraries(vuln ${log-lib})
