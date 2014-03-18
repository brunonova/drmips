# Configurations
set(LATEX_OUTPUT_PATH "${CMAKE_CURRENT_BINARY_DIR}/latex_build") # to allow in-source builds

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
foreach(TEX ${SRC})
	add_latex_document("${TEX}.tex" INPUTS ${RES} DEFAULT_PDF MANGLE_TARGET_NAMES)
endforeach()

# Install targets
foreach(TEX ${SRC})
	install(FILES "${LATEX_OUTPUT_PATH}/${TEX}.pdf" DESTINATION "${DRMIPS_SHARE_DIR}/doc")
endforeach()
