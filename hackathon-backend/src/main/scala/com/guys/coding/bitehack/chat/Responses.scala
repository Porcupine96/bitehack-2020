package com.guys.coding.bitehack.chat

import com.guys.coding.bitehack.domain.jira.Task
import com.bitehack.ner.proto.api.{ExtractionType, IssueEntry}
import io.codeheroes.herochat.environment.facebook.Buttons.{MessengerExtension, UrlButton}
import io.codeheroes.herochat.environment.facebook.FacebookResponse.{Buttons, QuickReplies, Simple}
import io.codeheroes.herochat.environment.facebook.{FacebookResponse, MessengerExtensionHeights, QuickReplies => QR}

import scala.util.Random

//noinspection SpellCheckingInspection
object Responses {

  val helloMessage = QuickReplies(
    "Would you like to reaport an issue? 🤔",
    QR.Text("Yes ✔️", "FOUND_NEW_BUG"),
    QR.Text("Nope ❌", "NOTHING_NEW")
  )

  def giveMeMoreInfo(about: ExtractionType): Simple =
    Simple(
      Random
        .shuffle(
          List(
            s"Interesting... 🤔 However I still miss any info about ${extractionTypeToString(about)}. Can you tell me more about it? ",
            s"Thank you 😄 Now I need just a few more things. Please tell me about ${extractionTypeToString(about)}.",
            s"That makes it clear❗ Now, please tell me something about ${extractionTypeToString(about)} 😉",
            s"Noted. And what can you tell me about ${extractionTypeToString(about)}?",
            s"Thanks 💕. And what about ${extractionTypeToString(about)}?"
          )
        )
        .head
    )

  val giveMeIssueName   = Simple("What should be the name of the issue? 📝")
  val giveMeDescription = Simple("Add more description if you want 📝")
  val completedAllData  = Simple("Nice, you did it 🙃 We've gathered all data I needed to know. Do you want to create a JIRA issue?")

  def issuePreview(uri: String) = Buttons("Check it out", MessengerExtension("Click here", uri, MessengerExtensionHeights.Full))

  def extractionTypeToString(extractionType: ExtractionType): String = extractionType match {
    case ExtractionType.PRECONDITION    => "what happened before the issue"
    case ExtractionType.EXPECTED        => "expected behavior"
    case ExtractionType.ATUAL           => "erroneous behavior"
    case ExtractionType.PRIORITY        => "priority of an issue"
    case ExtractionType.ERROR_DATE      => "date of issue occurance"
    case ExtractionType.PART_OF_APP     => "the part of the application where issue has happened"
    case ExtractionType.Unrecognized(_) => throw new IllegalStateException()
  }

  def showPossibleDuplicates(issueEntries: List[IssueEntry]) =
    Buttons(
      "Possible duplicates",
      UrlButton(issueEntries.head.title, s"https://codevillains.atlassian.net/browse/${issueEntries.head.id}"),
      issueEntries.tail.map(entry => UrlButton(entry.title, s"https://codevillains.atlassian.net/browse/${entry.id}")): _*
    )

  val askIsaDuplicateMessage = QuickReplies(
    "Is it a dupliacte issue?",
    QR.Text("Yes", "DUPLICATE"),
    QR.Text("No", "NO_DUPLICATE")
  )

  val sorryItsDuplicateMessage = Simple(
    "Sorry to hear that :'("
  )

  def issueCreated(task: Task): Buttons = Buttons("Issue created! Click link below to see it in JIRA", UrlButton("Jira", task.url))

  val couldntCreateIssue = Simple(s"Couldn't create issue :(")

  def imListeningMessage =
    Simple(
      Random
        .shuffle(
          List(
            "Fine, I'm listening...",
            "Okay, what do you have ? 🤔",
            "Nice. Show me what you've found 😉"
          )
        )
        .head
    )

  val iDoNotUnderstand = Simple("Sorry, I don't understand that 😰")

}
