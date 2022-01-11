@echo off

IF "%~1" == "" goto help

if %1==help goto help
if %1==clean goto clean
if %1==open goto open
if %1==jar goto jar
if %1==jvm goto jvm
if %1==exe goto exe
if %1==prepare goto prepare
if %1==portable goto portable
if %1==installer goto installer
if %1==full goto full

:help
echo Available parameters
echo make help          - Display this help
echo make clean         - Remove the release folder and clean ./jvm-creator/ and ./exe-creator/
echo make open          - Opens release folder
echo make jar           - Create the jar in ./build/libs/
echo make jvm           - Create the JVM in ./jvm-creator/
echo make exe           - Create the EXEs in ./exe-creator/
echo make prepare       - Move files from above directories
echo make portable      - Zip the windows version up as the portable release
echo make installer     - Create an installer from the windows version
echo make full          - Do everything (Except open)
goto exit

:clean
echo Cleaning...
if exist release\ RMDIR /S /Q release
call jvm-creator/make clean
call exe-creator/make clean
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
echo Compiling jar...
call gradlew build
goto :EOF

:jvm
echo Running jvm-creator...
call jvm-creator/make build
goto :EOF

:exe
echo Running exe-creator/
call exe-creator/make build
goto :EOF

:prepare
echo Preparing files...
mkdir release
robocopy jvm-creator/output/jdk/ release/SnipSniper/jdk/ /E
robocopy exe-creator/output/ release/ /E
xcopy build\libs\SnipSniper.jar release\
goto :EOF

:full
call :clean
call :jar
call :jvm
call :exe
call :prepare
goto exit

:exit
echo Exiting...