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

import wvlet.log.LogSupport
import wvlet.surface.Surface

import scala.util.{Failure, Success, Try}
import wvlet.airframe._

/**
  * Returns the available controller instance from the given Surface information.
  */
trait ControllerProvider {

  /**
    * Returns the actual controller instance if exists.
    */
  def findController(controllerSurface: Surface): Option[Any]
}

trait ControllerProviderFromSession extends ControllerProvider with LogSupport {
  private[this] val session = bind[Session]

  override def findController(controllerSurface: Surface): Option[Any] = {
    Try(session.getInstanceOf(controllerSurface)) match {
      case Success(controller) =>
        Some(controller)
      case Failure(e) =>
        warn(e)
        None
    }
  }
}
