# Subdirectory
add_subdirectory(src)

# Configurations
configure_file(drmips.in drmips)

# Install target
install(PROGRAMS "${CMAKE_CURRENT_BINARY_DIR}/drmips" DESTINATION bin)
install(DIRECTORY cpu lang lib DESTINATION "${DRMIPS_SHARE_DIR}")
