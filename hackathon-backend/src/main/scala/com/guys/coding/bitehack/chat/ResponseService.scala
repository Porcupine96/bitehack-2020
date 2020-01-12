package com.guys.coding.bitehack.chat

import akka.Done
import io.codeheroes.herochat.ChatResponse
import io.codeheroes.herochat.environment.facebook.FacebookSenderId

import scala.concurrent.Future

trait ResponseService {
  def send(senderId: FacebookSenderId, response: ChatResponse): Future[Done]
}
