addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.mojolly.scalate" % "xsbt-scalate-generator" % "0.4.2")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.9.0")

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.3")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.6"

javaOptions += "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" //For debugging