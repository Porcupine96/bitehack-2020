package com.guys.coding.bitehack.infrastructure.chat

import java.util.concurrent.ConcurrentHashMap

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class FacebookAttachmentService(pageAccessToken: String)(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer) {
  private val client           = Http()
  private implicit val formats = DefaultFormats

  private val attachments = new ConcurrentHashMap[String, String]().asScala

  private def prepareBody(attachmentId: String, attachmentUrl: String) =
    Map(
      "message" -> Map(
        "attachment" -> Map(
          "type" -> "image",
          "payload" -> Map(
            "url"         -> attachmentUrl,
            "is_reusable" -> true
          )
        )
      )
    )

  private def prepareRequest(body: String) =
    HttpRequest(
      method = HttpMethods.POST,
      uri = s"https://graph.facebook.com/v2.6/me/message_attachments?access_token=$pageAccessToken",
      entity = HttpEntity(ContentTypes.`application/json`, body)
    )

  def register(attachmentId: String, attachmentUrl: String): Future[Done] = {
    val body    = prepareBody(attachmentId, attachmentUrl)
    val request = prepareRequest(Serialization.write(body))

    client
      .singleRequest(request)
      .flatMap {
        case response if response.status == OK =>
          response.entity
            .toStrict(5 seconds)
            .map(_.data.utf8String)
            .map(parse(_))
            .map(r => (r \ "attachment_id").extract[String])

        case response => throw new IllegalStateException(s"Failed to upload attachment for request $request with response ${response.status}")
      }
      .map { id =>
        attachments += attachmentId -> id
        Done
      }
  }

  def get(id: String): Option[String] = attachments.get(id)

}
