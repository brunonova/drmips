# Configurations
if(NOT DONE_FIRST_RUN)
	# Allow in-source builds
	set(LATEX_OUTPUT_PATH "${CMAKE_CURRENT_BINARY_DIR}/build" CACHE STRING
	                      "Specifies the location to place LaTeX output." FORCE)
	# Reduce output of latex/pdflatex by default
	set(PDFLATEX_COMPILER_FLAGS "-interaction=batchmode" CACHE STRING
	                            "Flags passed to pdflatex." FORCE)
	set(LATEX_COMPILER_FLAGS "-interaction=batchmode" CACHE STRING
	                         "Flags passed to latex." FORCE)
endif()

# TeX files (without the extension)
set(SRC configuration-en configuracao-pt_PT manual-en manual-pt_PT)

# Other resource files
file(GLOB RES RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" "listings/*")

# Build and install targets
add_custom_target(manuals) # add meta-target "manuals"
foreach(TEX ${SRC})
	add_latex_document("${TEX}.tex" INPUTS ${RES} DEFAULT_PDF MANGLE_TARGET_NAMES) # build target
	add_dependencies(manuals "${TEX}_pdf") # add this document to "manuals" meta-target
	install(FILES "${LATEX_OUTPUT_PATH}/${TEX}.pdf" DESTINATION "${DRMIPS_MANUALS_DIR}") # install target
	add_custom_command(TARGET "${TEX}_pdf" POST_BUILD
	                   COMMAND ${CMAKE_COMMAND} -E copy_if_different
	                           "${LATEX_OUTPUT_PATH}/${TEX}.pdf" "${DRMIPS_ZIP_BUILD_DIR}/doc/${TEX}.pdf"
	                   COMMENT "Copying ${TEX}.pdf to the ZIP build directory"
	                   VERBATIM) # copy PDF to DRMIPS_ZIP_BUILD_DIRECTORY/doc
endforeach()
