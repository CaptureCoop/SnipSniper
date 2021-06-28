@echo off
setlocal EnableDelayedExpansion
setlocal enabledelayedexpansion

set javaFile="src\main\java\io\wollinger\snipsniper\utils\ConfigHelper.java"
del %javaFile%

echo package io.wollinger.snipsniper.utils; >> %javaFile%
echo public class ConfigHelper { >> %javaFile%

echo    /* >> %javaFile%
echo    This was created using the CreateConfigHelper.bat file at the root folder. Leave formatting as is and use the bat file whenever you make any changes to any config >> %javaFile%
echo    */ >> %javaFile%

echo public enum MAIN { >> %javaFile%

for /F "tokens=*" %%A in (src\main\resources\cfg\main_defaults.cfg) do (
    for /f "tokens=1 delims==" %%A in ("%%A") Do (
        set tmp= %%A
        set char=!tmp:~0,2!

        IF NOT "!char!"==" <" echo %%A, >> %javaFile%
    )
)

echo } >> %javaFile%

echo public enum PROFILE { >> %javaFile%

for /F "tokens=*" %%A in (src\main\resources\cfg\profile_defaults.cfg) do (
    for /f "tokens=1 delims==" %%A in ("%%A") Do (
        set tmp= %%A
        set char=!tmp:~0,2!

        IF NOT "!char!"==" <" echo %%A, >> %javaFile%
    )
)

echo } >> %javaFile%
echo } >> %javaFile%