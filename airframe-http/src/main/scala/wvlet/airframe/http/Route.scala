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

import wvlet.airframe.codec.PrimitiveCodec.StringCodec
import wvlet.airframe.codec.{JSONCodec, MessageCodec}
import wvlet.log.LogSupport
import wvlet.surface.{Surface, Zero}
import wvlet.surface.reflect._

case class Route(controllerSurface: Surface, method: HttpMethod, path: String, methodSurface: ReflectMethodSurface)
    extends LogSupport {
  require(
    path.startsWith("/"),
    s"Invalid route path: ${path}. EndPoint path must start with a slash (/) in ${methodSurface.owner.name}:${methodSurface.name}")

  val pathComponents: IndexedSeq[String] = {
    path
      .substring(1)
      .split("/")
      .toIndexedSeq
  }

  lazy val pathPrefix: String = {
    "/" +
      pathComponents
        .takeWhile(!_.startsWith(":"))
        .mkString("/")
  }

  def returnTypeSurface: Surface = methodSurface.returnType

  /**
    * Extracting path parameter values. For example, /user/:id with /user/1 gives { id -> 1 }
    */
  private def extractPathParams[A](request: HttpRequest[A]): Map[String, String] = {
    val pathParams = (for ((elem, actual) <- pathComponents.zip(request.pathComponents) if elem.startsWith(":")) yield {
      elem.substring(1) -> actual
    }).toMap[String, String]
    pathParams
  }

  /**
    * Find a corresponding controller and call the matching methods
    * @param request
    * @return
    */
  def buildControllerMethodArgs[A](request: HttpRequest[A]): Seq[Any] = {
    // Collect URL query parameters and other parameters embedded inside URL.
    val requestParams = request.query ++ extractPathParams(request)

    // Build the function arguments
    val methodArgs: Seq[Any] =
      for (arg <- methodSurface.args) yield {
        arg.surface.rawType match {
          case cl if classOf[HttpRequest[_]].isAssignableFrom(cl) =>
            // Bind the current http request instance
            request
          case _ =>
            // Build from the string value in the request params
            val argCodec = MessageCodec.defautlFactory.of(arg.surface)
            val v: Option[Any] = requestParams.get(arg.name) match {
              case Some(paramValue) =>
                // String parameter to the method argument
                argCodec.unpackMsgPack(StringCodec.toMsgPack(paramValue))
              case None =>
                // Build from the content body
                val contentBytes = request.contentBytes
                if (contentBytes.nonEmpty) {
                  // JSON -> msgpack -> argument
                  val msgpack = JSONCodec.toMsgPack(contentBytes)
                  argCodec.unpackMsgPack(msgpack)
                } else {
                  // return the method default argument if exists
                  arg.getDefaultValue
                }
            }
            // If mapping fails, use the zero value
            v.getOrElse(Zero.zeroOf(arg.surface))
        }
      }
    trace(s"(${methodSurface.args.mkString(", ")}) <=  [${methodArgs.mkString(", ")}]")
    methodArgs
  }

  def call(controller: Any, methodArgs: Seq[Any]): Any = {
    methodSurface.call(controller, methodArgs: _*)
  }

  def call[A](controllerProvider: ControllerProvider, request: HttpRequest[A]): Option[Any] = {
    controllerProvider.findController(controllerSurface).map { controller =>
      call(controller, buildControllerMethodArgs(request))
    }
  }
}
