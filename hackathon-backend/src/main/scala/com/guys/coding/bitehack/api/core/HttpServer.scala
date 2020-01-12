package com.guys.coding.bitehack.api.core

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class HttpServer(bindHost: String, bindPort: Int, routes: Route)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext)
    extends StrictLogging {

  def start(): Unit = Http().bindAndHandle(routes, bindHost, bindPort).onComplete {
    case Success(_)  => logger.info(s"HttpServer started at $bindHost:$bindPort")
    case Failure(ex) => logger.error(s"Failed to start HttpServer at $bindHost:$bindPort", ex)
  }

}
