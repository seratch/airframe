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

import javax.servlet.ServletOutputStream
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import wvlet.airframe.codec.MessageCodec
import wvlet.airframe.http.ResponseHandler
import wvlet.log.LogSupport
import wvlet.surface.Surface

import scala.util.control.NonFatal

trait JettyResponseHandler extends ResponseHandler[HttpServletRequest, HttpServletResponse] with LogSupport {

  def httpServletResponse(): HttpServletResponse

  /**
    * Converts the result value to actual HTTP response.
    */
  override def toHttpResponse[RespValue](request: HttpServletRequest,
                                         responseTypeSurface: Surface,
                                         response: RespValue): HttpServletResponse = {
    response match {
      case servletResp: HttpServletResponse => servletResp
      case str: String =>
        // TODO: Content-Type: plain text, json, xml, etc..
        httpServletResponse.getWriter.write(str)
        httpServletResponse
      case otherToBeMsgpack: RespValue =>
        // Convert the response object into JSON
        val resp = httpServletResponse()
        resp.setHeader("Content-Type", "application/x-msgpack")
        val out: ServletOutputStream = resp.getOutputStream
        try {
          val m: MessageCodec[_] = mapCodecFactory.of(responseTypeSurface)
          val bytes: Array[Byte] = m.asInstanceOf[MessageCodec[RespValue]].toMsgPack(otherToBeMsgpack)
          out.write(bytes)
          resp
        } finally {
          try {
            if (out != null) out.close()
          } catch { case NonFatal(e) => warn("Failed to close output stream", e) }
        }
    }
  }

}
