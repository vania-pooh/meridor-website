import sbt._
import Keys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import com.earldouglas.xsbtwebplugin.WebPlugin
import com.earldouglas.xsbtwebplugin.PluginKeys._
import net.virtualvoid.sbt.graph.Plugin._

object WebsiteBuild extends Build {

  val Organization = "ru.meridor"
  val Name = "website"
  val Version = "0.3.0"
  val ScalaVersion = "2.10.0"
  val ScalatraVersion = "2.2.2"

  lazy val root = project
    .in(file("."))
    .settings(
      Defaults.defaultSettings ++ Seq(
        organization := Organization,
        name := Name,
        version := Version,
        scalaVersion := ScalaVersion
      ): _*
    )
    .settings(graphSettings: _*)
    .aggregate (tools, model, frontend, backend)

  lazy val tools = project
    .in(file("modules/tools"))
    .settings(
      Defaults.defaultSettings ++ Seq(
        organization := Organization,
        name := Name + "-tools",
        version := Version,
        scalaVersion := ScalaVersion,
        libraryDependencies ++= Seq(
          "org.scalatra" %% "scalatra" % ScalatraVersion,
          "com.googlecode.flyway" % "flyway-core" % "2.1.1",
          "org.json4s" %% "json4s-jackson" % "3.2.4",
          "org.json4s" %% "json4s-ext" % "3.2.4",
          "org.apache.commons" % "commons-io" % "1.3.2",
          "org.apache.commons" % "commons-lang3" % "3.1",
          "org.apache.httpcomponents" % "httpclient" % "4.2.5",
          "org.slf4j" % "slf4j-api" % "1.7.7",
          "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided;test" artifacts Artifact("javax.servlet", "jar", "jar")
        )
      ): _*
    )
    .settings(graphSettings: _*)

  lazy val model = project
    .in(file("modules/model"))
    .dependsOn(tools)
    .settings(
      Defaults.defaultSettings ++ Seq(
        organization := Organization,
        name := Name + "-model",
        version := Version,
        scalaVersion := ScalaVersion,
        libraryDependencies ++= Seq(
          "com.jolbox" % "bonecp" % "0.7.1.RELEASE",
          "com.typesafe.slick" %% "slick" % "2.0.2",
          "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
        ),
        slick <<= slickCodeGenTask, //manual sbt command
        sourceGenerators in Compile <+= slickCodeGenTask //automatic code generation on every compile
      ): _*
    )
    .settings(graphSettings: _*)

  lazy val slick = TaskKey[Seq[File]]("gen-tables")
  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
    val outputDir = (dir / "model").getPath
    val url = "jdbc:postgresql://localhost/meridor"
    val jdbcDriver = "org.postgresql.Driver"
    val slickDriver = "scala.slick.driver.PostgresDriver"
    val pkg = "ru.meridor.website.db.tables"
    val user = "meridor"
    val password = "meridor"
    toError(r.run("scala.slick.model.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg, user, password), s.log))
    val fname = outputDir + "/ru/meridor/website/db/tables/Tables.scala"
    Seq(file(fname))
  }

  lazy val frontend = project
    .in(file("modules/frontend"))
    .dependsOn(tools, model)
    .settings(
      Defaults.defaultSettings ++ scalateSettings ++ WebPlugin.webSettings ++ Seq(
        organization := Organization,
        name := Name + "-frontend",
        version := Version,
        scalaVersion := ScalaVersion,
        port in WebPlugin.container.Configuration := 8080,
        resolvers += Classpaths.typesafeReleases,
        libraryDependencies ++= Seq(
          "org.scalatra" %% "scalatra" % ScalatraVersion,
          "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
          "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test" exclude("org.specs2", "specs2_2.10"),
          "org.fusesource.scalamd" % "scalamd_2.10" % "1.6",
          "org.mockito" % "mockito-all" % "1.9.5" % "test",
          "org.specs2" % "specs2_2.10" % "2.3.10" % "test",
          "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
          "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
          "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container",
          "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts Artifact("javax.servlet", "jar", "jar"),
          "com.google.code" % "sitemapgen4j" % "1.0.1"
        ).map(_.excludeAll(ExclusionRule(organization = "commons-logging"))),
        scalateTemplateConfig in Compile <<= (sourceDirectory in Compile in thisProject){ base =>
          Seq(
            TemplateConfig(
              base / "webapp" / "WEB-INF" / "templates",
              Seq.empty,
              Seq(
                Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
              ),
              Some("templates")
            )
          )
        },
        warPostProcess in Compile <<= target map {
          t => {
            (file) =>
              val webapp = t / "webapp"
              IO.delete(webapp / "sitemap.xml")
          }
        }
      ): _*
    )
    .settings(graphSettings: _*)

  lazy val backend = project
    .in(file("modules/backend"))
    .dependsOn(tools, model)
    .settings(
      Defaults.defaultSettings ++ WebPlugin.webSettings ++ Seq(
        organization := Organization,
        name := Name + "-backend",
        version := Version,
        scalaVersion := ScalaVersion,
        port in WebPlugin.container.Configuration := 9090,
        resolvers += Classpaths.typesafeReleases,
        libraryDependencies ++= Seq(
          "org.scalatra" %% "scalatra" % ScalatraVersion,
          "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test" exclude("org.specs2", "specs2_2.10"),
          "org.scalatra" %% "scalatra-swagger"    % ScalatraVersion,
          "org.mockito" % "mockito-all" % "1.9.5" % "test",
          "org.specs2" % "specs2_2.10" % "2.3.10" % "test",
          "org.scalatra" %% "scalatra-json" % ScalatraVersion,
          "org.json4s"   %% "json4s-jackson" % "3.2.4",
          "org.json4s"   %% "json4s-ext" % "3.2.4",
          "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
          "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
          "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container",
          "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts Artifact("javax.servlet", "jar", "jar"),
          "com.jolbox" % "bonecp" % "0.7.1.RELEASE",
          "com.typesafe.slick" % "slick_2.10" % "1.0.0",
          "com.lowagie" % "itext" % "4.2.1"
        ).map(_.excludeAll(ExclusionRule(organization = "commons-logging"))),
        scalateTemplateConfig in Compile <<= (sourceDirectory in Compile in thisProject){ base =>
          Seq(
            TemplateConfig(
              base / "webapp" / "WEB-INF" / "templates",
              Seq.empty,
              Seq(
                Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
              ),
              Some("templates")
            )
          )
        }

      ): _*
    )
    .settings(graphSettings: _*)

}