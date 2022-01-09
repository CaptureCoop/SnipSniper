@echo off
setlocal enabledelayedexpansion
cls

IF "%~1" == "" goto help

if %1==help goto help
if %1==clean goto clean
if %1==open goto open
if %1==full goto create_full
if %1==portable goto create_portable
if %1==installer goto create_install
if %1==jar goto create_jar

:help
echo Available parameters
echo make clean
echo make open
echo make full
echo make portable
echo make installer
echo make jar
goto done

:open
if exist release\ (
    echo Opening release folder
    %SystemRoot%\explorer.exe "release"
) else (
    echo Release folder does not exist. Try "make full" first
)
goto done

:clean
if exist release\ (
    RMDIR /S /Q release
)
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
jpackage @jpackage\jpackage_defaults.txt --main-class net.snipsniper.Main --app-version @version.txt --type exe --add-launcher SnipSniperEditor=jpackage\jpackage_static_editor.txt --add-launcher SnipSniperViewer=jpackage\jpackage_static_viewer.txt --license-file LICENSE --win-dir-chooser --win-menu --win-shortcut --java-options "-Dplatform=win_installed -DlaunchType=normal"
rename release\SnipSniper*.exe "SnipSniper_Installer_Win.exe"
goto :EOF

:create_portable
echo Creating portable
jpackage @jpackage\jpackage_defaults.txt --main-class net.snipsniper.Main --app-version @version.txt --type app-image --add-launcher SnipSniperEditor=jpackage\jpackage_portable_editor.txt --add-launcher SnipSniperViewer=jpackage\jpackage_portable_viewer.txt --java-options "-Dplatform=win -DlaunchType=normal"
rename release\SnipSniper "SnipSniper_Portable_Win"
goto :EOF

:create_jar
echo Creating jar
xcopy /S /Q /Y /F build\libs\SnipSniper.jar release\SnipSniper.jar* >> nul
goto :EOF

:done