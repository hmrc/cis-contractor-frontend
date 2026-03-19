import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "cis-contractor-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings) *)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    scalaSettings,
    defaultSettings(),
    Test / parallelExecution := false,
    Test / fork := false,
    routesGenerator := InjectedRoutesGenerator,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort := 6998,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*components.*;" +
      ".*Routes.*;.*viewmodels.govuk.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 78,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Wconf:src=html/.*:s",
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    ),
    scalacOptions := scalacOptions.value.distinct,
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(concat),
    columnarFmtConfig := Seq(
      ColumnarConfig(
        sections        = appRoutesSections,
        lineLimit       = 160 * 1,
        fileGlob        = "conf/app.routes",
        fileHeader      = "# Routes",
        formatterConfig = ColumnarFormatterConfig.playRoutes
      ),
      ColumnarConfig(
        sections        = messagesSections,
        lineLimit       = 120 * 1,
        fileGlob        = "conf/messages.*",
        formatterConfig = ColumnarFormatterConfig(
          parse        = ColumnarFormatterConfig.playRoutes.parse,
          primaryCol   = 0,
          secondaryCol = 0,
          dedupeKey    = cols => cols(0),
          subkeyFn     = cols => cols(0).takeWhile(_ != '.')
        )
      )
    )
  )
lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")

lazy val messagesSections = Seq(
  ColumnarSection("# Infrastructure",
    primaryPrefixes = Seq("service", "site", "date", "error", "timeout", "index",
                          "checkYourAnswers", "journeyRecovery", "signedOut")),
  ColumnarSection("# Errors & Auth",
    primaryPrefixes = Seq("pageNotFound", "systemError", "accessDenied", "unauthorised")),
  ColumnarSection("# Individual and Common"),
  ColumnarSection("# Partnership",
    primaryPrefixes = Seq("partnership")),
  ColumnarSection("# Company",
    primaryPrefixes = Seq("company"))
)

lazy val appRoutesSections = Seq(
  ColumnarSection("# Infrastructure"),
  ColumnarSection("# Errors & Auth",
    primaryPrefixes = Seq("/there-is-a-problem", "/page-not-found", "/access-denied",
      "/account/", "/unauthorised", "/system-error/")),
  ColumnarSection("# Individual",
    primaryPrefixes = Seq("/add/")),
  ColumnarSection("# Company",
    primaryPrefixes   = Seq("/add/company/"),
    secondaryPrefixes = Seq("controllers.add.company.")),
  ColumnarSection("# Partnership",
    primaryPrefixes   = Seq("/add/partnership/"),
    secondaryPrefixes = Seq("controllers.add.partnership."))
)
