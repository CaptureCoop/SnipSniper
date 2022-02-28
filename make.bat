@echo off

set initialPath=%cd%
set zip=%programFiles%\7-Zip\7z.exe
set nsis=%ProgramFiles(x86)%\NSIS\makensis.exe

IF "%~1" == "" goto help

if %1==help goto help
if %1==clean goto clean
if %1==open goto open
if %1==jar goto jar
if %1==portable goto portable
if %1==installer goto installer
if %1==full goto full

:help
	echo Available parameters
	echo make help          - Display this help
	echo make clean <all>	- Cleans the build system (add "all" to also remove ./release/output/
	echo make open          - Opens release folder
	echo make jar           - Create the jar in ./build/libs/
	echo make portable      - Zip the windows version up as the portable release
	echo make installer     - Create an installer from the windows version
	echo make full          - Build all three versions
goto exit

:clean
	echo Cleaning...
	if "%~2" == "all" (
		if exist release\ RMDIR /S /Q release\
	) else (
		if exist release\raw\ RMDIR /S /Q release\raw\
	)
	call jvm-creator/make clean
	call exe-creator/make clean
	call gradlew clean
	echo Done!
goto :EOF

:open
	if exist release\ (
		echo Opening release folder
		%SystemRoot%\explorer.exe "release"
	) else (
		echo Release folder does not exist. Try "make full" first
	)
goto :EOF

:jar
	call :clean
	echo Compiling jar...
	call gradlew build
	call :prepare

	cd release\raw\
		echo Moving jar...
		xcopy SnipSniper.jar ..\output\ > nul
	cd %initialPath%
goto :EOF

:portable
	echo Creating portable...
	call :clean
	call gradlew build
	call exe-creator/make build WIN
	call jvm-creator/make build
	call :prepare

	cd release\raw\
		"%zip%" a ..\output\SnipSniper_Portable_Win.zip *
	cd %initialPath%
goto :EOF

:installer
	echo Creating installer...
	call :clean
	call gradlew build
	call exe-creator/make build WIN_INSTALLED
	call jvm-creator/make build
	call :prepare
	if not exist release\output\ (
		mkdir release\output\
	)
	"%nsis%" nsis-installer.nsi
goto :EOF

:prepare
	echo Preparing files...
	mkdir release
	robocopy jvm-creator/output/jdk/ release/raw/SnipSniper/jdk/ /E > nul
	robocopy exe-creator/output/ release/raw/ /E > nul
	xcopy build\libs\SnipSniper.jar release\raw\ > nul
	echo Done preparing files
goto :EOF


:full
	call :clean all
	call :jar
	call :portable
	call :installer
goto exit

:exit
	echo Exiting...