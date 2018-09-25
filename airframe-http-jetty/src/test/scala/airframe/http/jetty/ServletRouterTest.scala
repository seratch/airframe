/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package airframe.http.jetty

import okhttp3.{OkHttpClient, Request}
import wvlet.airframe.AirframeSpec
import wvlet.airframe.codec.JSONCodec
import wvlet.airframe.http.{Endpoint, Router, httpDefaultDesign}
import wvlet.log.LogSupport

class ServletRouterTest extends AirframeSpec {

  case class RichInfo(version: String, name: String, details: RichNestedInfo)
  case class RichNestedInfo(serverType: String)
  case class RichRequest(id: Int, name: String)

  trait MyApi extends LogSupport {
    @Endpoint(path = "/v1/info")
    def getInfo: String = {
      "hello MyApi"
    }

    @Endpoint(path = "/v1/rich_info")
    def getRichInfo: RichInfo = {
      RichInfo("0.1", "MyApi", RichNestedInfo("test-server"))
    }

//    @Endpoint(path = "/v1/future")
//    def futureString: Future[String] = {
//      Future.value("hello")
//    }
//
//    @Endpoint(path = "/v1/rich_info_future")
//    def futureRichInfo: Future[RichInfo] = {
//      Future.value(getRichInfo)
//    }
//
//    // An example to map JSON requests to objects
//    @Endpoint(path = "/v1/json_api")
//    def jsonApi(request: RichRequest): Future[String] = {
//      Future.value(request.toString)
//    }
  }

  val router = Router.of[MyApi]

  val design = httpDefaultDesign
      .bind[Router].toInstance(router)
      .bind[MyApi].toSingleton

  val httpClient = new OkHttpClient()

//  System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog")
//  System.setProperty("org.eclipse.jetty.LEVEL", "DEBUG")

  "RouterServlet" should {

    "work with Airframe" in {
      design.build[JettyServer] { server =>
        server.state should equal("STARTED")

        {
          // info
          val req = new Request.Builder().get.url("http://localhost:8888/v1/info").build()
          val resp = httpClient.newCall(req).execute()
          resp.code should equal(200)
          resp.body.string should equal("hello MyApi")
        }

        {
          // rich_info
          val req = new Request.Builder().get.url("http://localhost:8888/v1/rich_info").build()
          val resp = httpClient.newCall(req).execute()
          resp.code should equal(200)
          val json = """{"version":"0.1","name":"MyApi","details":{"serverType":"test-server"}}"""
          val responseBody = JSONCodec.unpackMsgPack(resp.body.bytes).get
          responseBody should equal(json)
        }
      }
    }
  }

}
