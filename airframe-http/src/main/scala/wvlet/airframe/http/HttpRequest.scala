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
  * HTTP request abstraction in airframe-http.
  * @tparam Req the raw HTTP request type
  */
trait HttpRequest[Req] {

  /**
    * Returns the HTTP method which receives the HTTP request.
    */
  def method: HttpMethod

  /**
    * Returns the path which receives the HTTP request.
    */
  def path: String

  /**
    * Returns the array of String values split from the path value.
    */
  lazy val pathComponents: IndexedSeq[String] = {
    path.replaceFirst("/", "").split("/").toIndexedSeq
  }

  /**
    * Returns a Map value built from the given query string parameters.
    */
  def query: Map[String, String]

  /**
    * Returns the whole request body as a String value.
    */
  def contentString: String

  /**
    * Returns the request body as a byte array.
    */
  def contentBytes: Array[Byte]

  /**
    * Returns the raw HTTP request.
    */
  def toRaw: Req

}
