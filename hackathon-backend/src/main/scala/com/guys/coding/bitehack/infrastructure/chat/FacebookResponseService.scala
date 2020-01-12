package com.guys.coding.bitehack.infrastructure.chat

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.stream.QueueOfferResult.{Dropped, Enqueued, QueueClosed}
import akka.stream._
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.guys.coding.bitehack.chat.ResponseService
import com.typesafe.scalalogging.StrictLogging
import io.codeheroes.herochat.environment.facebook.FacebookSenderId
import io.codeheroes.herochat.{ChatResponse, SenderId}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class FacebookResponseService(pageAccessToken: String, attachmentService: FacebookAttachmentService)(
    implicit system: ActorSystem,
    mat: ActorMaterializer,
    ec: ExecutionContext
) extends ResponseService
    with StrictLogging {

  import Converters._

  private implicit val formats: DefaultFormats = DefaultFormats

  private val client = Http()

  private val (queue, stream) =
    Source
      .queue[Message](4096, OverflowStrategy.backpressure)
      .groupBy(Int.MaxValue, _.senderId)
      .map { message =>
        val body = message.response.toBody(attachmentService) ++ Map(
          "recipient" -> Map(
            "id" -> message.senderId.value
          )
        )
        (
          HttpRequest(
            method = HttpMethods.POST,
            uri = s"https://graph.facebook.com/v2.6/me/messages?access_token=$pageAccessToken",
            entity = HttpEntity(
              ContentTypes.`application/json`,
              Serialization.write(body)
            )
          ),
          message.senderId
        )
      }
      .mapAsync(1) {
        case (request, senderId) =>
          try {
            client.singleRequest(request).map(response => Success(ResponseWithSender(senderId, response))).recover {
              case ex: Throwable => Failure(ex)
            }
          } catch {
            case ex: Throwable => Future.failed(ex)
          }
      }
      .mergeSubstreams
      .mapAsync(8) {
        case Success(ResponseWithSender(id, response)) if response.status == BadRequest =>
          parseResponse(response).map { entity =>
            logger.warn(s"Received status ${response.status} with response $entity while sending message to $id")
            Done
          }
        case Success(ResponseWithSender(id, response)) if response.status != OK =>
          parseResponse(response).map { entity =>
            logger.warn(s"Received status ${response.status} with response $entity")
            Done
          }
        case Success(_) => Future.successful(Done)

        case Failure(ex) =>
          logger.error(s"Error while sending request", ex)
          Future.successful(Done)

      }
      .toMat(Sink.ignore)(Keep.both)
      .run()

  stream.onComplete {
    case Success(_)  => logger.info(s"ResponseService finished successfully")
    case Failure(ex) => logger.error(s"ResponseService failed", ex)
  }

  def send(senderId: FacebookSenderId, response: ChatResponse): Future[Done] =
    queue
      .offer(Message(senderId, response))
      .map {
        case Enqueued                        => Done
        case Dropped                         => throw new IllegalStateException(s"Response $response to $senderId dropped by queue")
        case QueueOfferResult.Failure(cause) => throw new IllegalStateException(s"Response $response to $senderId failed", cause)
        case QueueClosed                     => throw new IllegalStateException(s"Response $response to $senderId failed - queue closed")
      }

  private final case class Message(senderId: FacebookSenderId, response: ChatResponse)

  private final case class ResponseWithSender(senderId: SenderId, response: HttpResponse)

  private def parseResponse(response: HttpResponse) = response.entity.toStrict(5 seconds).map(_.data.utf8String)

}
