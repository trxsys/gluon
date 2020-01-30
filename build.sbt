import java.util.concurrent.locks.ReentrantLock

val ProjectName  = "gluon"

lazy val gluon = (project in file("."))
  .settings(settings: _*)

lazy val settings = Seq(
  name := ProjectName,

  mainClass in (Compile, run) := Some("gluon.Main"),

  autoScalaLibrary := false,
  crossPaths := false,

  libraryDependencies ++= Seq(
    "ca.mcgill.sable" % "soot"        % "4.0.0",
    "gnu.getopt"      % "java-getopt" % "1.0.13"
  ),

  resourceGenerators in Compile += Def.task {
    val javaSourceDir = (javaSource in Compile).value
    val resourceDir = (resourceManaged in Compile).value

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

  sourceGenerators in Compile += Def.task {
    val javaSourceDir: File = (javaSource in Compile).value
    val grammarDir = javaSourceDir / "gluon" / "contract" / "parsing"

    generateSableCC.value

    (grammarDir ** "*.java").get
  },

  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", _*) => MergeStrategy.discard
    case "module-info.class" => MergeStrategy.first
    case path => (assemblyMergeStrategy in assembly).value(path)
  },

  cleanFiles ++= {
    val javaSourceDir = (javaSource in Compile).value
    val grammarDir = javaSourceDir / "gluon" / "contract" / "parsing"

    (baseDirectory.value ** "target").get ++
      Seq(
        grammarDir / "analysis",
        grammarDir / "lexer",
        grammarDir / "node",
        grammarDir / "parser"
      )
  },

  mappings in Universal += file("README.md") -> "README.md"
)

lazy val gluonTests =
  (project in file("test"))
    .settings(
      name := "test",

      autoScalaLibrary := false,
      crossPaths := false,

      javacOptions += "-g",

      javaSource in Compile := baseDirectory.value
    )

val generateSableCCLock: ReentrantLock = new ReentrantLock

def generateSableCC: Def.Initialize[Task[Unit]] = Def.task {
  import org.sablecc.sablecc.SableCC

  val javaSourceDir: File = (javaSource in Compile).value
  val grammarDir = javaSourceDir / "gluon" / "contract" / "parsing"
  val grammarFile = grammarDir / "grammar.sablecc"

  generateSableCCLock.lock()

  try {
    if (!(grammarDir / "parser").exists()) {
      SableCC.processGrammar(grammarFile.getAbsolutePath, javaSourceDir.getAbsolutePath)
    }
  } finally {
    generateSableCCLock.unlock()
  }
}

enablePlugins(JavaAppPackaging)

addCommandAlias("dist", "universal:packageXzTarball")
addCommandAlias("compileTests", "gluonTests/compile")
