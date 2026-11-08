@echo off
setlocal enabledelayedexpansion
title Bus Ticket Booking Management System - Application Launcher
echo ==================================================================
echo   Starting Bus Ticket Booking Management System
echo ==================================================================

REM Ensure bin folder exists
if not exist "bin\Main.class" (
    echo [NOTICE] Compiled classes not found. Running compile.bat first...
    call compile.bat
)

REM Select best java executable
set JAVA_BIN=java
if exist "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe" (
    set JAVA_BIN="C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set JAVA_BIN="%JAVA_HOME%\bin\java.exe"
    )
)

echo.
echo [INFO] Make sure XAMPP Apache and MySQL are running in XAMPP Control Panel.
echo [INFO] Database: bus_ticketing on localhost:3306
echo [INFO] Launching Java Swing Desktop GUI...
echo.

!JAVA_BIN! -cp "bin;lib/mysql-connector-j-8.3.0.jar" Main

if %ERRORLEVEL% neq 0 (
    echo.
    echo [NOTICE] Application closed with code %ERRORLEVEL%.
    pause
)
