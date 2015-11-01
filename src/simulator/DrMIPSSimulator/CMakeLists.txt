# Subdirectory
add_subdirectory(src)
if(DRMIPS_BUILD_TESTS)
	add_subdirectory(test)
endif()

# Configurations
set(PC_CPU_PATH "${CMAKE_SOURCE_DIR}/src/pc/${PROJECT_NAME}/cpu")
set(ANDROID_CPU_PATH "${CMAKE_SOURCE_DIR}/src/android/${PROJECT_NAME}/app/src/main/res/raw")

# Copy .cpu and .set files to the PC and Android projects
message(STATUS "Copying cpu/ to the PC and Android projects")
file(MAKE_DIRECTORY "${PC_CPU_PATH}")
file(MAKE_DIRECTORY "${ANDROID_CPU_PATH}")
file(GLOB FILES RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}/cpu" "cpu/*")
foreach(RES ${FILES})
	configure_file("cpu/${RES}" "${PC_CPU_PATH}/${RES}" COPYONLY)
	
	string(REGEX REPLACE "[\\.\\-]" "_" DST "${RES}") # replace '.' and '-' in the name with '_'
	configure_file("cpu/${RES}" "${ANDROID_CPU_PATH}/${DST}" COPYONLY)
endforeach()
message(STATUS "Copying cpu/ to the PC and Android projects -- done")
