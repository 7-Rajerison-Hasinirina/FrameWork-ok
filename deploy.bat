@echo off
setlocal

REM ============================================
REM Configuration
REM ============================================
set JAR_NAME=framework.jar
set SRC_DIR=src
set BIN_DIR=bin
set LIB_DIR=lib
set SERVLET_API=lib\servlet-api.jar

echo.
echo ===============================
echo Nettoyage...
echo ===============================

if exist "%BIN_DIR%" rmdir /S /Q "%BIN_DIR%"
if exist "%JAR_NAME%" del /Q "%JAR_NAME%"

mkdir "%BIN_DIR%"

echo.
echo ===============================
echo Compilation des fichiers Java...
echo ===============================

REM Genere la liste des sources Java
dir /S /B "%SRC_DIR%\*.java" > sources.txt

REM Le classpath inclut servlet-api.jar ET tous les jars de lib (Spring, etc.)
javac --release 17 -cp "%SERVLET_API%;%LIB_DIR%\*" -d "%BIN_DIR%" @sources.txt

if errorlevel 1 (
    echo.
    echo =====================================
    echo ERREUR : La compilation a echoue.
    echo =====================================
    del sources.txt
    pause
    exit /b 1
)

del sources.txt

echo.
echo ===============================
echo Creation du fichier JAR...
echo ===============================

cd "%BIN_DIR%"
jar -cvf "..\%JAR_NAME%" .
cd ..

echo.
echo =====================================
echo Compilation terminee avec succes !
echo Le fichier %JAR_NAME% a ete cree.
echo =====================================

pause