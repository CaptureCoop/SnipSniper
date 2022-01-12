@echo off
set initialPath=%cd%
set title=jvm-creator: 
set cFolder=%~dp0
cd %~dp0

IF "%~1" == "" goto help

if %1==help goto help
if %1==clean goto clean
if %1==build goto build

:help
echo JVM-Creator
echo Available parameters
echo make clean
echo make build
goto exit

:build
if not exist jdk\ (
	echo %title%jdk folder not found, please place as "jdk"
	goto exit
)

set jarPath=%cFolder%../build/libs/SnipSniper.jar
if not exist %jarPath% (
	if not exist SnipSniper.jar (
		echo %title%SnipSniper.jar missing! Try building with gradle or place SnipSniper.jar here
		goto exit
	)
	jarPath=%cFolder%SnipSniper.jar
)

call :clean
echo %title%Building...
echo %title%Running jdeps on SnipSniper.jar...
%cFolder%jdk\bin\jdeps.exe %jarPath% >> %cFolder%jdeps-output.txt
echo %title%Running PowerJDEP.jar on jdeps-output.txt...
%cFolder%jdk\bin\java.exe -jar %cFolder%PowerJDEP.jar %cFolder%jdeps-output.txt -jlink-pretty >> %cFolder%powerjdep-output.txt

set /p modules=<%cFolder%powerjdep-output.txt

echo %title%Running jlink with powerjdep-output.txt...
%cFolder%jdk\bin\jlink.exe --output %cFolder%output\jdk\ --add-modules %modules%

echo %title%Done!
goto exit

:clean
echo %title%Cleaning...
if exist output\ rmdir /Q /S output
if exist powerjdep-output.txt del /Q /S powerjdep-output.txt >> nul
if exist jdeps-output.txt del /Q /S jdeps-output.txt >> nul
echo %title%Done!
goto exit

:exit
cd %initialPath%