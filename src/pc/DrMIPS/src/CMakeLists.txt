# Directory where Java classes are compiled to (should not be changed)
set(CLASS_DIR "${CMAKE_CURRENT_BINARY_DIR}${CMAKE_FILES_DIRECTORY}/${PROJECT_NAME}.dir")

# The manifest file (specifying the Main class and the libraries)
set(MANIFEST "${CMAKE_CURRENT_BINARY_DIR}/Manifest.mf")

# List of Java source files
file(GLOB_RECURSE SRC RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" "org/*.java" "com/jtattoo/plaf/*"
                               "org/fife/io/*" "org/fife/print/*" "org/fife/ui/*" "org/fife/util/*")

# List of resource files
file(GLOB_RECURSE RES RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" "res/*" "org/*.properties")

# List of libraries (without the path)
set(LIBS DrMIPSSimulator.jar)

# Generate list of paths to the libraries for add_jar(), class-Path for Manifest.mf and class-Path for javadoc
set(LIBS_PATHS)
set(CLASS_PATH "Class-Path:")
set(CLASS_PATH_JAVADOC "")
foreach(LIB ${LIBS})
	list(APPEND LIBS_PATHS "../lib/${LIB}")
	set(CLASS_PATH "${CLASS_PATH} lib/${LIB}")
	if(CLASS_PATH_JAVADOC)
		set(CLASS_PATH_JAVADOC "${CLASS_PATH_JAVADOC}:")
	endif()
	set(CLASS_PATH_JAVADOC "${CLASS_PATH_JAVADOC}../lib/${LIB}")
endforeach()

# Generate Manifest file with the Class-Path
message(STATUS "PC version: generating Manifest.mf")
file(WRITE "${MANIFEST}" "${CLASS_PATH}\n")
message(STATUS "PC version: generating Manifest.mf -- done")

# Jar compilation target
add_jar(${PROJECT_NAME} ${SRC} ${RES} INCLUDE_JARS ${LIBS_PATHS} MANIFEST ${MANIFEST}
                        ENTRY_POINT org.feup.brunonova.drmips.gui.DrMIPS)
add_custom_target(pc_version DEPENDS ${PROJECT_NAME}) # add meta-target "pc_version"
add_dependencies(${PROJECT_NAME} "${PROJECT_NAME}Simulator") # depend on the simulator project

# Install target
install_jar(${PROJECT_NAME} "${DRMIPS_SHARE_DIR}")

# Copy jar to DRMIPS_ZIP_BUILD_DIRECTORY
add_custom_command(TARGET "${PROJECT_NAME}" POST_BUILD
                  COMMAND ${CMAKE_COMMAND} -E copy_if_different
                          "${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}.jar" "${DRMIPS_ZIP_BUILD_DIR}/${PROJECT_NAME}.jar"
                  COMMENT "Copying ${PROJECT_NAME}.jar to the ZIP build directory"
                  VERBATIM)
