package com.guys.coding.bitehack

import akka.actor.{ActorSystem, Props}
import com.guys.coding.bitehack.Application.Services
import com.typesafe.config.ConfigFactory
import io.codeheroes.herochat.PassivationConfig
import io.codeheroes.herochat.environment.facebook.{FacebookRequest, FacebookResponse, FacebookSenderId}
import io.codeheroes.herochat.infrastructure.inmem.InMemChatRepository

import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App {
  private val config = ConfigFactory.load("default.conf")

  private val services: Services = new Services {
    override def chatActorRepositoryProvider(propsProvider: PassivationConfig => Props)(implicit system: ActorSystem) =
      new InMemChatRepository[FacebookSenderId, FacebookRequest, FacebookResponse](propsProvider, 10 seconds)
  }

  new Application(ConfigValueLoader.provide(config), services).start()
}
