# List of Java source files
file(GLOB_RECURSE SRC RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" org/*.java)

# Jar compilation target
add_jar("${PROJECT_NAME}Simulator" ${SRC})
add_custom_target(simulator DEPENDS "${PROJECT_NAME}Simulator") # add meta-target "simulator"
get_property(SIMULATOR_JAR_PATH TARGET "${PROJECT_NAME}Simulator" PROPERTY JAR_FILE) # find path to built jar
set(SIMULATOR_JAR_PATH "${SIMULATOR_JAR_PATH}" PARENT_SCOPE) # make variable available in parent scope

# Javadoc target
if(DRMIPS_BUILD_SIMULATOR_JAVADOC)
	create_javadoc(${PROJECT_NAME} FILES ${SRC}
	                               WINDOWTITLE "${PROJECT_NAME}" DOCTITLE "${PROJECT_NAME}"
	                               INSTALLPATH "${DRMIPS_JAVADOC_DIR}")
	add_custom_target(simulator_javadoc DEPENDS "${PROJECT_NAME}_javadoc") # add meta-target "simulator_javadoc"
endif()

# Copy jar to the lib/ directory of the PC version project
add_custom_command(TARGET "${PROJECT_NAME}Simulator" POST_BUILD
                   COMMAND ${CMAKE_COMMAND} -E copy_if_different
                           "${SIMULATOR_JAR_PATH}"
                           "${CMAKE_SOURCE_DIR}/src/pc/${PROJECT_NAME}/lib/${PROJECT_NAME}Simulator.jar"
                   COMMENT "Copying ${PROJECT_NAME}Simulator.jar to the PC version project lib/ directory"
                   VERBATIM)

# Copy jar to the libs/ directory of the Android version project
add_custom_command(TARGET "${PROJECT_NAME}Simulator" POST_BUILD
                   COMMAND ${CMAKE_COMMAND} -E copy_if_different
                           "${SIMULATOR_JAR_PATH}"
                           "${CMAKE_SOURCE_DIR}/src/android/${PROJECT_NAME}/app/libs/${PROJECT_NAME}Simulator.jar"
                   COMMENT "Copying ${PROJECT_NAME}Simulator.jar to the Android version project libs/ directory"
                   VERBATIM)

# Copy jar to DRMIPS_ZIP_BUILD_DIRECTORY
add_custom_command(TARGET "${PROJECT_NAME}Simulator" POST_BUILD
                   COMMAND ${CMAKE_COMMAND} -E copy_if_different
                           "${SIMULATOR_JAR_PATH}"
                           "${DRMIPS_ZIP_BUILD_DIR}/lib/${PROJECT_NAME}Simulator.jar"
                   COMMENT "Copying ${PROJECT_NAME}Simulator.jar to the ZIP build directory"
                   VERBATIM)

# Add copied DrMIPSSimulator.jar files to the "clean" target
set_directory_properties(PROPERTIES ADDITIONAL_MAKE_CLEAN_FILES
                         "${CMAKE_SOURCE_DIR}/src/pc/${PROJECT_NAME}/lib/${PROJECT_NAME}Simulator.jar;${CMAKE_SOURCE_DIR}/src/android/${PROJECT_NAME}/app/libs/${PROJECT_NAME}Simulator.jar") 
