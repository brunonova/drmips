#.rst:
# UseJavaClassFilelist
# --------------------
#
#
#
#
#
# This script create a list of compiled Java class files to be added to
# a jar file.  This avoids including cmake files which get created in
# the binary directory.

#=============================================================================
# Copyright 2010-2011 Andreas schneider <asn@redhat.com>
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
# * Redistributions of source code must retain the above copyright
#   notice, this list of conditions and the following disclaimer.
#
# * Redistributions in binary form must reproduce the above copyright
#   notice, this list of conditions and the following disclaimer in the
#   documentation and/or other materials provided with the distribution.
#
# * Neither the names of Kitware, Inc., the Insight Software Consortium,
#   nor the names of their contributors may be used to endorse or promote
#   products derived from this software without specific prior written
#   permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#=============================================================================

if (CMAKE_JAVA_CLASS_OUTPUT_PATH)
    if (EXISTS "${CMAKE_JAVA_CLASS_OUTPUT_PATH}")

        set(_JAVA_GLOBBED_FILES)
        if (CMAKE_JAR_CLASSES_PREFIX)
            foreach(JAR_CLASS_PREFIX ${CMAKE_JAR_CLASSES_PREFIX})
                message(STATUS "JAR_CLASS_PREFIX: ${JAR_CLASS_PREFIX}")

                file(GLOB_RECURSE _JAVA_GLOBBED_TMP_FILES "${CMAKE_JAVA_CLASS_OUTPUT_PATH}/${JAR_CLASS_PREFIX}/*.class")
                if (_JAVA_GLOBBED_TMP_FILES)
                    list(APPEND _JAVA_GLOBBED_FILES ${_JAVA_GLOBBED_TMP_FILES})
                endif ()
            endforeach()
        else()
            file(GLOB_RECURSE _JAVA_GLOBBED_FILES "${CMAKE_JAVA_CLASS_OUTPUT_PATH}/*.class")
        endif ()

        set(_JAVA_CLASS_FILES)
        # file(GLOB_RECURSE foo RELATIVE) is broken so we need this.
        foreach(_JAVA_GLOBBED_FILE ${_JAVA_GLOBBED_FILES})
            file(RELATIVE_PATH _JAVA_CLASS_FILE ${CMAKE_JAVA_CLASS_OUTPUT_PATH} ${_JAVA_GLOBBED_FILE})
            set(_JAVA_CLASS_FILES ${_JAVA_CLASS_FILES}${_JAVA_CLASS_FILE}\n)
        endforeach()

        # write to file
        file(WRITE ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist ${_JAVA_CLASS_FILES})

    else ()
        message(SEND_ERROR "FATAL: Java class output path doesn't exist")
    endif ()
else ()
    message(SEND_ERROR "FATAL: Can't find CMAKE_JAVA_CLASS_OUTPUT_PATH")
endif ()
