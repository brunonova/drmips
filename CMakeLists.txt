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
	find_program(ANDROID_TOOL android)
	if(ANDROID_TOOL STREQUAL "ANDROID_TOOL-NOTFOUND")
		message(FATAL_ERROR "android tool not found!\nMake sure that:\n- The Android SDK is installed\n- The SDK's tools/ directory is in the PATH (or specify the path manually)")
	else()
		message(STATUS "Found android tool: ${ANDROID_TOOL}")
	endif()
	
	find_program(ANT_TOOL ant)
	if(ANT_TOOL STREQUAL "ANT_TOOL-NOTFOUND")
		message(FATAL_ERROR "ant tool not found!\nMake sure that the JDK and Ant are installed.")
	else()
		message(STATUS "Found ant: ${ANT_TOOL}")
		set(ANT_TOOL_FLAGS "-q" CACHE STRING "Flags passed to ant.")
		separate_arguments(ANT_TOOL_FLAGS)
	endif()
	
	find_program(JARSIGNER_TOOL jarsigner)
	if(JARSIGNER_TOOL STREQUAL "JARSIGNER_TOOL-NOTFOUND")
		message(FATAL_ERROR "jarsigner tool not found!\nMake sure that the JDK is installed.")
	else()
		message(STATUS "Found jarsigner: ${JARSIGNER_TOOL}")
		set(JARSIGNER_TOOL_FLAGS "-sigalg SHA1withRSA -digestalg SHA1" CACHE STRING "Flags passed to jarsigner.")
		separate_arguments(JARSIGNER_TOOL_FLAGS)
	endif()
	
	find_program(ZIPALIGN_TOOL zipalign)
	if(ZIPALIGN_TOOL STREQUAL "ZIPALIGN_TOOL-NOTFOUND")
		message(FATAL_ERROR "zipalign tool not found!\nMake sure that:\n- The Android SDK is installed\n- The SDK's tools/ directory is in the PATH (or specify the path manually)")
	else()
		message(STATUS "Found zipalign tool: ${ZIPALIGN_TOOL}")
		set(ZIPALIGN_TOOL_FLAGS "-f 4" CACHE STRING "Flags passed to zipalign.")
		separate_arguments(ZIPALIGN_TOOL_FLAGS)
	endif()
	
	mark_as_advanced(FORCE ANDROID_TOOL ANT_TOOL JARSIGNER_TOOL ZIPALIGN_TOOL
	                       ANT_TOOL_FLAGS JARSIGNER_TOOL_FLAGS ZIPALIGN_TOOL_FLAGS)
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
