import java.util.concurrent.locks.ReentrantLock

val ProjectName  = "gluon"

lazy val gluon = (project in file("."))
  .settings(settings: _*)

lazy val settings = Seq(
  name := ProjectName,

  Compile / mainClass / run := Some("gluon.Main"),

  autoScalaLibrary := false,
  crossPaths := false,

  libraryDependencies ++= Seq(
    "ca.mcgill.sable" % "soot"         % "4.1.0",
    "gnu.getopt"      % "java-getopt"  % "1.0.13",
    "org.slf4j"       % "slf4j-simple" % "1.6.1"
  ),

  Compile / resourceGenerators += Def.task {
    val javaSourceDir = (Compile / javaSource).value
    val resourceDir = (Compile / resourceManaged).value

    generateSableCC.value

    Seq(
      file("gluon") / "contract" / "parsing" / "lexer" / "lexer.dat",
      file("gluon") / "contract" / "parsing" / "parser" / "parser.dat"
    ).map { file =>
      val src = javaSourceDir.toPath.resolve(file.toPath).toFile
      val dest = resourceDir.toPath.resolve(file.toPath).toFile
      IO.copyFile(src, dest)
      dest
    }
  },

  Compile / sourceGenerators += Def.task {
    val javaSourceDir: File = (Compile / javaSource).value
    val grammarDir = javaSourceDir / "gluon" / "contract" / "parsing"

    generateSableCC.value

    (grammarDir ** "*.java").get
  },

  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", _*) => MergeStrategy.discard
    case "module-info.class" => MergeStrategy.first
    case path => (assembly / assemblyMergeStrategy).value(path)
  },

  cleanFiles ++= {
    val javaSourceDir = (Compile / javaSource).value
    val grammarDir = javaSourceDir / "gluon" / "contract" / "parsing"

    (baseDirectory.value ** "target").get ++
      Seq(
        grammarDir / "analysis",
        grammarDir / "lexer",
        grammarDir / "node",
        grammarDir / "parser"
      )
  },

  Universal / mappings += file("README.md") -> "README.md",

  run / fork := true
)

lazy val gluonTests =
  (project in file("test"))
    .settings(
      name := "test",

      autoScalaLibrary := false,
      crossPaths := false,

      javacOptions += "-g",

      Compile / javaSource := baseDirectory.value
    )

val generateSableCCLock: ReentrantLock = new ReentrantLock

def generateSableCC: Def.Initialize[Task[Unit]] = Def.task {
  import org.sablecc.sablecc.SableCC

  val javaSourceDir: File = (Compile / javaSource).value
  val grammarDir = javaSourceDir / "gluon" / "contract" / "parsing"
  val grammarFile = grammarDir / "grammar.sablecc"

  generateSableCCLock.lock()

  try {
    if (!(grammarDir / "parser" / "Parser.java").exists()) {
      SableCC.processGrammar(grammarFile.getAbsolutePath, javaSourceDir.getAbsolutePath)
    }
  } finally {
    generateSableCCLock.unlock()
  }
}

enablePlugins(JavaAppPackaging)

addCommandAlias("dist", "universal:packageXzTarball")
addCommandAlias("compileTests", "gluonTests/compile")
