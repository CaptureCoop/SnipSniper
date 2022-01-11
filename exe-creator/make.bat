@echo off
set initialPath=%cd%
set title=exe-creator: 
cd %~dp0

IF "%~1" == "" goto help

if %1==help goto help
if %1==clean goto clean
if %1==build goto build

:help
echo EXE-Creator
echo Available parameters
echo make clean
echo make build
goto :exit

:clean
echo %title%Cleaning...
if exist output\ (
	del /Q /S output\*.* >> %temp%\compilelog.txt
)
if not exist output\ (
	mkdir output
)
echo %title%Done
goto exit

:build
call :clean
echo %title%Building...
set icoPath=../../src/main/resources/net/snipsniper/resources/img/icons/
call :compile %icoPath%snipsniper.ico SnipSniper NORMAL
call :compile %icoPath%editor.ico Editor EDITOR
call :compile %icoPath%viewer.ico Viewer VIEWER
rename "output\Editor.exe" "SnipSniper Editor.exe"
rename "output\Viewer.exe" "SnipSniper Viewer.exe"
echo %title%Done
goto exit

:compile
echo.
echo %title%Starting compile for %~2
if exist src\resources.rc (
	del src\resources.rc
)
echo %title%Creating resources.rc for icon %~1...
echo MAINICON ICON "%~1" >> src\resources.rc
echo %title%Compiling resources.rc for icon %~1...
windres src\resources.rc -o output\res.ress
echo %title%Compiling code.cpp to %~2.exe...
c++ -D%~3 -Wl,--subsystem,windows src\code.cpp output\res.ress -o output\%~2.exe -s -Os -static-libgcc -static-libstdc++
echo %title%Cleaning resource files...
del output\res.ress
del src\resources.rc
goto :EOF

:exit
cd %initialPath%