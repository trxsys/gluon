addSbtPlugin("com.typesafe.sbt" %  "sbt-native-packager" % "1.2.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.8")

unmanagedJars in Compile += file("tools/sablecc.jar")
