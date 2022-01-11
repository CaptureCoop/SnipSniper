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
echo make full          - Do everything
goto exit

:clean
echo Cleaning...
if exist release\ RMDIR /S /Q release
call jvm-creator/make clean
REM exe-creator/make clean
echo Done!
goto :EOF

:exit