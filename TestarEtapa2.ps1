param([string]$Javac = "javac")
$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "CompilarEtapa2.ps1") -Javac $Javac
$compiler = (Get-Command $Javac -ErrorAction Stop).Source
$java = Join-Path (Split-Path $compiler) "java.exe"
$classes = Join-Path $PSScriptRoot "build\etapa2"
$tests = Join-Path $PSScriptRoot "build\testes-etapa2"
New-Item -ItemType Directory -Force -Path $tests | Out-Null
& $compiler -encoding UTF-8 --release 8 -Xlint:all,-options -cp $classes -d $tests (Join-Path $PSScriptRoot "Testes\Etapa2\TesteParserEtapa2.java")
if ($LASTEXITCODE -ne 0) { throw "Falha ao compilar os testes." }
& $java -cp "$classes;$tests" TesteParserEtapa2 $PSScriptRoot
if ($LASTEXITCODE -ne 0) { throw "Falha nos testes." }
