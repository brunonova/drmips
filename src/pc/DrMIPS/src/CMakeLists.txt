# Directory where Java classes are compiled to (should not be changed)
set(CLASS_DIR "${CMAKE_CURRENT_BINARY_DIR}${CMAKE_FILES_DIRECTORY}/${PROJECT_NAME}.dir")

# The manifest file (specifying the Main class and the libraries)
set(MANIFEST "${CMAKE_CURRENT_BINARY_DIR}/Manifest.mf")

# List of Java source files
set(SRC org/feup/brunonova/drmips/exceptions/InfiniteLoopException.java
        org/feup/brunonova/drmips/exceptions/InvalidCPUException.java
        org/feup/brunonova/drmips/exceptions/InvalidInstructionSetException.java
        org/feup/brunonova/drmips/exceptions/SyntaxErrorException.java
        org/feup/brunonova/drmips/gui/AssembledCodeTable.java
        org/feup/brunonova/drmips/gui/CodeEditor.java
        org/feup/brunonova/drmips/gui/DataMemoryTable.java
        org/feup/brunonova/drmips/gui/DatapathComponent.java
        org/feup/brunonova/drmips/gui/DatapathPanel.java
        org/feup/brunonova/drmips/gui/DlgAbout.java
        org/feup/brunonova/drmips/gui/DlgFindReplace.java
        org/feup/brunonova/drmips/gui/DlgLoading.java
        org/feup/brunonova/drmips/gui/DlgStatistics.java
        org/feup/brunonova/drmips/gui/DlgSupportedInstructions.java
        org/feup/brunonova/drmips/gui/DrMIPS.java
        org/feup/brunonova/drmips/gui/ExecTable.java
        org/feup/brunonova/drmips/gui/FrmSimulator.java
        org/feup/brunonova/drmips/gui/IOPortTip.java
        org/feup/brunonova/drmips/gui/Lang.java
        org/feup/brunonova/drmips/gui/RegistersTable.java
        org/feup/brunonova/drmips/gui/SupportedInstructionsTable.java
        org/feup/brunonova/drmips/gui/Util.java
        org/feup/brunonova/drmips/mips/AbstractInstruction.java
        org/feup/brunonova/drmips/mips/AssembledInstruction.java
        org/feup/brunonova/drmips/mips/Assembler.java
        org/feup/brunonova/drmips/mips/Component.java
        org/feup/brunonova/drmips/mips/components/Add.java
        org/feup/brunonova/drmips/mips/components/ALUControl.java
        org/feup/brunonova/drmips/mips/components/ALU.java
        org/feup/brunonova/drmips/mips/components/And.java
        org/feup/brunonova/drmips/mips/components/Concatenator.java
        org/feup/brunonova/drmips/mips/components/Constant.java
        org/feup/brunonova/drmips/mips/components/ControlUnit.java
        org/feup/brunonova/drmips/mips/components/DataMemory.java
        org/feup/brunonova/drmips/mips/components/Distributor.java
        org/feup/brunonova/drmips/mips/components/ExtendedALU.java
        org/feup/brunonova/drmips/mips/components/Fork.java
        org/feup/brunonova/drmips/mips/components/ForwardingUnit.java
        org/feup/brunonova/drmips/mips/components/HazardDetectionUnit.java
        org/feup/brunonova/drmips/mips/components/InstructionMemory.java
        org/feup/brunonova/drmips/mips/components/Multiplexer.java
        org/feup/brunonova/drmips/mips/components/Not.java
        org/feup/brunonova/drmips/mips/components/Or.java
        org/feup/brunonova/drmips/mips/components/PC.java
        org/feup/brunonova/drmips/mips/components/PipelineRegister.java
        org/feup/brunonova/drmips/mips/components/RegBank.java
        org/feup/brunonova/drmips/mips/components/ShiftLeft.java
        org/feup/brunonova/drmips/mips/components/SignExtend.java
        org/feup/brunonova/drmips/mips/components/SimpleBinaryOperationComponent.java
        org/feup/brunonova/drmips/mips/components/Xor.java
        org/feup/brunonova/drmips/mips/components/ZeroExtend.java
        org/feup/brunonova/drmips/mips/ControlALU.java
        org/feup/brunonova/drmips/mips/Control.java
        org/feup/brunonova/drmips/mips/CPU.java
        org/feup/brunonova/drmips/mips/Data.java
        org/feup/brunonova/drmips/mips/Input.java
        org/feup/brunonova/drmips/mips/Instruction.java
        org/feup/brunonova/drmips/mips/InstructionSet.java
        org/feup/brunonova/drmips/mips/InstructionType.java
        org/feup/brunonova/drmips/mips/IOPort.java
        org/feup/brunonova/drmips/mips/IsSynchronous.java
        org/feup/brunonova/drmips/mips/Output.java
        org/feup/brunonova/drmips/mips/PseudoInstruction.java
        org/feup/brunonova/drmips/util/Dimension.java
        org/feup/brunonova/drmips/util/Point.java
        org/json/JSONArray.java
        org/json/JSONException.java
        org/json/JSONObject.java
        org/json/JSONStringer.java
        org/json/JSONString.java
        org/json/JSONTokener.java
        org/json/JSONWriter.java)

# List of resource files
set(RES res/icons/icon_large.png
        res/icons/icon_medium.png
        res/icons/icon_small.png
        res/icons/icon_xlarge.png
        res/icons/icon_xsmall.png
        res/icons/x16/about.png
        res/icons/x16/assemble.png
        res/icons/x16/back_step.png
        res/icons/x16/copy.png
        res/icons/x16/cut.png
        res/icons/x16/dark_theme.png
        res/icons/x16/error.png
        res/icons/x16/exit.png
        res/icons/x16/help.png
        res/icons/x16/info.png
        res/icons/x16/load_cpu.png
        res/icons/x16/new.png
        res/icons/x16/open.png
        res/icons/x16/paste.png
        res/icons/x16/print.png
        res/icons/x16/redo.png
        res/icons/x16/reset_data.png
        res/icons/x16/restart.png
        res/icons/x16/run.png
        res/icons/x16/save_as.png
        res/icons/x16/save.png
        res/icons/x16/search.png
        res/icons/x16/select_all.png
        res/icons/x16/statistics.png
        res/icons/x16/step.png
        res/icons/x16/undo.png
        res/icons/x16/windows.png
        res/icons/x24/assemble.png
        res/icons/x24/back_step.png
        res/icons/x24/help.png
        res/icons/x24/info.png
        res/icons/x24/new.png
        res/icons/x24/open.png
        res/icons/x24/restart.png
        res/icons/x24/run.png
        res/icons/x24/save_as.png
        res/icons/x24/save.png
        res/icons/x24/statistics.png
        res/icons/x24/step.png)

# List of libraries (without the path)
set(LIBS autocomplete.jar      # v2.5.0
         JTattoo.jar           # v1.6.9
         rsyntaxtextarea.jar)  # v2.5.0

# Generate list of paths to the libraries for add_jar(), class-Path for Manifest.mf and class-Path for javadoc
set(LIBS_PATHS)
set(CLASS_PATH "Class-Path:")
set(CLASS_PATH_JAVADOC "")
foreach(LIB ${LIBS})
	list(APPEND LIBS_PATHS "../lib/${LIB}")
	set(CLASS_PATH "${CLASS_PATH} lib/${LIB}")
	if(CLASS_PATH_JAVADOC)
		set(CLASS_PATH_JAVADOC "${CLASS_PATH_JAVADOC}:")
	endif()
	set(CLASS_PATH_JAVADOC "${CLASS_PATH_JAVADOC}../lib/${LIB}")
endforeach()

# Generate Manifest file with the Class-Path
message(STATUS "PC version: generating Manifest.mf")
file(WRITE "${MANIFEST}" "${CLASS_PATH}\n")
message(STATUS "PC version: generating Manifest.mf -- done")

# Jar compilation target
add_jar(${PROJECT_NAME} ${SRC} ${RES} INCLUDE_JARS ${LIBS_PATHS} MANIFEST ${MANIFEST}
                        ENTRY_POINT org.feup.brunonova.drmips.gui.DrMIPS)
add_custom_target(pc_version DEPENDS ${PROJECT_NAME}) # add meta-target "pc_version"

# Javadoc target
if(DRMIPS_BUILD_PC_VERSION_JAVADOC)
	create_javadoc(${PROJECT_NAME} FILES ${SRC} CLASSPATH ${CLASS_PATH_JAVADOC}
	                               WINDOWTITLE "${PROJECT_NAME}" DOCTITLE "${PROJECT_NAME}"
	                               INSTALLPATH "${DRMIPS_DOC_DIR}/javadoc")
	add_custom_target(pc_version_javadoc DEPENDS "${PROJECT_NAME}_javadoc") # add meta-target "pc_version_javadoc"
endif()

# Install target
install_jar(${PROJECT_NAME} "${DRMIPS_SHARE_DIR}")

# Copy jar to DRMIPS_ZIP_BUILD_DIRECTORY
add_custom_command(TARGET "${PROJECT_NAME}" POST_BUILD
                  COMMAND ${CMAKE_COMMAND} -E copy_if_different
                          "${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}.jar" "${DRMIPS_ZIP_BUILD_DIR}/${PROJECT_NAME}.jar"
                  COMMENT "Copying ${PROJECT_NAME}.jar to the ZIP build directory"
                  VERBATIM) # copy PDF to DRMIPS_ZIP_BUILD_DIRECTORY/doc
