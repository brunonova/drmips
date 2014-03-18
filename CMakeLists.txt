cmake_minimum_required(VERSION 2.8)
project(DrMIPS NONE)

# identify OS
#string(TOLOWER ${CMAKE_SYSTEM_NAME} OS_NAME)
#if(${OS_NAME} STREQUAL "windows")
#	set(WINDOWS 1)
#	message(STATUS "Compiling on Windows")
#elseif(${OS_NAME} STREQUAL "linux")
#	set(LINUX 1)
#	message(STATUS "Compiling on Linux")
#endif()

# Include required CMake modules
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake/Modules")
find_package(Java REQUIRED)
include(UseJava) # requires the version of the module and dependencies from CMake 3.0 (not yet released)
include(UseLATEX)

# Configurations
set(DRMIPS_SHARE_DIR "${CMAKE_INSTALL_PREFIX}/share/drmips")
#TODO Allow to choose whether to build PC version, Android version and Manuals

# Subdirectories
add_subdirectory(src/pc/DrMIPS)
add_subdirectory(doc/manuals)

# Uninstall target
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/cmake/cmake_uninstall.cmake.in"
               "${CMAKE_CURRENT_BINARY_DIR}/cmake/cmake_uninstall.cmake" IMMEDIATE @ONLY)
add_custom_target(uninstall COMMAND ${CMAKE_COMMAND} -P ${CMAKE_CURRENT_BINARY_DIR}/cmake/cmake_uninstall.cmake)

# make package
#include(InstallRequiredSystemLibraries)
#set(CPACK_PACKAGE_VERSION 1.0.0)
#set(CPACK_PACKAGE_VENDOR "Bruno Nova")
#set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "Desc")
#set(CPACK_PACKAGE_DESCRIPTION_FILE "${CMAKE_CURRENT_SOURCE_DIR}/description.txt")
#set(CPACK_RESOURCE_FILE_LICENSE "${CMAKE_CURRENT_SOURCE_DIR}/license.txt")
#include(CPack)
