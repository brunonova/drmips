cp build/outputs/apk/DrMIPS-release-unsigned.apk build/outputs/apk/DrMIPS-unaligned.apk
jarsigner -sigalg SHA1withRSA -digestalg SHA1 -keystore $HOME/drmips.keystore build/outputs/apk/DrMIPS-unaligned.apk drmips
zipalign -f 4 build/outputs/apk/DrMIPS-unaligned.apk build/outputs/apk/DrMIPS.apk