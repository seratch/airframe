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
package wvlet.airframe.http.finagle

import com.twitter.finagle.http.{Request, Response, Status}
import wvlet.airframe.codec.{JSONCodec, MessageCodec}
import wvlet.airframe.http.ResponseHandler
import wvlet.surface.Surface

/**
  * Converting controller results into finagle http responses.
  */
trait FinagleResponseHandler extends ResponseHandler[Request, Response] {

  // Use Map codecs to create natural JSON responses
  private[this] val mapCodecFactory =
    MessageCodec.defautlFactory.withObjectMapCodec

  def toHttpResponse[ResponseValue](request: Request,
                                    responseSurface: Surface,
                                    responseValue: ResponseValue): Response = {
    responseValue match {
      case httpResponse: Response =>
        // Return the response as is
        httpResponse
      case str: String =>
        val r = Response()
        r.contentString = str
        r
      case _ =>
        // Convert the response object into JSON
        val codec: MessageCodec[_] = mapCodecFactory.of(responseSurface)
        val bytes: Array[Byte]     = codec.asInstanceOf[MessageCodec[ResponseValue]].toMsgPack(responseValue)

        // TODO return application/msgpack content type
        val json = JSONCodec.unpackMsgPack(bytes)
        json match {
          case Some(j) =>
            val res = Response(Status.Ok)
            res.setContentTypeJson()
            res.setContentString(json.get)
            res
          case None =>
            Response(Status.InternalServerError)
        }
    }
  }
}
