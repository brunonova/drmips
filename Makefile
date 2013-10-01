# Makefile that compiles everything
#
# Run 'make' to compile the simulator (both versions) and the documentation
# Run 'make dist' to create the DrMIPS.zip and DrMIPS.apk files for distribution
#
# By default, the makefile will suppress the output of the commands executed by
# it. To print that output, run make with the 'VERBOSE=yes' argument.
#
# Warning: the PC project must be opened in Netbeans at least once for the
# makefile to work correctly!

MANUALS_DIR=doc/manuals
PC_DIR=src/pc/DrMIPS
ANDROID_DIR=src/android/DrMIPS
PROG_NAME=DrMIPS
TMP_DIR=$(PROG_NAME)
TMP_DOC_DIR=$(TMP_DIR)/doc
ZIP_FILE=$(PROG_NAME).zip
APK_FILE=$(PROG_NAME).apk
RM=rm -rf
MV=mv
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

.PHONY: all doc manuals simulator release pc android android-release dist sign-apk
all: doc simulator
doc: manuals
simulator: pc android
release: pc android-release
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

android-release:
	@echo ' * Compiling Android (release) version of the simulator...'
	@(cd $(ANDROID_DIR) && make release $(EXTRA_ARGS))

$(ZIP_FILE): pc manuals
	@echo ' * Creating $(ZIP_FILE)...'
	@$(RM) $(TMP_DIR) $(ZIP_FILE)
	@$(MKDIR) $(TMP_DIR) $(TMP_DOC_DIR)
	@$(CP) $(PC_DIR)/dist/$(PROG_NAME).jar $(PC_DIR)/dist/lib $(PC_DIR)/cpu\
		$(PC_DIR)/lang $(TMP_DIR)
	@$(CP) $(MANUALS_DIR)/*.pdf $(TMP_DOC_DIR)
	@$(ZIP) $(ZIP_FILE) $(TMP_DIR) $(EXTRA_ARGS)
	@$(RM) $(TMP_DIR)

$(APK_FILE): android-release
	@echo ' * Creating $(APK_FILE) (the apk will not be signed)...'
	@$(CP) $(ANDROID_DIR)/bin/$(PROG_NAME)-release-unsigned.apk \
		$(PROG_NAME)-unsigned.apk

## Generate key:
# keytool -genkey -v -keystore my-release-key.keystore -alias alias_name\
# -keyalg RSA -keysize 2048 -validity 10000
sign-apk:
	@echo ' * Signing apk (pass the KEYSTORE and ALIAS vars)...'
	@jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $(KEYSTORE)\
		$(PROG_NAME)-unsigned.apk $(ALIAS)
	@zipalign -v 4 $(PROG_NAME)-unsigned.apk $(APK_FILE)

clean:
	@echo ' * Removing compiled files...'
	@(cd $(MANUALS_DIR) && make clean $(EXTRA_ARGS))
	@(cd $(PC_DIR) && make clean $(EXTRA_ARGS))
	@(cd $(ANDROID_DIR) && make clean $(EXTRA_ARGS))
	@$(RM) $(TMP_DIR) $(ZIP_FILE) $(APK_FILE)
