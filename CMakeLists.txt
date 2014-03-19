cmake_minimum_required(VERSION 2.8)
project(DrMIPS NONE)

# Configurations
set(DRMIPS_SHARE_DIR "${CMAKE_INSTALL_PREFIX}/share/drmips")
set(DRMIPS_DOC_DIR "${CMAKE_INSTALL_PREFIX}/share/doc/drmips")
option(DRMIPS_BUILD_MANUALS "Build and install ${PROJECT_NAME} manuals?" ON)
option(DRMIPS_BUILD_PC_VERSION "Build and install the PC version of the ${PROJECT_NAME} simulator?" ON)
option(DRMIPS_BUILD_PC_VERSION_JAVADOC "Build and install the Java API documentation of the PC version of the ${PROJECT_NAME} simulator? (DRMIPS_BUILD_PC_VERSION must be ON too)" OFF)
option(DRMIPS_BUILD_ANDROID_VERSION "Build and install the Android version of the ${PROJECT_NAME} simulator?" OFF)

# Include required CMake modules
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake/Modules")

if(${DRMIPS_BUILD_PC_VERSION})
	find_package(Java REQUIRED)
	include(UseJava)
endif()

if(${DRMIPS_BUILD_MANUALS})
	include(UseLATEX)
	# Mark several LaTeX options as advanced
	mark_as_advanced(FORCE BIBTEX_COMPILER
	                       DVIPS_CONVERTER
	                       HTLATEX_CONVERTER
	                       IMAGEMAGICK_CONVERT
	                       LATEX2HTML_CONVERTER
	                       LATEX_COMPILER
	                       MAKEINDEX_COMPILER
	                       PDFLATEX_COMPILER
	                       PDFTOPS_CONVERTER
	                       PS2PDF_CONVERTER
	                       XINDY_COMPILER
	                       LATEX_OUTPUT_PATH)
endif()

# Subdirectories
if(${DRMIPS_BUILD_PC_VERSION})
	add_subdirectory(src/pc/DrMIPS)
endif()
if(${DRMIPS_BUILD_MANUALS})
	add_subdirectory(doc/manuals)
endif()

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

# Mark first CMake run as finished
set(DONE_FIRST_RUN ON CACHE INTERNAL "Has CMake been executed at least once already?")
