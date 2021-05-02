@echo off
setlocal enabledelayedexpansion
cls

set jpackage="%userprofile%\.jdks\openjdk-16.0.1\bin\jpackage.exe"

IF "%~1" == "" goto help

if %1==clean goto clean
if %1==full goto create_full
if %1==portable goto create_portable
if %1==installer goto create_install
if %1==jar goto create_jar

:help
echo Available parameters (<debug> is optional and adds Debug Binaries for console output)
echo make clean
echo make full <debug>
echo make portable <debug>
echo make installer <debug>
echo make jar
goto done

:clean
RMDIR /S /Q release
echo Cleaned!
goto :EOF

:create_full
call :clean
call :create_install
call :create_portable
call :create_jar
goto done

:create_install
echo Creating installer
if "%2"=="debug" set debug=--add-launcher SnipSniperDebug=jpackage_static_debug.txt
%jpackage% @jpackage_defaults.txt --main-class io.wollinger.snipsniper.main.MainStatic --app-version @src/main/resources/version.txt --type exe --add-launcher SnipSniperEditor=jpackage_static_editor.txt %debug% --license-file LICENSE --win-dir-chooser --win-menu --win-shortcut
rename release\SnipSniper*.exe "SnipSniper_Installer.exe"
goto :EOF

:create_portable
echo Creating portable
if "%2"=="debug" set debug=--add-launcher SnipSniperDebug=jpackage_portable_debug.txt
%jpackage% @jpackage_defaults.txt --main-class io.wollinger.snipsniper.main.MainPortable --app-version @src/main/resources/version.txt --type app-image --add-launcher SnipSniperEditor=jpackage_portable_editor.txt %debug%
rename release\SnipSniper "SnipSniper_Portable"
goto :EOF

:create_jar
echo Creating jar
xcopy build\libs\SnipSniper.jar release > nul
goto :EOF

:done