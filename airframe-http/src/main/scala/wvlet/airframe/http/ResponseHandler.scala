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

import wvlet.surface.Surface

/**
  * A function interface which handles HTTP requests.
  *
  * @tparam RawReq the type which represents an HTTP request
  * @tparam RawResp the type which represents the corresponding response to an HTTP request.
  */
trait ResponseHandler[RawReq, RawResp] {

  /**
    * Receives a given HTTP request, then returns the corresponding response.
    * @param request an HTTP request
    * @param responseTypeSurface Surface object to extract the type of response
    * @param responseValue the expected value of the value included in the response
    * @tparam ResV the expected type of the value included in the response
    * @return
    */
  def toHttpResponse[ResV](request: RawReq, responseTypeSurface: Surface, responseValue: ResV): RawResp
}
