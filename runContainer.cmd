@echo off
for /f "usebackq delims=" %%i in (`mvnw.cmd -q -DforceStdout help:evaluate "-Dexpression=project.version"`) do set "PROJECT_VERSION=%%i"
echo PROJECT_VERSION=%PROJECT_VERSION%
call docker compose -f src/main/container/compose.yaml up
pause
