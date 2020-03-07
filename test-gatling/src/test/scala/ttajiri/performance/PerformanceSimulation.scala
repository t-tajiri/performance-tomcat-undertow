package ttajiri.performance

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

class PerformanceSimulation extends Simulation {

  object Server {
    val launch: HttpRequestBuilder = http("request").get(System.getProperty("gatling.test.path", "/"))
  }

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(System.getenv("gatling.test.url"))
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val scn: ScenarioBuilder = scenario("Launch Performance").exec(Server.launch)

  setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))
}
