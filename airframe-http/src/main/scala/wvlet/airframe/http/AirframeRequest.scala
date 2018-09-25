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

/**
  * Wrapper object of an HTTP request in airframe-http.
  * @tparam RawReq the type of raw HTTP request object
  */
trait AirframeRequest[RawReq] {

  /**
    * Returns the HTTP method used.
    */
  def method: HttpMethod

  /**
    * Returns the path the request reached to.
    */
  def path: String

  /**
    * Returns the elements of the path value split by slash char.
    */
  lazy val pathComponents: IndexedSeq[String] = {
    path.replaceFirst("/", "").split("/").toIndexedSeq
  }

  /**
    * Returns a Map value built from query string parameters.
    *
    * TODO: multiple values using the same key.
    */
  def query: Map[String, String]

  /**
    * Returns the body if the value is a text message.
    *
    * TODO: to be determined
    * Otherwise, this method returns null value?
    */
  def contentString: String

  /**
    * Returns the request body as a byte array regardless of the format of the body content.
    */
  def contentBytes: Array[Byte]

  /**
    * Returns a raw HTTP request object
    * (e.g. Finagle's Request, Servlet's ServletHTTPRequest, or their wrapper)
    */
  def raw: RawReq

}
