@echo off
set initialPath=%cd%
cd %~dp0

echo Cleaning output directory ...
if exist output\ (
	del /Q /S output\*.* >> %temp%\compilelog.txt
)
if not exist output\ (
	mkdir output
)

set icoPath=../../src/main/resources/org/snipsniper/resources/img/icons/
call :compile %icoPath%snipsniper.ico SnipSniper NORMAL
call :compile %icoPath%editor.ico Editor EDITOR
call :compile %icoPath%viewer.ico Viewer VIEWER
rename "output\Editor.exe" "SnipSniper Editor.exe"
rename "output\Viewer.exe" "SnipSniper Viewer.exe"
goto exit

:compile
echo.
echo Starting compile for %~2!
if exist src\resources.rc (
	del src\resources.rc
)
echo Creating resources.rc for icon %~1...
echo MAINICON ICON "%~1" >> src\resources.rc
echo Compiling resources.rc for icon %~1...
windres src\resources.rc -o output\res.ress
echo Compiling code.cpp to %~2.exe...
c++ -D%~3 -Wl,--subsystem,windows src\code.cpp output\res.ress -o output\%~2.exe -s -Os -static-libgcc -static-libstdc++
echo Cleaning...
del output\res.ress
del src\resources.rc
goto :EOF

:exit
echo.
echo Goodbye!
cd %initialPath%