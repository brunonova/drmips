# Subdirectory
add_subdirectory(src)

# Configurations
set(DRMIPS_JAR_PATH "${CMAKE_INSTALL_PREFIX}/${DRMIPS_SHARE_DIR}/${PROJECT_NAME}.jar")
configure_file(drmips.in drmips)

# Copy distributable static files to DRMIPS_ZIP_BUILD_DIR
message(STATUS "PC version: copying cpu,lang,lib to the ZIP build directory")
file(COPY cpu lang lib DESTINATION "${DRMIPS_ZIP_BUILD_DIR}")
message(STATUS "PC version: copying cpu,lang,lib to the ZIP build directory -- done")

# Install target
install(PROGRAMS "${CMAKE_CURRENT_BINARY_DIR}/drmips" DESTINATION bin)
install(DIRECTORY cpu lang lib DESTINATION "${DRMIPS_SHARE_DIR}")
