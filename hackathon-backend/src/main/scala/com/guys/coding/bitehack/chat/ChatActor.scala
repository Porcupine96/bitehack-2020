package com.guys.coding.bitehack.chat

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, ZoneId}

import akka.actor.Status
import akka.persistence.{PersistentActor, RecoveryCompleted}
import cats.data.NonEmptyList
import com.guys.coding.bitehack.chat.ChatActor.{CancelCreatingAnIssue, ConfirmCreatingAnIssue}
import com.guys.coding.bitehack.infrastructure.ner.GrpcNerService
import cats.instances.future._
import com.bitehack.ner.proto.api.ExtractionType
import com.typesafe.scalalogging.StrictLogging
import io.codeheroes.herochat.environment.facebook.service.FacebookService
import io.codeheroes.herochat.environment.facebook.{FacebookRequest, FacebookResponse, FacebookSenderId}
import java.util.Base64

import akka.Done
import com.guys.coding.bitehack.api.ConfirmationEndpoint.ConfirmIssueCreationRequest
import com.guys.coding.bitehack.domain.jira.{Priority, TaskData}
import com.guys.coding.bitehack.infrastructure.jira.RestJiraService

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import cats.data.OptionT
import scala.concurrent.Future
import com.bitehack.ner.proto.api.IssueEntry

class ChatActor(
    id: FacebookSenderId,
    facebookService: FacebookService,
    responseService: ResponseService,
    nerService: GrpcNerService,
    jiraService: RestJiraService
) extends PersistentActor
    with StrictLogging {

  implicit val executionContext: ExecutionContext = context.system.dispatcher

  override def persistenceId: String = s"chat-${id.value}"

  override def receiveRecover: Receive = {
    case e: ChatEvent      => handleEvent(e)
    case RecoveryCompleted => // ignore
  }

  def handleEvent(e: ChatEvent): Unit = ()

  override def receiveCommand: Receive = initial

  def initial: Receive = {
    case FacebookRequest.Message(_) =>
      logger.info(s"I AM [$id]")
      logger.info(s"I AM [$self]")
      sender ! Responses.helloMessage

    case FacebookRequest.QuickReply("FOUND_NEW_BUG") =>
      sender ! Responses.imListeningMessage
      context.become(completingContent(extractions = Map.empty, askingAbout = None))

    case FacebookRequest.QuickReply("NOTHING_NEW") =>
      val uri = webviewUri(
        "Example name",
        "Lorem ipsum dolom sores",
        ExtractionType.values.map {
          case ext @ ExtractionType.PRECONDITION => ext -> NonEmptyList.of("I was scrolling users list")
          case ext @ ExtractionType.EXPECTED     => ext -> NonEmptyList.of("I expected to see users which are archived")
          case ext @ ExtractionType.ATUAL        => ext -> NonEmptyList.of("I didn't see archived users")
          case ext @ ExtractionType.PRIORITY     => ext -> NonEmptyList.of("HIGH")
          case ext @ ExtractionType.ERROR_DATE   => ext -> NonEmptyList.of("2020-01-08T23:41:18.701808")
          case ext @ ExtractionType.PART_OF_APP  => ext -> NonEmptyList.of("User page")
          case ExtractionType.Unrecognized(_)    => throw new IllegalStateException()
        }.toMap
      )

      println(s"URI ${uri}")
      sender ! Responses.issuePreview(
        uri
      )

//    case SendChatMessage(title, message) =>
//      responseService
//        .send(id, Simple(message))
//        .onComplete {
//          case Success(_)  => logger.info("Successs!!!!!!!!!!!!!!!!!")
//          case Failure(ex) => logger.error("Errror", ex)
//        }

    case other => handleUnknown(other, "initial")
  }

  def completingContent(extractions: Map[ExtractionType, NonEmptyList[String]], askingAbout: Option[ExtractionType]): Receive = {
    case FacebookRequest.Message(text) if text.contains("dupa") || text.contains("ass") => ()

    case FacebookRequest.Message(text) if askingAbout.isEmpty =>
      val nerExtractions     = Await.result(nerService.getExtractions(text), 10 seconds)
      val missingExtractions = ExtractionType.values.filterNot(nerExtractions.contains)
      if (missingExtractions.isEmpty) {
        sender ! Responses.giveMeIssueName
        context.become(completingIssueName(nerExtractions))
      } else {
        val willBeAskingAbout = missingExtractions.head
        sender ! Responses.giveMeMoreInfo(willBeAskingAbout)
        context.become(completingContent(nerExtractions, askingAbout = Some(willBeAskingAbout)))
      }

    case FacebookRequest.Message(text) if askingAbout.isDefined =>
      val askingAboutExtraction   = askingAbout.get
      val alreadyFoundExtractions = extractions.keySet
      val nerExtractions =
        Await
          .result(nerService.getExtractions(text), 10 seconds)
          .filterNot(extraction => alreadyFoundExtractions.contains(extraction._1))

      val askedExtractionWithFallback = nerExtractions.getOrElse(askingAboutExtraction, NonEmptyList.of(text))
      val newFoundExtractions         = nerExtractions + (askingAboutExtraction -> askedExtractionWithFallback)

      val allExtractionsFoundEver = extractions ++ newFoundExtractions

      val missingExtractions = ExtractionType.values.filterNot(allExtractionsFoundEver.contains)
      if (missingExtractions.isEmpty) {
        sender ! Responses.giveMeIssueName
        context.become(completingIssueName(allExtractionsFoundEver))
      } else {
        val willBeAskingAbout = missingExtractions.head
        sender ! Responses.giveMeMoreInfo(willBeAskingAbout)
        context.become(completingContent(allExtractionsFoundEver, askingAbout = Some(willBeAskingAbout)))
      }

    case unknown => handleUnknown(unknown, "completingContent")
  }

  def completingIssueName(extractions: Map[ExtractionType, NonEmptyList[String]]): Receive = {
    case FacebookRequest.Message(text) =>
      sender ! Responses.giveMeDescription
      context.become(completingDescription(text, extractions))

    case unknown => handleUnknown(unknown, "completingIssueName")
  }

  def completingDescription(name: String, extractions: Map[ExtractionType, NonEmptyList[String]]): Receive = {
    case FacebookRequest.Message(text) =>
      val similarIssues = Await.result(getOddlySimilar(name).value, 10 seconds).getOrElse(List.empty)
      if (similarIssues.isEmpty) {
        sender ! Responses.issuePreview(webviewUri(name, text, extractions))
        context.become(waitingForConfirmation)
      } else {
        sender ! Responses.showPossibleDuplicates(similarIssues)
        sender ! Responses.askIsaDuplicateMessage
        context.become(waitingForRemovingDuplicate(name, text, extractions))
      }

    case unknown => handleUnknown(unknown, "completingDescription")
  }

  def waitingForRemovingDuplicate(
      name: String,
      desc: String,
      extractions: Map[ExtractionType, NonEmptyList[String]]
  ): Receive = {
    case FacebookRequest.QuickReply("DUPLICATE") =>
      sender ! Responses.couldntCreateIssue
      context.become(initial)
//      val map = extractions.mapValues(_.toList.mkString("\n"))
//      val taskData = TaskData(
//        summary = name,
//        priority = (map(ExtractionType.PRIORITY) match {
//          case "HIGH" => Priority.High
//          case "LOW"  => Priority.Low
//          case _      => Priority.Low
//        }),
//        precondition = map(ExtractionType.PRECONDITION),
//        expectedBehavior = map(ExtractionType.EXPECTED),
//        actualBehavior = map(ExtractionType.ATUAL),
//        partOfSystem = map(ExtractionType.PART_OF_APP),
//        errorDate = (Try(LocalDateTime.parse(map(ExtractionType.ERROR_DATE))) match {
//          case Failure(exception) =>
//            logger.error(s"Error parsing errorDate [${map(ExtractionType.ERROR_DATE)}]", exception)
//            Try(LocalDate.parse(map(ExtractionType.ERROR_DATE)).atStartOfDay()) match {
//              case Failure(exception) =>
//                logger.error(s"Error parsing errorDate [${map(ExtractionType.ERROR_DATE)}]", exception)
//                LocalDate.now().atStartOfDay()
//              case Success(value) =>
//                value
//            }
//          case Success(value) =>
//            value
//        }).atZone(ZoneId.systemDefault()),
//        description = desc
//      )
//      val result = Await.result(jiraService.createTask(taskData), 10 seconds)
//      result match {
//        case Some(task) =>
//          sender ! Responses.issueCreated(task)
//        case None =>
//          sender ! Responses.couldntCreateIssue
//      }
//      context.become(initial)

    case FacebookRequest.QuickReply("NO_DUPLICATE") =>
      sender ! Responses.issuePreview(webviewUri(name, desc, extractions))
      context.become(waitingForConfirmation)
  }

  def waitingForConfirmation: Receive = {
    case ConfirmCreatingAnIssue(payload) =>
      val taskData = TaskData(
        summary = payload.name,
        priority = (payload.priority match {
          case "HIGH"   => Priority.High
          case "MEDIUM" => Priority.Medium
          case "LOW"    => Priority.Low
          case _        => Priority.Medium
        }),
        precondition = payload.precondition,
        expectedBehavior = payload.expected,
        actualBehavior = payload.actual,
        partOfSystem = payload.partOfApp,
        errorDate = (Try(LocalDateTime.parse(payload.errorDate)) match {
          case Failure(exception) =>
            logger.error(s"Error parsing errorDate [${payload.errorDate}]", exception)
            Try(LocalDate.parse(payload.errorDate).atStartOfDay()) match {
              case Failure(exception) =>
                logger.error(s"Error parsing errorDate [${payload.errorDate}]", exception)
                LocalDate.now().atStartOfDay()
              case Success(value) =>
                value
            }
          case Success(value) =>
            value
        }).atZone(ZoneId.systemDefault()),
        description = payload.description
      )
      val result = Await.result(jiraService.createTask(taskData), 10 seconds)
      sender ! Done
      result match {
        case Some(task) =>
          responseService
            .send(id, Responses.issueCreated(task))
            .onComplete {
              case Success(_)  => logger.info("Successs!!!!!!!!!!!!!!!!!")
              case Failure(ex) => logger.error("Errror", ex)
            }

        case None =>
          responseService
            .send(id, Responses.couldntCreateIssue)
            .onComplete {
              case Success(_)  => logger.info("Successs!!!!!!!!!!!!!!!!!")
              case Failure(ex) => logger.error("Errror", ex)
            }
      }

    case CancelCreatingAnIssue =>
  }

  def handleUnknown(message: Any, stateName: String): Unit = message match {
    case _: FacebookResponse =>
      logger.warn(s"Unexpected message in state $stateName - $message")
      sender ! Responses.iDoNotUnderstand
      context.become(initial)

    case _ =>
      logger.warn(s"Unexpected internal message in state $stateName - $message")
      sender ! Status.Failure(new IllegalStateException(s"Unexpected message $message"))
      context.become(initial)
  }

  def webviewUri(name: String, description: String, extractionTypes: Map[ExtractionType, NonEmptyList[String]]): String = {
    val extractionTypesQueryParams = extractionTypes
      .mapValues(_.toList.mkString("\n"))
      .map {
        case (t @ ExtractionType.ERROR_DATE, date) =>
          t -> (Try(LocalDateTime.parse(date)) match {
            case Failure(exception) =>
              logger.error(s"Error parsing errorDate [$date]", exception)
              Try(LocalDate.parse(date).atStartOfDay()) match {
                case Failure(exception) =>
                  logger.error(s"Error parsing errorDate [$date]", exception)
                  LocalDate.now().atStartOfDay()
                case Success(value) =>
                  value
              }
            case Success(dateTime) => dateTime
          }).format(DateTimeFormatter.ISO_DATE_TIME)
        case e => e
      }
      .map {
        case (extractionType, content) =>
          val queryParamName = extractionType match {
            case ExtractionType.PRECONDITION    => "precondition"
            case ExtractionType.EXPECTED        => "expected"
            case ExtractionType.ATUAL           => "actual"
            case ExtractionType.PRIORITY        => "priority"
            case ExtractionType.ERROR_DATE      => "errorDate"
            case ExtractionType.PART_OF_APP     => "partOfApp"
            case ExtractionType.Unrecognized(_) => throw new IllegalStateException()
          }
          val basedContent = Base64.getEncoder.encodeToString(content.getBytes).replace('=', '_')
          queryParamName -> basedContent
      }

    val nameAndDescriptionQueryParam = Map(
      "name"        -> Base64.getEncoder.encodeToString(name.getBytes).replace('=', '_'),
      "description" -> Base64.getEncoder.encodeToString(description.getBytes).replace('=', '_')
    )

    val allQueryParams = (extractionTypesQueryParams ++ nameAndDescriptionQueryParam).map {
      case (n, v) => s"$n=$v"
    }

    s"https://bitehack-webview.codeheroes.tech/issue?${allQueryParams.mkString("&")}"
  }

  def getOddlySimilar(title: String): OptionT[Future, List[IssueEntry]] =
    for {
      notDone <- OptionT(jiraService.getNotDoneIssues())
      similar <- OptionT.liftF(nerService.getOddlySimilar(title, notDone))
    } yield similar

}

object ChatActor {

  final case class ConfirmCreatingAnIssue(issue: ConfirmIssueCreationRequest)

  final object CancelCreatingAnIssue

}
