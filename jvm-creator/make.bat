@echo off
set initialPath=%cd%
set title=jvm-creator: 
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

if not exist SnipSniper.jar (
	echo %title%SnipSniper.jar is missing, please place as "SnipSniper.jar"
	goto exit
)

call :clean
echo %title%Building...
echo %title%Running jdeps on SnipSniper.jar...
jdk\bin\jdeps.exe SnipSniper.jar >> jdeps-output.txt
echo %title%Running PowerJDEP.jar on jdeps-output.txt...
jdk\bin\java.exe -jar PowerJDEP.jar jdeps-output.txt -jlink-pretty >> powerjdep-output.txt

set /p modules=<powerjdep-output.txt

echo %title%Running jlink with powerjdep-output.txt...
jdk\bin\jlink.exe --output output\jdk\ --add-modules %modules%

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