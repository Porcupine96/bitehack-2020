package com.guys.coding.bitehack.api

import akka.Done
import akka.actor.{ActorPath, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import com.guys.coding.bitehack.api.core.Endpoint
import com.guys.coding.bitehack.chat.ChatActor
import io.codeheroes.herochat.environment.facebook.FacebookSenderId

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
class ConfirmationEndpoint(actorProvider: FacebookSenderId => Props)(implicit system: ActorSystem, ec: ExecutionContext) extends Endpoint {

  import ConfirmationEndpoint._

  override def routing: Route = confirm ~ cancel

  private val confirm = (path("confirm") & post & entity(as[ConfirmIssueCreationRequest])) { request =>
    onSuccess(
      system
        .actorSelection("/user/ChatActorsRepository/2637241379727698/chatActor")
        .resolveOne(5 seconds)
        .flatMap(_.ask(ChatActor.ConfirmCreatingAnIssue(request)))
        .mapTo[Done]
    ) {
      case Done => complete(OK -> "chuj")
    }
  }

  private val cancel = (path("cancel") & post) {
    onSuccess(
      system
        .actorOf(actorProvider(FacebookSenderId("2637241379727698")))
        .ask(ChatActor.CancelCreatingAnIssue)
        .mapTo[Done]
    ) {
      case Done => complete(OK -> "chuj")
    }

  }

}

object ConfirmationEndpoint {

  final case class ConfirmIssueCreationRequest(
      precondition: String,
      expected: String,
      actual: String,
      priority: String,
      errorDate: String,
      partOfApp: String,
      name: String,
      description: String
  )

}
