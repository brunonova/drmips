# Install target
install(FILES index.html style.css DESTINATION "${DRMIPS_MANUALS_DIR}")
install(DIRECTORY img en pt DESTINATION "${DRMIPS_MANUALS_DIR}")

# Copy documentation files to DRMIPS_ZIP_BUILD_DIR/doc
message(STATUS "Copying documentation files to the ZIP build directory")
file(MAKE_DIRECTORY "${DRMIPS_ZIP_BUILD_DIR}/doc")
file(COPY index.html style.css img en pt DESTINATION "${DRMIPS_ZIP_BUILD_DIR}/doc")
message(STATUS "Copying documentation files to the ZIP build directory -- done")
