Installing DrMIPS from source
=============================

1.  Install the dependencies.
    In Ubuntu and Debian, these are the known dependencies:

    *   cmake
    *   openjdk-7-jdk *(or another JDK)*

    To compile the manuals, which is done by default, you also need:

    *   texlive
    *   texlive-latex-extra
    *   texlive-science
    *   texlive-lang-portuguese

    To compile the Android version, you will additionally need to install the
    Android SDK.

2.  Open a terminal in the project directory and run:

        mkdir build
        cd build
        cmake ..
        make
        sudo make install

    If you don't want to compile the manuals, run
    `cmake -DDRMIPS_BUILD_MANUALS=off` instead of `cmake ..`.
    For further configurations, you can run `ccmake ..` or `cmake-gui ..`.

    To create a ZIP file for distribution of the PC version, run:

        make dist
