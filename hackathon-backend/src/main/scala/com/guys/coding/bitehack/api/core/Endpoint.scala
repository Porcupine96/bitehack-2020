package com.guys.coding.bitehack.api.core

import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, Serialization}

import scala.concurrent.duration._
import scala.language.postfixOps

trait Endpoint extends Json4sSupport with Directives with StrictLogging {

  protected implicit val formats: Formats             = DefaultFormats ++ DomainFormatters.all
  protected implicit val serialization: Serialization = Serialization
  protected implicit val timeout: Timeout             = Timeout(60 seconds)

  def routing: Route

}
