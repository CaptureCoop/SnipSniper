@echo off

set jpackage="C:\Users\Sven\.jdks\adopt-openjdk-14.0.2\bin\jpackage.exe"

%jpackage% --app-version @src/main/resources/version.txt --copyright "2021 Sven Wollinger" --license-file LICENSE --win-dir-chooser --win-menu --win-shortcut --description "SnipSniper" --name "SnipSniper" --dest build/libs/ --input build/libs/ --main-jar SnipSniper.jar --main-class io.wollinger.snipsniper.Main --icon src/main/resources/res/SnSn.ico --java-options -Dfile.encoding=GB18030 --add-launcher SnipSniperDebug=jpackagedebug.txt
