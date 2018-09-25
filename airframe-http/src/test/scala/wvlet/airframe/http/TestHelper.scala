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

import java.nio.charset.StandardCharsets

object TestHelper {

  case class TextBodyAirframeRequest(method: HttpMethod,
                                     path: String,
                                     query: Map[String, String] = Map.empty,
                                     contentString: String = "")
    extends AirframeRequest[TextBodyAirframeRequest] {
    override def contentBytes: Array[Byte] = {
      contentString.getBytes(StandardCharsets.UTF_8)
    }
    override def raw: TextBodyAirframeRequest = this
  }

  def buildTextBodyRequest(method: HttpMethod,
                           path: String,
                           query: Map[String, String] = Map.empty,
                           contentString: String = ""): AirframeRequest[TextBodyAirframeRequest] = {
    TextBodyAirframeRequest(
      method = method,
      path = path,
      query = query,
      contentString = contentString
    )
  }

}
