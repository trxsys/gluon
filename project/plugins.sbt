addSbtPlugin("com.typesafe.sbt" %  "sbt-native-packager" % "1.2.2")

unmanagedJars in Compile += file("tools/sablecc.jar")

