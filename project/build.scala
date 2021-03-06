import sbt._
import sbt.Keys._
import org.scalatra.sbt._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._
import com.earldouglas.xsbtwebplugin.PluginKeys._

object WebsiteBuild extends Build {
  val Organization = "ru.meridor"
  val Name = "website"
  val Version = "0.2.5"
  val ScalaVersion = "2.10.0"
  val ScalatraVersion = "2.2.2"

  lazy val project = Project (
    "website",
    file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      scalacOptions ++= Seq("-feature", "-deprecation"),
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test" exclude("org.specs2", "specs2_2.10"),
        "org.specs2" % "specs2_2.10" % "2.3.10" % "test",
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.json4s"   %% "json4s-jackson" % "3.2.4",
        "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
        "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts Artifact("javax.servlet", "jar", "jar"),
        "com.jolbox" % "bonecp" % "0.7.1.RELEASE",
        "com.typesafe.slick" % "slick_2.10" % "1.0.0",
        "com.google.code" % "sitemapgen4j" % "1.0.1",
        "com.lowagie" % "itext" % "4.2.1",
        "org.fusesource.scalamd" % "scalamd_2.10" % "1.6"
      ).map(_.excludeAll(ExclusionRule(organization = "commons-logging"))),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      },
      warPostProcess in Compile <<= target map {
        t => {
          () =>
            val webapp = t / "webapp"
            IO.delete(webapp / "sitemap.xml")
        }
      }
    )
  ) dependsOn diana settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*) settings(YuiCompressorTaskProvider.settings: _*)

  lazy val diana = ProjectRef(uri("../diana"), "diana")
}

/**
  * This is a custom YUI compressor wrapper that  
  */
private object YuiCompressorTaskProvider {

  import sbt._
  import Keys._

  object YuiCompressorTaskProviderKeys {
    lazy val jsInputDirectory = SettingKey[File]("meridor-yui-js-input-directory", "JS files input directory")
    lazy val cssInputDirectory = SettingKey[File]("meridor-yui-css-input-directory", "CSS files input directory")
    lazy val jsOutputFile = SettingKey[File]("meridor-yui-js-output-file", "JS output file")
    lazy val cssOutputFile = SettingKey[File]("meridor-yui-css-output-file", "CSS output file")
    lazy val jsCompressor = TaskKey[Seq[File]]("meridor-yui-js-compressor", "JS compressor task")
    lazy val cssCompressor = TaskKey[Seq[File]]("meridor-yui-css-compressor", "CSS compressor task")
  }

  import YuiCompressorTaskProviderKeys._

  private val compressorClass = "com.yahoo.platform.yui.compressor.YUICompressor"

  private def compressFiles(inputDirectory: File, wildcard: String, outputFile: File, options: Seq[String], classpath: Seq[File], runner: ScalaRun, log: Logger) = {
    import org.apache.commons.io.filefilter.WildcardFileFilter
    import java.util.Date
    log.info("Compressing files from " + inputDirectory.getAbsolutePath + " with wildcard " + wildcard + " and saving them to " + outputFile.getAbsoluteFile + "...")
    IO.createDirectory(outputFile.getParentFile)
    val filter: java.io.FileFilter = new WildcardFileFilter(wildcard)
    val files: Array[java.io.File] = inputDirectory.listFiles(filter)
    val inputFilePaths = files.filter(_.isFile).sortBy(_.getAbsolutePath).map(f => f.absolutePath)
    val outputFilePath = outputFile.absolutePath
    if (outputFile.exists()){
      IO.delete(outputFile)
      IO.touch(outputFile)
    }
    for (inputFilePath <- inputFilePaths){
      val tmpFilePath = outputFilePath + "." + new Date().getTime
      val tmpFile = file(tmpFilePath)
      runner.run(compressorClass, classpath, options ++ Seq("-o", tmpFilePath, inputFilePath), log)
      IO.append(outputFile, IO.read(tmpFile))
      IO.delete(tmpFile)
    }
    Seq(outputFile)
  }

  private def compressFilesTask(inputDirectory: SettingKey[File], wildcard: String, fileType: String, outputFile: SettingKey[File], task: TaskKey[Seq[File]]) =
    (inputDirectory, outputFile, state in task, runner in task, streams) map {
      (id, of, stt, rnr, str) => {
        val classpath = Project.extract(stt).currentUnit.classpath
        val log = str.log
        var opts = Seq("--type", fileType)
        if (fileType == "js"){
          opts :+= "--nomunge"
          opts :+= "--disable-optimizations"
        }
        compressFiles(id, wildcard, of, opts, classpath, rnr, log)
      }
    }

  private def compressJsFilesTask(inputDirectory: SettingKey[File], outputFile: SettingKey[File]) =
    compressFilesTask(inputDirectory, "*.js", "js", outputFile, jsCompressor)

  private def compressCSSFilesTask(inputDirectory: SettingKey[File], outputFile: SettingKey[File]) =
    compressFilesTask(inputDirectory, "*.css", "css", outputFile, cssCompressor)

  private lazy val allSettings: Seq[Setting[_]] = Seq(
    jsInputDirectory <<= (sourceDirectory in Runtime)( _ / "js"),
    cssInputDirectory <<= (sourceDirectory in Runtime)( _ / "css"),
    jsOutputFile <<= (sourceDirectory in Runtime)( _ / "webapp/script22.js"),
    cssOutputFile <<= (sourceDirectory in Runtime)( _ / "webapp/style25.css"),
    jsCompressor <<= compressJsFilesTask(jsInputDirectory, jsOutputFile),
    cssCompressor <<= compressCSSFilesTask(cssInputDirectory, cssOutputFile),
    resourceGenerators <+= jsCompressor,
    resourceGenerators <+= cssCompressor
  )

  lazy val settings: Seq[Setting[_]] = inConfig(Compile)(allSettings) ++ inConfig(Test)(allSettings)

}


