@echo off
setlocal enabledelayedexpansion
title Bus Ticket Booking System - Compiler
echo ==================================================================
echo   Compiling Bus Ticket Booking Management System (Java Swing)
echo ==================================================================

REM Ensure bin directory exists
if not exist "bin" mkdir bin

REM Select best javac executable
set JAVAC_BIN=javac
if exist "C:\Program Files\Java\jdk-26.0.2.1\bin\javac.exe" (
    set JAVAC_BIN="C:\Program Files\Java\jdk-26.0.2.1\bin\javac.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set JAVAC_BIN="%JAVA_HOME%\bin\javac.exe"
    )
)

echo Using Compiler: !JAVAC_BIN!
echo Compiling source files from src/...

!JAVAC_BIN! -encoding UTF-8 -cp "lib/mysql-connector-j-8.3.0.jar;src" -d bin src\Main.java src\database\DBConnection.java src\model\*.java src\dao\*.java src\service\*.java src\gui\*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo ==================================================================
    echo [SUCCESS] Compilation completed with 0 errors!
    echo All .class files generated in bin/ folder.
    echo Run 'run.bat' to launch the desktop application.
    echo ==================================================================
) else (
    echo.
    echo [ERROR] Compilation failed. Please check error messages above.
)

pause
