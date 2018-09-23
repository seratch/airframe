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
package wvlet.airframe.http.finagle

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, ListeningServer, Service, SimpleFilter}
import com.twitter.util.{Await, Future}
import javax.annotation.{PostConstruct, PreDestroy}
import wvlet.airframe.http.finagle.FinagleServer.FinagleService
import wvlet.log.LogSupport

/**
  *
  */
class FinagleServer(finagleConfig: FinagleServerConfig, finagleService: FinagleService) extends LogSupport {
  protected[this] var server: Option[ListeningServer] = None

  def port: Int = finagleConfig.port

  @PostConstruct
  def start {
    info(s"Starting a server at http://localhost:${finagleConfig.port}")
    server = Some(Http.serve(s":${finagleConfig.port}", finagleService))
  }

  @PreDestroy
  def stop = {
    info(s"Stopping the server http://localhost:${finagleConfig.port}")
    server.map(_.close())
  }

  def waitServerTermination: Unit = {
    server.map(s => Await.ready(s))
  }
}

object FinagleServer extends LogSupport {
  type FinagleService = Service[Request, Response]

  def defaultService(router: FinagleRouter): FinagleService = {
    FinagleServer.defaultRequestLogger andThen
      FinagleServer.defaultErrorHandler andThen
      router andThen
      FinagleServer.notFound
  }

  /**
    * A simple error handler for wrapping exceptions as InternalServerError (500)
    */
  def defaultErrorHandler: SimpleFilter[Request, Response] =
    (request: Request, service: Service[Request, Response]) => {
      service(request).rescue {
        case e: Throwable =>
          logger.warn(e.getMessage)
          logger.trace(e)
          Future.value(Response(Status.InternalServerError))
      }
    }

  /**
    * Simple logger for logging http requests and responses to stderr
    */
  def defaultRequestLogger: SimpleFilter[Request, Response] =
    (request: Request, service: Service[Request, Response]) => {
      logger.trace(request)
      service(request).map { response =>
        logger.trace(response)
        response
      }
    }

  /**
    * A fallback service if FinagleRouter cannot find any matching endpoint
    */
  def notFound: Service[Request, Response] = (_: Request) => {
    Future.value(Response(Status.NotFound))
  }
}
