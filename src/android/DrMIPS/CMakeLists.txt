# To generate a (different) keyfor signing:
# $ keytool -genkey -v -keystore my-release-key.keystore -alias alias_name\
#   -keyalg RSA -keysize 2048 -validity 10000

# Configurations
option(DRMIPS_ANDROID_RELEASE_MODE "If ON, the Android version will be built in Release mode (signed), else it will be in Debug mode." OFF)
set(DRMIPS_ANDROID_KEYSTORE_PATH "" CACHE FILEPATH "Path to the keystore used to sign the Android apk file (in Release mode).")
set(DRMIPS_ANDROID_KEYSTORE_ALIAS "" CACHE STRING "Alias of the keystore entry used to sign the Android apk file (in Release mode).")
set(ANDROID_BUILD_DIR "${CMAKE_CURRENT_SOURCE_DIR}/app/build/outputs/apk")
set(ANDROID_DEBUG_APK_PATH "${ANDROID_BUILD_DIR}/app-debug.apk")
set(ANDROID_UNSIGNED_APK_PATH "${ANDROID_BUILD_DIR}/app-release-unsigned.apk")
set(ANDROID_RELEASE_APK_PATH "${ANDROID_BUILD_DIR}/${PROJECT_NAME}.apk")

# Find Gradle script
if(${UNIX})
	set(ANDROID_GRADLEW "${CMAKE_CURRENT_SOURCE_DIR}/gradlew")
else()
	set(ANDROID_GRADLEW "${CMAKE_CURRENT_SOURCE_DIR}/gradlew.bat")
endif()
set(ANDROID_GRADLEW_FLAGS "")

# Target to build the project in Debug mode
add_custom_target(android_debug
                  COMMAND "${ANDROID_GRADLEW}" ${ANDROID_GRADLEW_FLAGS} assembleDebug
                  WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
                  DEPENDS "${PROJECT_NAME}Simulator"
                  COMMENT "Building Android project (Debug)"
                  VERBATIM)

# Target to build the project in Release mode
add_custom_target(android_release
                  COMMAND "${ANDROID_GRADLEW}" ${ANDROID_GRADLEW_FLAGS} assembleRelease
                  COMMAND "${JARSIGNER_TOOL}" ${JARSIGNER_TOOL_FLAGS}
                          -keystore "${DRMIPS_ANDROID_KEYSTORE_PATH}"
                          "${ANDROID_UNSIGNED_APK_PATH}" "${DRMIPS_ANDROID_KEYSTORE_ALIAS}"
                  COMMAND "${ZIPALIGN_TOOL}" ${ZIPALIGN_TOOL_FLAGS} "${ANDROID_UNSIGNED_APK_PATH}"
                          "${ANDROID_RELEASE_APK_PATH}"
                  WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
                  DEPENDS "${PROJECT_NAME}Simulator"
                  COMMENT "Building Android project (Release)"
                  VERBATIM)

# Android build target
if(DRMIPS_ANDROID_RELEASE_MODE) # release mode
	if(DRMIPS_ANDROID_KEYSTORE_PATH AND DRMIPS_ANDROID_KEYSTORE_ALIAS)
		add_custom_target(android_version ALL DEPENDS android_release)
		
		# Copy apk to DRMIPS_DIST_BUILD_DIRECTORY
		add_custom_command(TARGET android_version POST_BUILD
		                   COMMAND "${CMAKE_COMMAND}" -E copy_if_different
		                           "${ANDROID_RELEASE_APK_PATH}" "${DRMIPS_DIST_BUILD_DIR}/${PROJECT_NAME}.apk"
		                   COMMENT "Copying ${PROJECT_NAME}.apk to the dist directory"
		                   VERBATIM)
	else()
		message(SEND_ERROR "DRMIPS_ANDROID_KEYSTORE_PATH and DRMIPS_ANDROID_KEYSTORE_ALIAS variables must be set to build the Android project in Release mode.")
	endif()
else() # debug mode
	add_custom_target(android_version ALL DEPENDS android_debug)
endif()

# Add "build" and "app/build" directories to the "clean" target
set_directory_properties(PROPERTIES ADDITIONAL_MAKE_CLEAN_FILES
                         "${CMAKE_CURRENT_SOURCE_DIR}/build;${CMAKE_CURRENT_SOURCE_DIR}/app/build") 
