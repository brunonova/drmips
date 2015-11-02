# List of Java source files
file(GLOB_RECURSE SRC RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" org/*.java)

# Jar compilation target
add_jar("${PROJECT_NAME}Tests" ${SRC} INCLUDE_JARS "${PROJECT_NAME}Simulator" "${JUNIT_PATH}")
add_custom_target(tests DEPENDS "${PROJECT_NAME}Tests") # add meta-target "tests"
get_property(TEST_JAR_PATH TARGET "${PROJECT_NAME}Tests" PROPERTY JAR_FILE) # find path to built jar

# Add "test" target to run the unit tests
add_custom_target(test COMMAND "${Java_JAVA_EXECUTABLE}" -cp "${JUNIT_PATH}:${SIMULATOR_JAR_PATH}:${TEST_JAR_PATH}"
                               org.junit.runner.JUnitCore org.feup.brunonova.drmips.simulator.TestSuite
                       DEPENDS tests
                       COMMENT "Running tests...")
