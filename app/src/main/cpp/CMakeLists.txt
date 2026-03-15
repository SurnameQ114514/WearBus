cmake_minimum_required(VERSION 3.4.1)

# 创建共享库
add_library(
    secure-keys
    SHARED
    secure-keys.cpp
)

# 链接库
find_library(
    log-lib
    log
)

target_link_libraries(
    secure-keys
    ${log-lib}
)