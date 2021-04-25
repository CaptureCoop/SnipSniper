@echo off
cls

set jpackage="C:\Users\Sven\.jdks\openjdk-16.0.1\bin\jpackage.exe"

IF "%~1" == "" goto help

if %1==clean goto clean
if %1==full goto create_full
if %1==portable goto create_portable
if %1==installer goto create_install
if %1==jar goto create_jar

:help
echo Available parameters
echo build clean
echo build full
echo build portable
echo build installer
echo build jar
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
%jpackage% @jpackage_defaults.txt --dest release --app-version @src/main/resources/version.txt --type exe --license-file LICENSE --win-dir-chooser --win-menu --win-shortcut
rename release\SnipSniper*.exe "SnipSniper Installer.exe"
goto :EOF

:create_portable
echo Creating portable
%jpackage% @jpackage_defaults.txt --dest release --app-version @src/main/resources/version.txt --type app-image
rename release\SnipSniper "SnipSniper Portable"
goto :EOF

:create_jar
echo Creating jar
xcopy build\libs\SnipSniper.jar release > nul
goto :EOF

:done