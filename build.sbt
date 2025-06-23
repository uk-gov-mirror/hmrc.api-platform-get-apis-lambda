lazy val appName = "api-platform-get-apis-lambda"
lazy val appDependencies: Seq[ModuleID] = compileDependencies ++ testDependencies

lazy val compileDependencies = Seq(
  "uk.gov.hmrc" %% "aws-gateway-proxied-request-lambda" % "0.14.0",
  "uk.gov.hmrc" %% "api-platform-manage-api" % "0.48.0"
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest"                % "3.2.18",
  "org.mockito"   %% "mockito-scala-scalatest"  % "1.17.29"
).map(_ % Test)

lazy val plugins: Seq[Plugins] = Seq()

lazy val lambda = (project in file("."))
  .enablePlugins(plugins: _*)
  .settings(
    name := appName,
    scalaVersion := "2.13.12",
    libraryDependencies ++= appDependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    retrieveManaged := true
  )
  .settings(
    resolvers += "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/",
    resolvers += Resolver.jcenterRepo
  )
  .settings(
    assemblyOutputPath in assembly := file(s"./$appName.zip"),
    assemblyMergeStrategy in assembly := {
      case path if path.endsWith("io.netty.versions.properties") => MergeStrategy.discard
      case path if path.endsWith("BuildInfo$.class") => MergeStrategy.discard
      case path =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(path)
    }
  )

// Coverage configuration
coverageMinimumStmtTotal := 85
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>"
