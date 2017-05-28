cp build\outputs\apk\DrMIPS-release-unsigned.apk build\outputs\apk\DrMIPS-unaligned.apk
jarsigner.exe -sigalg SHA1withRSA -digestalg SHA1 -keystore %USERPROFILE%\drmips.keystore build\outputs\apk\DrMIPS-unaligned.apk drmips
%USERPROFILE%\AppData\Local\Android\sdk\build-tools\25.0.2\zipalign.exe -f 4 build\outputs\apk\DrMIPS-unaligned.apk build\outputs\apk\DrMIPS.apk
pause