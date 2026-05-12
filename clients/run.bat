@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
call mvnw.cmd spring-boot:run -DskipTests
pause
