cmake_minimum_required(VERSION 3.10.2)
project("chibimo")
add_library(chibimo SHARED chibimo.c)
include_directories(/home/leonsch/Android/Sdk/ndk/21.4.7075529/sysroot/usr/include /usr/include)
target_link_libraries(chibimo ${PROJECT_SOURCE_DIR}/../libs/${ANDROID_ABI}/libsodium.so)