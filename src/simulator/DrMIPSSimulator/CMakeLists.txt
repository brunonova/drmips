# Subdirectory
add_subdirectory(src)

# Configurations
set(DRMIPS_SIMULATOR_JAR_PATH "${CMAKE_INSTALL_PREFIX}/${DRMIPS_SHARE_DIR}/lib/${PROJECT_NAME}Simulator.jar")

# Copy distributable static files to DRMIPS_ZIP_BUILD_DIR
#message(STATUS "PC version: copying cpu,lang,lib to the ZIP build directory")
#file(COPY cpu lang lib DESTINATION "${DRMIPS_ZIP_BUILD_DIR}")
#message(STATUS "PC version: copying cpu,lang,lib to the ZIP build directory -- done")

# Install target
#install(DIRECTORY cpu lang lib DESTINATION "${DRMIPS_SHARE_DIR}")
