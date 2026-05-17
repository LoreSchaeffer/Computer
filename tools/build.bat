@echo off
setlocal EnableDelayedExpansion

set INPUT_FILE=
set OUTPUT_FILE=

:: Loop to parse command line arguments
:parse_args
if "%~1"=="" goto validate_args

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

:: If an unknown parameter is passed, show help
goto show_help

:validate_args
:: Check if variables are properly set
if "%INPUT_FILE%"=="" goto show_error
if "%OUTPUT_FILE%"=="" goto show_error
goto execute_build

:show_error
echo Error: Missing required parameters.
goto show_help

:show_help
echo Usage: %~nx0 -i ^<input_file.asm^> -o ^<output_file.bin^>
echo Example: %~nx0 -i min_mon.asm -o lyco8_os.bin
exit /b 1

:execute_build
set OBJ_FILE=temp_build.o
set CONFIG_FILE=lyco8.cfg

echo Compiling %INPUT_FILE%...
ca65 %INPUT_FILE% -o %OBJ_FILE%

:: Check for compilation errors
if %ERRORLEVEL% NEQ 0 (
    echo Error: Compilation failed!
    exit /b %ERRORLEVEL%
)

echo Linking to %OUTPUT_FILE% using %CONFIG_FILE%...
ld65 -C %CONFIG_FILE% %OBJ_FILE% -o %OUTPUT_FILE%

:: Check for linking errors
if %ERRORLEVEL% NEQ 0 (
    echo Error: Linking failed!
    :: Clean up even on failure
    if exist %OBJ_FILE% del %OBJ_FILE%
    exit /b %ERRORLEVEL%
)

echo Cleaning up intermediate files...
if exist %OBJ_FILE% del %OBJ_FILE%

echo Build successful: %OUTPUT_FILE% generated.
exit /b 0