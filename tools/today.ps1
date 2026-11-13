param(
    [int]$Day = 0,
    [switch]$Week,
    [switch]$List,
    [switch]$Open,
    [switch]$Toast,
    [switch]$NoWrite
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$root = Split-Path -Parent $PSScriptRoot
$planPath = Join-Path $root 'plan\daily-plan.json'
$todayPath = Join-Path $root 'plan\TODAY.md'

if (-not (Test-Path $planPath)) {
    Write-Error "找不到计划文件：$planPath"
    exit 1
}

$plan = Get-Content -Raw -Encoding UTF8 $planPath | ConvertFrom-Json
$days = @($plan.days)
$todayStr = Get-Date -Format 'yyyy-MM-dd'

function Format-Day {
    param($Entry)

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add('=' * 64)
    $lines.Add("OpsPilot 第 $($Entry.day) 天 / $($Entry.date) $($Entry.weekday)  (Week $($Entry.week))")
    $lines.Add("主题：$($Entry.theme)")
    $lines.Add("目标：$($Entry.goal)")
    $lines.Add('-' * 64)
    $lines.Add('今日任务')
    $i = 1
    foreach ($task in $Entry.tasks) {
        $lines.Add("[ ] $i. $task")
        $i++
    }
    $lines.Add('')
    $lines.Add('验收标准')
    foreach ($item in $Entry.acceptance) {
        $lines.Add("- $item")
    }
    $lines.Add('')
    $lines.Add('求职任务')
    $lines.Add("- $($Entry.jobSearch)")
    $lines.Add('=' * 64)
    return ($lines -join [Environment]::NewLine)
}

if ($List) {
    $days | ForEach-Object {
        "{0,2}. {1} {2}  {3}" -f $_.day, $_.date, $_.weekday, $_.theme
    }
    exit 0
}

$entry = $null
$prefix = ''

if ($Day -gt 0) {
    $entry = $days | Where-Object { $_.day -eq $Day } | Select-Object -First 1
    if (-not $entry) {
        Write-Error "没有第 $Day 天，范围是 1-$($days.Count)。"
        exit 1
    }
}
elseif ($Week) {
    $current = $days | Where-Object { $_.date -eq $todayStr } | Select-Object -First 1
    if (-not $current) {
        $current = $days | Where-Object { $_.date -gt $todayStr } | Sort-Object date | Select-Object -First 1
    }
    if (-not $current) {
        $current = $days | Select-Object -Last 1
    }
    $weekDays = $days | Where-Object { $_.week -eq $current.week }
    "OpsPilot Week $($current.week)  ($($weekDays[0].date) ~ $($weekDays[-1].date))"
    ''
    $weekDays | ForEach-Object {
        "{0,2}. {1} {2}  {3}" -f $_.day, $_.date, $_.weekday, $_.theme
        "     目标：$($_.goal)"
        ''
    }
    exit 0
}
else {
    $entry = $days | Where-Object { $_.date -eq $todayStr } | Select-Object -First 1
    if (-not $entry) {
        $entry = $days | Where-Object { $_.date -gt $todayStr } | Sort-Object date | Select-Object -First 1
        if ($entry) {
            $prefix = "今天（$todayStr）不在计划日期内，显示下一天："
        }
    }
    if (-not $entry) {
        $entry = $days | Select-Object -Last 1
        $prefix = "计划已结束，显示最后一天："
    }
}

$text = Format-Day -Entry $entry
if ($prefix) {
    $text = $prefix + [Environment]::NewLine + [Environment]::NewLine + $text
}

$remaining = $days.Count - $entry.day
$footer = "进度：第 $($entry.day) / $($days.Count) 天；还剩 $remaining 天。计划文件：plan\daily-plan.json"
$text = $text + [Environment]::NewLine + $footer

Write-Output $text

if (-not $NoWrite) {
    $md = @()
    $md += "# OpsPilot 今日任务"
    $md += ''
    $md += "> $($entry.date) $($entry.weekday) · 第 $($entry.day) / $($days.Count) 天 · Week $($entry.week)"
    $md += ''
    $md += "## $($entry.theme)"
    $md += ''
    $md += "**目标：** $($entry.goal)"
    $md += ''
    $md += '## 今日任务'
    $md += ''
    $i = 1
    foreach ($task in $entry.tasks) {
        $md += "- [ ] $i. $task"
        $i++
    }
    $md += ''
    $md += '## 验收标准'
    $md += ''
    foreach ($item in $entry.acceptance) {
        $md += "- $item"
    }
    $md += ''
    $md += '## 求职任务'
    $md += ''
    $md += "- $($entry.jobSearch)"
    $md += ''
    $md += '---'
    $md += ''
    $md += "进度：第 $($entry.day) / $($days.Count) 天；还剩 $remaining 天。"
    $md | Out-File -FilePath $todayPath -Encoding utf8
}

if ($Open) {
    Start-Process $todayPath
}

if ($Toast) {
    Add-Type -AssemblyName System.Windows.Forms
    $preview = ($entry.tasks | Select-Object -First 3 | ForEach-Object { "• $_" }) -join [Environment]::NewLine
    $msg = "第 $($entry.day) 天 · $($entry.theme)$([Environment]::NewLine)$([Environment]::NewLine)$preview$([Environment]::NewLine)$([Environment]::NewLine)完整计划：$todayPath"
    [System.Windows.Forms.MessageBox]::Show(
        $msg,
        'OpsPilot 今日任务',
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Information
    ) | Out-Null
}
