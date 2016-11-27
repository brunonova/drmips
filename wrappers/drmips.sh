#!/bin/sh
# Wrapper to run drmips
case "$SNAP_ARCH" in
	"amd64") ARCH='x86_64-linux-gnu'
	;;
	"i386") ARCH='i386-linux-gnu'
	;;
	*)
		echo "Unsupported architecture"
		exit 1
	;;
esac

# Mesa Libs
export LD_LIBRARY_PATH=$SNAP/usr/lib/$ARCH/mesa:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH=$SNAP/usr/lib/$ARCH/mesa-egl:$LD_LIBRARY_PATH

# Tell libGL where to find the drivers
export LIBGL_DRIVERS_PATH=$SNAP/usr/lib/$ARCH/dri

# Setup environment variables
export JAVA_HOME="$SNAP/usr/lib/jvm/default-java"
export PATH="$SNAP/usr/lib/jvm/default-java/bin:$SNAP/usr/lib/jvm/default-java/jre/bin:$PATH"

# Make sure Java can find the fonts
export XDG_DATA_HOME=$SNAP/usr/share
export FONTCONFIG_PATH=$SNAP/etc/fonts/config.d
export FONTCONFIG_FILE=$SNAP/etc/fonts/fonts.conf

# This is another possible workaround to ensure Java find the fonts
#if [ ! -e "$SNAP_USER_DATA/.fonts" ]; then
#	ln -s "$SNAP/usr/share/fonts" "$SNAP_USER_DATA/.fonts"
#fi

# Create some folders in the "common" folder, and symlink them it into the
# user data folder
if [ ! -e "$SNAP_USER_DATA/code" ]; then
	mkdir -p "$SNAP_USER_COMMON/code" && ln -s "$SNAP_USER_COMMON/code" "$SNAP_USER_DATA/code"
fi
if [ ! -e "$SNAP_USER_DATA/cpu" ]; then
	mkdir -p "$SNAP_USER_COMMON/cpu" && ln -s "$SNAP_USER_COMMON/cpu" "$SNAP_USER_DATA/cpu"
fi


# Launch the simulator
# Ensure Java looks for the Preferences inside $SNAP_USER_DATA.
# Without this, the old $HOME directory is used.
# Also ensure that "SNAP_USER_DATA" is considered as the home directory.
java -Djava.util.prefs.userRoot="$SNAP_USER_DATA" -Duser.home="$SNAP_USER_DATA" \
     -Dprogram.name=drmips -jar "$SNAP/usr/share/drmips/DrMIPS.jar" "$@"
