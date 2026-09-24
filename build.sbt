ThisBuild / organization := "eu.ww86"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6" // LTS

lazy val munitVersion     = "1.1.1"
lazy val laminarVersion   = "17.2.1"
lazy val scalatagsVersion = "0.13.1"

// Pure logic and the site model. Cross-compiled, so the same tests run on the JVM and on Scala.js.
lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "core",
    libraryDependencies += "org.scalameta" %%% "munit" % munitVersion % Test
  )

// Static site generator: runs on the JVM at build time and writes plain HTML.
lazy val gen = project
  .in(file("gen"))
  .dependsOn(core.jvm)
  .settings(
    name := "gen",
    libraryDependencies ++= Seq(
      "com.lihaoyi"   %% "scalatags" % scalatagsVersion,
      "org.scalameta" %% "munit"     % munitVersion % Test
    )
  )

// Browser code: Laminar widgets mounted into containers of the generated pages.
lazy val web = project
  .in(file("web"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core.js)
  .settings(
    name                            := "web",
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "com.raquo" %%% "laminar" % laminarVersion
  )

lazy val buildSite = taskKey[File]("Assemble the whole site into target/site")

lazy val root = project
  .in(file("."))
  .aggregate(core.jvm, core.js, gen, web)
  .settings(
    name           := "ww86",
    publish / skip := true,
    buildSite := {
      val log = streams.value.log
      val out = target.value / "site"

      // Scala.js: with the default NoModule kind the linker report holds exactly one public module.
      val report    = (web / Compile / fullLinkJS).value.data
      val linkerOut = (web / Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value
      val script    = linkerOut / report.publicModules.head.jsFileName

      IO.delete(out)
      IO.createDirectory(out / "js")
      IO.copyDirectory(baseDirectory.value / "static", out)
      IO.copyFile(script, out / "js" / "main.js")

      // Every subdirectory of lab/ is a self-contained artifact, copied verbatim.
      (baseDirectory.value / "lab").listFiles.toSeq.filter(_.isDirectory).foreach { artifact =>
        IO.copyDirectory(artifact, out / "lab" / artifact.getName)
      }

      val classpath = (gen / Compile / fullClasspath).value.files
      (gen / run / runner).value
        .run("eu.ww86.gen.generate", classpath, Seq(out.getAbsolutePath), log)
        .get

      log.info(s"site assembled in $out")
      out
    }
  )
