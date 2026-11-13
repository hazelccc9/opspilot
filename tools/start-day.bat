@echo off
chcp 65001 >nul
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0today.ps1" -Open
if errorlevel 1 pause
