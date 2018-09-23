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
package wvlet.airframe.http

import wvlet.surface

import scala.reflect.runtime.{universe => ru}

/**
  * Provides mapping from HTTP requests to controller methods (= Route)
  * @param routes
  */
class Router(val routes: Seq[Route]) {

  def findRoute[A](request: HttpRequest[A]): Option[Route] = {
    routes
      .find { r =>
        r.method == request.method &&
        r.pathComponents.length == request.pathComponents.length &&
        request.path.startsWith(r.pathPrefix)
      }
  }

  /**
    * Add methods annotated with @Endpoint to the routing table
    */
  def add[Controller: ru.TypeTag]: Router = {
    // Import ReflectSurface to find method annotations (Endpoint)
    import wvlet.surface.reflect._

    // Check prefix
    val serviceSurface = surface.of[Controller]
    val prefixPath =
      serviceSurface
        .findAnnotationOf[Endpoint]
        .map(_.path())
        .getOrElse("")

    val newRoutes =
      surface
        .methodsOf[Controller]
        .map(m => (m, m.findAnnotationOf[Endpoint]))
        .collect {
          case (m: ReflectMethodSurface, Some(endPoint)) =>
            Route(serviceSurface, endPoint.method(), prefixPath + endPoint.path(), m)
        }

    new Router(routes ++ newRoutes)
  }
}

object Router {
  def empty: Router                      = Router()
  def of[Controller: ru.TypeTag]: Router = apply().add[Controller]
  def apply(): Router                    = new Router(Seq.empty)
}
