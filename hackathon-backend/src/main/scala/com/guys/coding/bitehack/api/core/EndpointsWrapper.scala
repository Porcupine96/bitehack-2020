package com.guys.coding.bitehack.api.core

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import akka.http.scaladsl.settings.RoutingSettings
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.collection.immutable.Seq
import scala.util.control.NonFatal

class EndpointsWrapper(endpoints: server.Route*)(implicit system: ActorSystem) extends Json4sSupport with StrictLogging {

//  private implicit val formats: DefaultFormats      = DefaultFormats
//  private implicit val serialization: Serialization = Serialization

  private val allowedCorsMethods = Seq(GET, POST, PUT, DELETE, OPTIONS)
  private val routingSettings    = RoutingSettings(system.settings.config)
  private val rejectionHandler   = RejectionHandler.default
  private val corsSettings       = CorsSettings.defaultSettings.copy(allowedMethods = allowedCorsMethods)

  private val exceptionHandler = ExceptionHandler {
    case NonFatal(e) ⇒
      ctx ⇒ {
        ctx.log.error(e, "Error during processing of request: '{}'. Completing with {} response.", ctx.request, InternalServerError)
        ctx.complete(InternalServerError)
      }
  }

  def routing: server.Route =
    (cors(corsSettings) & handleRejections(rejectionHandler) & handleExceptions(exceptionHandler.seal(routingSettings))) {
      endpoints.reduce(_ ~ _) ~ (path("status") & get)(complete(OK))
    }

}
