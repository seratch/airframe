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

import com.twitter.finagle.http.{Request, Response}
import wvlet.airframe.http.{ControllerProvider, ResponseHandler, Router}
import wvlet.airframe._
import wvlet.airframe.http.finagle.FinagleServer.FinagleService

trait FinagleServerFactory {
  private val controllerProvider = bind[ControllerProvider]
  private val responseHandler    = bind[ResponseHandler[Request, Response]]

  /**
    * Override this method to customize finagle service filters
    */
  protected def newService(finagleRouter: FinagleRouter): FinagleService = FinagleServer.defaultService(finagleRouter)

  def newFinagleServer(port: Int, router: Router): FinagleServer = {
    val finagleRouter = new FinagleRouter(router, controllerProvider, responseHandler)
    new FinagleServer(finagleConfig = FinagleServerConfig(port = port), finagleService = newService(finagleRouter))
  }
}
