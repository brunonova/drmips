# - Find Android
# This module finds if the required Android/Java tools are installed and
# determines their paths. This code sets the following variables:
#
#  ANDROID_TOOL          = the full path to the android executable (Android SDK)
#  ANT_TOOL              = the full path to the ant executable (JDK)
#  JARSIGNER_TOOL        = the full path to the jarsigner executable (JDK)
#  ZIPALIGN_TOOL         = the full path to the zipalign tool (Android SDK)
#  ANT_TOOL_FLAGS        = flags passed to the ant executable
#  JARSIGNER_TOOL_FLAGS  = flags passed to the jarsigner executable
#  ZIPALIGN_TOOL_FLAGS   = flags passed to the zipalign executable

#=============================================================================
# DrMIPS - Educational MIPS simulator
# Copyright (C) 2014-2015 Bruno Nova <brunomb.nova@gmail.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#=============================================================================

mark_as_advanced(JARSIGNER_TOOL ZIPALIGN_TOOL JARSIGNER_TOOL_FLAGS
                 ZIPALIGN_TOOL_FLAGS)

# Check in ANDROID_HOME is defined
message(STATUS "Android_HOME: $ENV{ANDROID_HOME}")
if(EXISTS $ENV{ANDROID_HOME})
	set(ANDROID_HOME "$ENV{ANDROID_HOME}")
endif()

# Find required tools
find_program(JARSIGNER_TOOL jarsigner)
find_program(ZIPALIGN_TOOL zipalign)

# Define the default flags for the tools
set(JARSIGNER_TOOL_FLAGS "-sigalg SHA1withRSA -digestalg SHA1" CACHE STRING "Flags passed to jarsigner.")
separate_arguments(JARSIGNER_TOOL_FLAGS)
set(ZIPALIGN_TOOL_FLAGS "-f 4" CACHE STRING "Flags passed to zipalign.")
separate_arguments(ZIPALIGN_TOOL_FLAGS)

# Finish
find_package_handle_standard_args(Android "Could not find all the required Android tools!\nBoth JDK and Android SDK must be installed, and the SDK's tools/ directory must be in the PATH.\nAdditionally, the environment variable ANDROID_HOME must be defined and contain the installation path of the Android SDK."
                                  ANDROID_HOME JARSIGNER_TOOL ZIPALIGN_TOOL)
