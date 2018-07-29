Compiling DrMIPS from source
============================

1.  Install the dependencies.
    In Ubuntu and Debian, these are the known dependencies:

    *   openjdk-7-jdk *(or another JDK)*

    To compile the Android version, you will additionally need to install the
    Android SDK.

2.  Open a terminal in the project directory. On Windows run:

        gradlew.bat dist

    On Linux run:

        ./gradlew dist

    This will create a zip file in `src/pc/build/distributions`, ready to
    run it or distribute it.

    To build the Android version, you also need to uncomment the following line
    in settings.gradle:

        //include "src:android"
