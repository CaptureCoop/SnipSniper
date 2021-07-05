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
echo Available parameters (debug is optional and adds Debug Binaries for console output)
echo make clean
echo make open
echo make full ^<debug^>
echo make portable ^<debug^>
echo make installer ^<debug^>
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
if "%2"=="debug" set debug=--add-launcher SnipSniperDebug=jpackage\jpackage_static_debug.txt
jpackage @jpackage\jpackage_defaults.txt --main-class io.wollinger.snipsniper.main.MainStatic --app-version @build/SSVersion.txt --type exe --add-launcher SnipSniperEditor=jpackage\jpackage_static_editor.txt --add-launcher SnipSniperViewer=jpackage\jpackage_static_viewer.txt %debug% --license-file LICENSE --win-dir-chooser --win-menu --win-shortcut
rename release\SnipSniper*.exe "SnipSniper_Installer_Win.exe"
goto :EOF

:create_portable
echo Creating portable
if "%2"=="debug" set debug=--add-launcher SnipSniperDebug=jpackage\jpackage_portable_debug.txt
jpackage @jpackage\jpackage_defaults.txt --main-class io.wollinger.snipsniper.main.MainPortable --app-version @build/SSVersion.txt --type app-image --add-launcher SnipSniperEditor=jpackage\jpackage_portable_editor.txt --add-launcher SnipSniperViewer=jpackage\jpackage_portable_viewer.txt %debug%
rename release\SnipSniper "SnipSniper_Portable_Win"
goto :EOF

:create_jar
echo Creating jar
xcopy /S /Q /Y /F build\libs\SnipSniper.jar release\SnipSniper.jar* >> nul
goto :EOF

:done