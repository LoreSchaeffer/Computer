@echo off
setlocal EnableDelayedExpansion

:: Default Configuration
set INPUT_FILE=src\main.ls
set OUTPUT_FILE=build\output.bin

:: Parameter Parsing
:parse_args
if "%~1"=="" goto start_build

if /I "%~1"=="-i" (
    set INPUT_FILE=%~2
    shift
    shift
    goto parse_args
)
if /I "%~1"=="-o" (
    set OUTPUT_FILE=%~2
    shift
    shift
    goto parse_args
)

echo [ERROR] Unknown parameter: %~1
echo Usage: build.bat [-i input.ls] [-o output.bin]
exit /b 1

:start_build
:: Derived File Names
set ASM_FILE=!OUTPUT_FILE:.bin=.asm!
set OBJ_FILE=!OUTPUT_FILE:.bin=.o!

echo [PRE-FLIGHT] Checking dependencies...
where ca65 >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] ca65 compiler not found in PATH!
    echo Please download cc65 from https://github.com/cc65/cc65
    echo Extract it and add the 'bin' folder to your Windows PATH.
    exit /b 1
)

:: Grants the presence of the build directory before compiling
if not exist build mkdir build

echo [1/4] Compiling LycoScript to Assembly...
java -jar LycoScriptCompiler.jar -i "%INPUT_FILE%" -o "%ASM_FILE%"
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo [2/4] Assembling Main Project...
ca65 "%ASM_FILE%" -o "%OBJ_FILE%"
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%

echo [3/4] Assembling Standard Library and Custom User Assembly (Recursive)...
set STDLIB_OBJS=

:: Compilation of Standard Library
if exist stdlib\assembly\*.asm (
    for %%f in (stdlib\assembly\*.asm) do (
        ca65 "%%f" -o "build\%%~nf.o"
        if !ERRORLEVEL! NEQ 0 exit /b !ERRORLEVEL!
        set STDLIB_OBJS=!STDLIB_OBJS! "build\%%~nf.o"
    )
)

:: Recursive search inside src and direct compilation to build directory
for /R src %%f in (*.asm) do (
    ca65 "%%f" -o "build\%%~nf.o"
    if !ERRORLEVEL! NEQ 0 exit /b !ERRORLEVEL!
    set STDLIB_OBJS=!STDLIB_OBJS! "build\%%~nf.o"
)

echo [4/4] Linking ROM...
ld65 -C lyco8.cfg "%OBJ_FILE%" !STDLIB_OBJS! -o "%OUTPUT_FILE%"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Linking failed!
    exit /b %ERRORLEVEL%
)

echo SUCCESS! Output generated in %OUTPUT_FILE%
exit /b 0