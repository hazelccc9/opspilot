param(
    [string]$Time = '08:30',
    [switch]$Uninstall,
    [switch]$Test
)

$ErrorActionPreference = 'Stop'
$taskName = 'OpsPilot-Daily'
$scriptPath = Join-Path $PSScriptRoot 'today.ps1'

if ($Test) {
    & $scriptPath -Toast
    exit 0
}

if ($Uninstall) {
    $existing = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    if ($existing) {
        Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
        "已删除每日提醒任务：$taskName"
    }
    else {
        "没有找到任务：$taskName"
    }
    exit 0
}

if (-not (Test-Path $scriptPath)) {
    Write-Error "找不到脚本：$scriptPath"
    exit 1
}

$action = New-ScheduledTaskAction `
    -Execute 'powershell.exe' `
    -Argument "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File `"$scriptPath`" -Toast"

$trigger = New-ScheduledTaskTrigger -Daily -At $Time

$settings = New-ScheduledTaskSettingsSet `
    -StartWhenAvailable `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -ExecutionTimeLimit (New-TimeSpan -Minutes 10)

$principal = New-ScheduledTaskPrincipal `
    -UserId $env:USERNAME `
    -LogonType Interactive `
    -RunLevel Limited

Register-ScheduledTask `
    -TaskName $taskName `
    -Action $action `
    -Trigger $trigger `
    -Settings $settings `
    -Principal $principal `
    -Description 'OpsPilot daily plan reminder' `
    -Force | Out-Null

"已安装每日提醒：每天 $Time 弹出 OpsPilot 今日任务。"
"任务名：$taskName"
"手动测试：powershell -ExecutionPolicy Bypass -File `"$PSScriptRoot\today.ps1`" -Toast"
"卸载：powershell -ExecutionPolicy Bypass -File `"$PSScriptRoot\install-reminder.ps1`" -Uninstall"
