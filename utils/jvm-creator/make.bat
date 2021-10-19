@echo off

if not exist jdk\ (
	echo jdk folder not found, please place as "jdk"
	goto exit
)

if not exist SnipSniper.jar (
	echo SnipSniper.jar is missing, please place as "SnipSniper.jar"
	goto exit
)

echo Cleaning...
if exist output\ rmdir /Q /S output
if exist jdeps-output.txt del jdeps-output.txt
if exist powerjdep-output.txt del powerjdep-output.txt

echo Running jdeps on SnipSniper.jar...
jdk\bin\jdeps.exe SnipSniper.jar >> jdeps-output.txt
echo Running PowerJDEP.jar on jdeps-output.txt...
jdk\bin\java.exe -jar PowerJDEP.jar jdeps-output.txt -jlink-pretty >> powerjdep-output.txt

set /p modules=<powerjdep-output.txt

echo Running jlink with powerjdep-output.txt...
jdk\bin\jlink.exe --output output\jdk\ --add-modules %modules%

echo Done!

:exit