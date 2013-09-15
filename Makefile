# Makefile that compiles everything
#
# Run 'make' to compile the simulator (both versions) and the documentation
# Run 'make dist' to create the DrMIPS.zip and DrMIPS.apk files for distribution
#
# By default, the makefile will suppress the output of the commands executed by
# it. To print that output, run make with the 'VERBOSE=yes' argument.

MANUALS_DIR=doc/manuals
PC_DIR=src/pc/DrMIPS
ANDROID_DIR=src/android/DrMIPS
PROG_NAME=DrMIPS
TMP_DIR=$(PROG_NAME)
TMP_DOC_DIR=$(TMP_DIR)/doc
ZIP_FILE=$(PROG_NAME).zip
APK_FILE=$(PROG_NAME).apk
RM=rm -rf
MKDIR=mkdir
CP=cp -r
ZIP=zip -r9

# check if verbose
ifneq '$(VERBOSE)' 'yes'
ifneq '$(VERBOSE)' 'y'
ifneq '$(VERBOSE)' '1'
EXTRA_ARGS=> /dev/null
endif
endif
endif

.PHONY: all doc manuals simulator pc android dist
all: doc simulator
doc: manuals
simulator: pc android
dist: $(ZIP_FILE) $(APK_FILE)

manuals:
	@echo ' * Compiling manuals...'
	@(cd $(MANUALS_DIR) && make $(EXTRA_ARGS))

pc:
	@echo ' * Compiling PC version of the simulator...'
	@(cd $(PC_DIR) && make $(EXTRA_ARGS))

android:
	@echo ' * Compiling Android version of the simulator...'
	@(cd $(ANDROID_DIR) && make $(EXTRA_ARGS))

$(ZIP_FILE): pc manuals
	@echo ' * Creating $(ZIP_FILE)...'
	@$(RM) $(TMP_DIR) $(ZIP_FILE)
	@$(MKDIR) $(TMP_DIR) $(TMP_DOC_DIR)
	@$(CP) $(PC_DIR)/dist/$(PROG_NAME).jar $(PC_DIR)/dist/lib $(PC_DIR)/cpu\
		$(PC_DIR)/lang $(TMP_DIR)
	@$(CP) $(MANUALS_DIR)/*.pdf $(TMP_DOC_DIR)
	@$(ZIP) $(ZIP_FILE) $(TMP_DIR) $(EXTRA_ARGS)
	@$(RM) $(TMP_DIR)

$(APK_FILE): android
	@echo ' * Creating $(APK_FILE)...'
	@$(CP) $(ANDROID_DIR)/bin/$(APK_FILE) $(APK_FILE)

clean:
	@echo ' * Removing compiled files...'
	@(cd $(MANUALS_DIR) && make clean $(EXTRA_ARGS))
	@(cd $(PC_DIR) && make clean $(EXTRA_ARGS))
	@(cd $(ANDROID_DIR) && make clean $(EXTRA_ARGS))
	@$(RM) $(TMP_DIR) $(ZIP_FILE) $(APK_FILE)
