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
  * Handles the conversion of the response value to actual HTTP response.
  */
trait ResponseHandler[RawReq, RawResp] {

  /**
    * Converts the result value to actual HTTP response.
    */
  def toHttpResponse[RespValue](request: RawReq, responseTypeSurface: Surface, response: RespValue): RawResp
}
