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
package airframe.http

import java.net.URLDecoder

import javax.servlet.http.HttpServletRequest
import wvlet.airframe.http.{HttpMethod, HttpRequest}

package object jetty {

  implicit class ServletAirframeRequest(val underlying: HttpServletRequest)
    extends HttpRequest[HttpServletRequest] {

    override lazy val method: HttpMethod = underlying.getMethod match {
      case "GET" => HttpMethod.GET
      case "POST" => HttpMethod.POST
      case "DELETE" => HttpMethod.DELETE
      case "PUT" => HttpMethod.PUT
      case _ => throw new UnsupportedOperationException // TODO: add more
    }
    override val path: String = underlying.getRequestURI

    override val query: Map[String, String] = {
      val params = new scala.collection.mutable.HashMap[String, Seq[String]]()
      val qs = Option(underlying.getQueryString)
      qs match {
        case Some(q) =>
          q.split("&").map { param: String =>
            val pair: Array[String] = param.split("=")
            if (pair.length < 2) {
              null
            } else {
              val key = URLDecoder.decode(pair(0), "UTF-8")
              val value = URLDecoder.decode(pair(1), "UTF-8")
              params.get(key) match {
                case Some(existingValues) => params.put(key, existingValues :+ value)
                case _ => params.put(key, Seq(value))
              }
            }
          }
        case _ =>
      }
      // TODO: really ok to omit other values?
      params.mapValues(_.head).toMap
    }

    override lazy val contentString: String = {
      val enc = {
        val encoding = underlying.getCharacterEncoding
        if (encoding == null || encoding.trim.length == 0) {
          if (Option(underlying.getContentType).exists(_.equalsIgnoreCase("application/json"))) "UTF-8"
          else "ISO-8859-1"
        } else {
          encoding
        }
      }
      new String(contentBytes, enc)
    }

    override lazy val contentBytes: Array[Byte] = {
      val is = underlying.getInputStream
      Stream.continually(is.read).takeWhile(_ != -1).map(_.toByte).toArray
    }

    override def toRaw: HttpServletRequest = underlying
  }

}
