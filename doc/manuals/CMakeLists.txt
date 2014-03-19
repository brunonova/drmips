# Configurations
if(NOT DONE_FIRST_RUN)
	# Allow in-source builds
	set(LATEX_OUTPUT_PATH "${CMAKE_CURRENT_BINARY_DIR}/build" CACHE STRING
	                      "Specifies the location to place LaTeX output." FORCE)
	# Reduce output of pdflatex by default
  set(PDFLATEX_COMPILER_FLAGS "-interaction=batchmode" CACHE STRING
                              "Flags passed to pdflatex." FORCE)
endif()

# TeX files (without the extension)
set(SRC configuration-en
        manual-en
        manual-pt_PT)

# Other resource files
set(RES listings/alu.txt
        listings/component.txt
        listings/control.txt
        listings/cpu.cpu
        listings/instruction.txt
        listings/pseudo.txt
        listings/set.set
        listings/type.txt
        listings/wire.txt)

# Manuals targets
add_custom_target(manuals) # add meta-target "manuals"
foreach(TEX ${SRC})
	add_latex_document("${TEX}.tex" INPUTS ${RES} DEFAULT_PDF MANGLE_TARGET_NAMES)
	add_dependencies(manuals "${TEX}_pdf")
endforeach()

# Install targets
foreach(TEX ${SRC})
	install(FILES "${LATEX_OUTPUT_PATH}/${TEX}.pdf" DESTINATION "${DRMIPS_SHARE_DIR}/doc")
endforeach()


