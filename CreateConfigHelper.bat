@echo off
setlocal EnableDelayedExpansion

set javaFile="src\main\kotlin\net\snipsniper\config\ConfigHelper.kt"
del %javaFile%

set configLocation=src\main\resources\net\snipsniper\resources\cfg\

echo package net.snipsniper.config; >> %javaFile%
echo class ConfigHelper { >> %javaFile%

echo    /* >> %javaFile%
echo    This was created using the CreateConfigHelper.bat file at the root folder. Leave formatting as is and use the bat file whenever you make any changes to any config >> %javaFile%
echo    */ >> %javaFile%

echo enum class MAIN { >> %javaFile%

for /F "tokens=*" %%A in (%configLocation%main_defaults.cfg) do (
    for /f "tokens=1 delims==" %%A in ("%%A") Do (
        set tmp= %%A
        set char=!tmp:~0,2!

        IF NOT "!char!"==" #" echo %%A, >> %javaFile%
    )
)

echo } >> %javaFile%

echo enum class PROFILE { >> %javaFile%

for /F "tokens=*" %%A in (%configLocation%profile_defaults.cfg) do (
    for /f "tokens=1 delims==" %%A in ("%%A") Do (
        set tmp= %%A
        set char=!tmp:~0,2!

        IF NOT "!char!"==" #" echo %%A, >> %javaFile%
    )
)

echo } >> %javaFile%

echo enum class BUILDINFO { >> %javaFile%

for /F "tokens=*" %%A in (%configLocation%buildinfo.cfg) do (
    for /f "tokens=1 delims==" %%A in ("%%A") Do (
        set tmp= %%A
        set char=!tmp:~0,2!

        IF NOT "!char!"==" #" echo %%A, >> %javaFile%
    )
)

echo } >> %javaFile%

echo } >> %javaFile%