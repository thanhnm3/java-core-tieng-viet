# Build static pages for GitHub Pages from src/main/resources/web
$webRoot = "src\main\resources\web"
$layout = [System.IO.File]::ReadAllText("_layout-template.html", [System.Text.Encoding]::UTF8)

function Get-TopicsNav {
  param([string]$toTopics, [string]$active, [array]$subPages = @(), [string]$activeSubPage = "")
  $mainLinks = @(
    @{ href = "index.html"; slug = "home"; label = "Trang ch&#7911;" },
    @{ href = "oop/01-intro.html"; slug = "oop"; label = "OOP" },
    @{ href = "collections/01-intro.html"; slug = "collections"; label = "Collections" },
    @{ href = "io-nio/01-intro.html"; slug = "io-nio"; label = "I/O & NIO" },
    @{ href = "concurrency/01-intro.html"; slug = "concurrency"; label = "Concurrency" },
    @{ href = "jvm-memory/01-architecture.html"; slug = "jvm-memory"; label = "JVM & Memory" },
    @{ href = "exception/01-hierarchy.html"; slug = "exception"; label = "Exception" },
    @{ href = "generics/01-intro.html"; slug = "generics"; label = "Generics" },
    @{ href = "lambda/01-intro.html"; slug = "lambda"; label = "Lambda & Stream" }
  )
  $sb = [System.Text.StringBuilder]::new()
  [void]$sb.AppendLine('<nav class="topic-sidebar"><ul>')
  foreach ($l in $mainLinks) {
    $h = if ($l.slug -eq "home") { $toTopics + "../index.html" } else { $toTopics + $l.href }
    $cls = if ($l.slug -eq $active) { ' class="topic-sidebar-link--active"' } else { '' }
    [void]$sb.AppendLine("        <li><a href=`"$h`"$cls>$($l.label)</a>")
    if ($l.slug -eq $active -and $subPages.Count -gt 0) {
      [void]$sb.AppendLine('        <ul class="topic-sub-nav">')
      foreach ($sp in $subPages) {
        $subH = $toTopics + $sp.href
        $subCls = if ($sp.name -eq $activeSubPage) { ' class="topic-sidebar-link--active"' } else { '' }
        [void]$sb.AppendLine("          <li><a href=`"$subH`"$subCls>$($sp.label)</a></li>")
      }
      [void]$sb.AppendLine('        </ul>')
    }
    [void]$sb.AppendLine('        </li>')
  }
  [void]$sb.AppendLine('</ul></nav>')
  $sb.ToString()
}

$topicLabels = @{
  "oop"         = "OOP"
  "collections" = "Collections"
  "io-nio"      = "I/O & NIO"
  "concurrency" = "Concurrency"
  "jvm-memory"  = "JVM & Memory"
  "exception"   = "Exception"
  "generics"    = "Generics"
  "lambda"      = "Lambda & Stream"
}
$topicFirstPage = @{
  "oop"         = "oop/01-intro.html"
  "collections" = "collections/01-intro.html"
  "io-nio"      = "io-nio/01-intro.html"
  "concurrency" = "concurrency/01-intro.html"
  "jvm-memory"  = "jvm-memory/01-architecture.html"
  "exception"   = "exception/01-hierarchy.html"
  "generics"    = "generics/01-intro.html"
  "lambda"      = "lambda/01-intro.html"
}

function New-Page {
  param([string]$srcFile, [string]$destFile, [string]$title, [string]$prefix, [string]$toTopics, [string]$activeSlug, [array]$subPages = @(), [string]$activeSubPage = "")
  $content = [System.IO.File]::ReadAllText($srcFile, [System.Text.Encoding]::UTF8)
  $cssPath = $prefix + "css/style.css"
  $faviconPath = $prefix + "favicon.svg"
  $homePath = $prefix + "index.html"
  $nav = Get-TopicsNav -toTopics $toTopics -active $activeSlug -subPages $subPages -activeSubPage $activeSubPage

  $topicLabel = $topicLabels[$activeSlug]
  if ($topicLabel) {
    $topicUrl = $toTopics + $topicFirstPage[$activeSlug]
    $breadcrumb = "<a href=`"$homePath`">Trang ch&#7911;</a> / <a href=`"$topicUrl`">$topicLabel</a> / $title"
  }
  else {
    $breadcrumb = "<a href=`"$homePath`">Trang ch&#7911;</a> / $title"
  }

  $html = $layout -replace '\{\{title\}\}', $title -replace '\{\{content\}\}', $content -replace '\{\{cssPath\}\}', $cssPath -replace '\{\{faviconPath\}\}', $faviconPath -replace '\{\{homePath\}\}', $homePath -replace '\{\{topicsNav\}\}', $nav -replace '\{\{breadcrumb\}\}', $breadcrumb
  $destDir = Split-Path $destFile -Parent
  if (!(Test-Path $destDir)) { New-Item -ItemType Directory -Force -Path $destDir | Out-Null }
  $utf8 = New-Object System.Text.UTF8Encoding $true
  [System.IO.File]::WriteAllText((Join-Path $PWD $destFile), $html, $utf8)
}

# Subdirs: oop, collections, io-nio, concurrency, jvm-memory, exception, generics, lambda
$subdirs = @("oop", "collections", "io-nio", "concurrency", "jvm-memory", "exception", "generics", "lambda")
$titlesJson = [System.IO.File]::ReadAllText("_titles.json", [System.Text.Encoding]::UTF8)
$titlesObj = $titlesJson | ConvertFrom-Json
foreach ($dir in $subdirs) {
  $files = Get-ChildItem "$webRoot\topics\$dir\*.html" -ErrorAction SilentlyContinue | Sort-Object Name
  $subPages = @()
  foreach ($f in $files) {
    $base = $f.BaseName
    $label = ($titlesObj.PSObject.Properties | Where-Object { $_.Name -eq $base }).Value; if (-not $label) { $label = $base }
    $subPages += @{ name = $base; href = "$dir/$($f.Name)"; label = $label }
  }
  foreach ($f in $files) {
    $base = $f.BaseName
    $title = ($titlesObj.PSObject.Properties | Where-Object { $_.Name -eq $base }).Value; if (-not $title) { $title = $base }
    New-Page -srcFile $f.FullName -destFile "topics\$dir\$($f.Name)" -title $title -prefix "../../" -toTopics "../" -activeSlug $dir -subPages $subPages -activeSubPage $base
  }
}
Write-Host "Done. Built topic pages."
