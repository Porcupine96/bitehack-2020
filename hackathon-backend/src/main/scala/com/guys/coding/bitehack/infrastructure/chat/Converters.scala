package com.guys.coding.bitehack.infrastructure.chat

import io.codeheroes.herochat.ChatResponse
import io.codeheroes.herochat.environment.facebook.Buttons.{MessengerExtension, UrlButton}
import io.codeheroes.herochat.environment.facebook.FacebookResponse.{Buttons, ImageAttachment, Simple, VideoAttachment}

object Converters {

  implicit class ChatResponseConverter(value: ChatResponse) {
    def toBody(attachmentService: FacebookAttachmentService): Map[Any, Any] = value match {
      case Simple(text) => Map("message" -> Map("text" -> text))

      case Buttons(buttonName, button: UrlButton) =>
        Map(
          "message" -> Map(
            "attachment" -> Map(
              "type" -> "template",
              "payload" -> Map(
                "template_type" -> "button",
                "text"          -> buttonName,
                "buttons" -> List(
                  Map(
                    "type"  -> "web_url",
                    "url"   -> button.url,
                    "title" -> button.title
                  )
                )
              )
            )
          )
        )

      case ImageAttachment(attachmentId) =>
        Map(
          "message" -> Map(
            "attachment" -> Map(
              "type" -> "image",
              "payload" -> Map(
                "attachment_id" -> attachmentService.get(attachmentId).getOrElse("")
              )
            )
          )
        )

      case VideoAttachment(url) =>
        Map(
          "message" -> Map(
            "attachment" -> Map(
              "type" -> "video",
              "payload" -> Map(
                "url" -> url
              )
            )
          )
        )
    }
  }

}
