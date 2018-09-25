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

import javax.annotation.{PostConstruct, PreDestroy}
import wvlet.log.LogSupport
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.webapp.WebAppContext
import wvlet.airframe.bind
import wvlet.airframe.http.{ControllerProvider, Router}

class JettyServer(routerServlet: RouterServlet) extends LogSupport {

  private[this] var server: Server = _

  private[this] var _port: Int = {
    // PORT: Heroku default env variable
    Option(System.getenv("PORT")).map(_.toInt).getOrElse(8888)
  }

  private[this] def refreshServer(): Unit = {
    stop()
    server = new Server(port)
  }

  def port(port: Int): JettyServer = {
    _port = port
    this
  }

  def port: Int = _port

  def state: String = server.getState

  @PostConstruct
  def start(): Unit = {
    refreshServer()

    val context = new WebAppContext()
    val contextPath = "/"
    context.setContextPath(contextPath)
    context.setWar({
      val domain = this.getClass.getProtectionDomain
      val location = domain.getCodeSource.getLocation
      location.toExternalForm
    })
    context.addServlet(new ServletHolder(routerServlet), "/")
    server.setHandler(context)
    server.start
  }

  @PreDestroy
  def stop(): Unit = {
    if (server != null) {
      server.stop()
    }
  }

  def waitServerTermination: Unit = {
    start()
    server.join
  }

}

trait RouterServletFactory {

  private val controllerProvider = bind[ControllerProvider]

  def routerServlet(router: Router): RouterServlet = {
    new RouterServlet(router, controllerProvider)
  }
}
