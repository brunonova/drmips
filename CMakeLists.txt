cmake_minimum_required(VERSION 2.8)
project(DrMIPS NONE)

# Configurations
set(DRMIPS_SHARE_DIR "share/drmips")
set(DRMIPS_DOC_DIR "share/doc/drmips")
set(DRMIPS_DIST_BUILD_DIR "${CMAKE_BINARY_DIR}/dist")
set(DRMIPS_ZIP_BUILD_DIR "${DRMIPS_DIST_BUILD_DIR}/${PROJECT_NAME}")
option(DRMIPS_BUILD_MANUALS "Build and install ${PROJECT_NAME} manuals?" ON)
option(DRMIPS_BUILD_PC_VERSION "Build and install the PC version of the ${PROJECT_NAME} simulator?" ON)
option(DRMIPS_BUILD_PC_VERSION_JAVADOC "Build and install the Java API documentation of the PC version of the ${PROJECT_NAME} simulator? (DRMIPS_BUILD_PC_VERSION must be ON too)" OFF)
option(DRMIPS_BUILD_ANDROID_VERSION "Build and install the Android version of the ${PROJECT_NAME} simulator?" OFF)

# Include required CMake modules and programs
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake/Modules")
find_package(Java REQUIRED)
include(UseJava)

if(${DRMIPS_BUILD_ANDROID_VERSION})
	find_package(Android REQUIRED)
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
if(${DRMIPS_BUILD_ANDROID_VERSION})
	add_subdirectory(src/android/DrMIPS)
endif()
if(${DRMIPS_BUILD_MANUALS})
	add_subdirectory(doc/manuals)
endif()

# Uninstall target
configure_file("${CMAKE_CURRENT_SOURCE_DIR}/cmake/cmake_uninstall.cmake.in"
               "${CMAKE_CURRENT_BINARY_DIR}/cmake/cmake_uninstall.cmake" IMMEDIATE @ONLY)
add_custom_target(uninstall COMMAND ${CMAKE_COMMAND} -P ${CMAKE_CURRENT_BINARY_DIR}/cmake/cmake_uninstall.cmake)

# Distribution target
if(${DRMIPS_BUILD_PC_VERSION} AND ${DRMIPS_BUILD_MANUALS})
	add_custom_command(OUTPUT "${DRMIPS_DIST_BUILD_DIR}/${PROJECT_NAME}.zip"
	                   COMMAND "${Java_JAR_EXECUTABLE}" cfM "${DRMIPS_DIST_BUILD_DIR}/${PROJECT_NAME}.zip"
	                           "-C" "${DRMIPS_DIST_BUILD_DIR}" "${PROJECT_NAME}"
	                   DEPENDS pc_version manuals COMMENT "Creating ZIP file for distribution of the PC version")
	add_custom_target(dist DEPENDS "${DRMIPS_DIST_BUILD_DIR}/${PROJECT_NAME}.zip")
endif()

# Mark first CMake run as finished
set(DONE_FIRST_RUN ON CACHE INTERNAL "Has CMake been executed at least once already?")
