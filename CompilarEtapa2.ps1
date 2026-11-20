param([string]$Javac = "javac")
$ErrorActionPreference = "Stop"
$compiler = (Get-Command $Javac -ErrorAction Stop).Source
$jar = Join-Path (Split-Path $compiler) "jar.exe"
if (!(Test-Path -LiteralPath $jar)) { throw "jar.exe nao encontrado ao lado de javac. Use um JDK 9 ou superior." }
$classes = Join-Path $PSScriptRoot "build\etapa2"
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = @("Main.java", "LexerEtapa2.java", "ParserEtapa2.java", "SyntaxException.java",
    "Token.java", "Tag.java", "Word.java", "Literal.java", "CharConst.java")
$paths = $sources | ForEach-Object { Join-Path $PSScriptRoot $_ }
& $compiler -encoding UTF-8 --release 8 -Xlint:all,-options -d $classes @paths
if ($LASTEXITCODE -ne 0) { throw "Falha ao compilar a etapa 2." }
& $jar cfe (Join-Path $PSScriptRoot "CompiladorEtapa2.jar") Main -C $classes .
if ($LASTEXITCODE -ne 0) { throw "Falha ao empacotar o JAR." }
Write-Output "JAR da etapa 2 gerado com sucesso. Requer Java 8 ou superior para executar."
