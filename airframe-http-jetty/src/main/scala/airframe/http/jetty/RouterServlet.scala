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

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import wvlet.airframe.http.{ControllerProvider, Router}

class RouterServlet(router: Router,
                    controllerProvider: ControllerProvider) extends HttpServlet {

  override def service(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val airframeRequest = ServletAirframeRequest(req)
    router.findRoute(airframeRequest) match {
      case Some(route) =>
        controllerProvider.findController(route.controllerSurface) match {
          case Some(controller) =>
            val args   = route.buildControllerMethodArgs(airframeRequest)
            val result = route.call(controller, args)
            val handler: JettyResponseHandler = new JettyResponseHandler {
              override def httpServletResponse = resp
            }
            handler.toHttpResponse(req, route.returnTypeSurface, result)
          case _ => super.service(req, resp)
        }
      case _ => super.service(req, resp)
    }
  }

}
