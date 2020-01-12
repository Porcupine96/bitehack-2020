package com.guys.coding.bitehack

import com.guys.coding.bitehack.chat.ChatActor
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import ch.qos.logback.classic.{Level, Logger}
import com.guys.coding.bitehack.Application.Services
import com.guys.coding.bitehack.ConfigValueLoader.ConfigValues
import com.guys.coding.bitehack.api.ConfirmationEndpoint
import com.guys.coding.bitehack.api.core.{EndpointsWrapper, HttpServer}
import com.guys.coding.bitehack.infrastructure.chat.{FacebookAttachmentService, FacebookResponseService}
import com.guys.coding.bitehack.infrastructure.jira.RestJiraService
import com.guys.coding.bitehack.infrastructure.ner.GrpcNerService
import io.codeheroes.herochat.core.ChatActorRepository
import io.codeheroes.herochat.environment.facebook.service.FacebookService
import io.codeheroes.herochat.environment.facebook.{FacebookRequest, FacebookResponse, FacebookSenderId}
import io.codeheroes.herochat.{Chat, PassivationConfig}
import org.slf4j.LoggerFactory
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import com.guys.coding.bitehack.infrastructure.akka.JournalTablesInitializer

import scala.concurrent.ExecutionContext

class Application(config: ConfigValues, services: Services) {

  private implicit val system: ActorSystem                = ActorSystem("ActorSystem", config.config)
  private implicit val executionContext: ExecutionContext = system.dispatcher
  private implicit val materializer: ActorMaterializer    = ActorMaterializer()

  private implicit val db: PostgresProfile.backend.Database = Database.forConfig("slick.db", config.config)

  LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger].setLevel(Level.valueOf(config.applicationConfig.logLevel))

  JournalTablesInitializer.createJournalSchema(db)

  private val facebookService                         = new FacebookService()
  private val token                                   = config.config.getString("herochat.facebook.pageAccessToken")
  private val attachementService                      = new FacebookAttachmentService(token)
  private val responseService                         = new FacebookResponseService(token, attachementService)
  private val nerService                              = new GrpcNerService("nero", 8080)
  private val jiraService                             = new RestJiraService()
  private def chatActorProvider(id: FacebookSenderId) = Props(new ChatActor(id, facebookService, responseService, nerService, jiraService))

  private val chat = new Chat[FacebookSenderId, FacebookRequest, FacebookResponse](
    services.chatActorRepositoryProvider,
    chatActorProvider
  )

  private val endpoints = List(
    new ConfirmationEndpoint(chatActorProvider)
  )

  private val routes = new EndpointsWrapper(endpoints.map(_.routing).reduce(_ ~ _) ~ pathPrefix("chat")(chat.routes)).routing

  private val server = new HttpServer("0.0.0.0", 8080, routes)

  def start(): Unit = {
    Thread.sleep(3000)
    server.start()
  }

}

object Application {

  trait Services {
    def chatActorRepositoryProvider(propsProvider: PassivationConfig => Props)(
        implicit system: ActorSystem
    ): ChatActorRepository[FacebookSenderId, FacebookRequest, FacebookResponse]
  }
}
