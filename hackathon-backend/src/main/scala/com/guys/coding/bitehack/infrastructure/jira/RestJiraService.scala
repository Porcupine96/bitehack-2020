package com.guys.coding.bitehack.infrastructure.jira

import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, MediaTypes, StatusCodes}
import akka.http.scaladsl.model
import akka.stream.ActorMaterializer
import com.guys.coding.bitehack.domain.jira.{IssueType, Priority, Task, TaskData}
import com.typesafe.scalalogging.StrictLogging
import org.json4s.native.{JsonMethods, JsonParser, Serialization}
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import org.json4s.JsonAST.JValue
import com.bitehack.ner.proto.api.IssueEntry

class RestJiraService(implicit system: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer) extends StrictLogging {

  private val projectKey = "BH"
  private val authHeader = "Basic ZGF3aWQuZ2RrYmF0dGxlbmV0QGdtYWlsLmNvbTpWTzVVdzhFOVlONTBQQldmNzFLVUQwMjI="

  private val client                    = Http()
  private implicit val formats: Formats = DefaultFormats

  def createTask(taskData: TaskData): Future[Option[Task]] = {
    val body = Map(
      "fields" -> Map(
        "project" ->
          Map(
            "key" -> projectKey
          ),
        "summary"     -> taskData.summary,
        "description" -> taskData.description,
        "issuetype" -> Map(
          "name" -> (taskData.issueType match {
            case IssueType.Bug => "Bug"
          })
        ),
        "priority" -> Map(
          "id" -> (taskData.priority match {
            case Priority.Low    => "4"
            case Priority.Medium => "3"
            case Priority.High   => "2"
          })
        ),
        "customfield_10039" -> taskData.precondition,
        "customfield_10041" -> taskData.expectedBehavior,
        "customfield_10040" -> taskData.actualBehavior,
        "customfield_10042" -> taskData.partOfSystem,
        "customfield_10037" -> taskData.errorDate.format(DateTimeFormatter.ISO_DATE_TIME)
      )
    )
    println(taskData.errorDate.format(DateTimeFormatter.ISO_DATE_TIME))
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = s"https://codevillains.atlassian.net/rest/api/2/issue",
      headers = RawHeader("Authorization", authHeader) :: RawHeader("Content-Type", "application/json") :: Nil,
      entity = HttpEntity.apply(ContentTypes.`application/json`, Serialization.write(body).getBytes)
    )

    client.singleRequest(request).flatMap {
      case response if response.status == StatusCodes.Created =>
        response.entity
          .toStrict(5 seconds)
          .map(_.data.utf8String)
          .map(JsonParser.parse)
          .map(r =>
            Some(
              Task(
                (r \ "id").extract[String],
                (r \ "key").extract[String],
                s"https://codevillains.atlassian.net/browse/${(r \ "key").extract[String]}",
                taskData
              )
            )
          )
      case response =>
        logger.error(
          s"Illegal http response when creating task [${response.status}] [${Await.result(response.entity.toStrict(5 seconds), 5 seconds).data.utf8String}]"
        )
        Future.successful(None)
    }
  }

  def getNotDoneIssues(): Future[Option[List[IssueEntry]]] = {

    val body = Map(
      "fields" -> List(
        "creator",
        "summary",
        "description",
        "status"
      )
    )

    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = s"https://codevillains.atlassian.net/rest/api/3/search",
      headers = RawHeader("Authorization", authHeader) :: RawHeader("Content-Type", "application/json") :: Nil,
      entity = HttpEntity.apply(ContentTypes.`application/json`, Serialization.write(body).getBytes)
    )

    client.singleRequest(request).flatMap {
      case response if response.status == StatusCodes.OK =>
        response.entity
          .toStrict(5 seconds)
          .map(_.data.utf8String)
          .map(JsonParser.parse)
          .map { r =>
            val issues = (r \ "issues").extract[List[JValue]]

            Some(issues.flatMap { issue =>
              val title  = (issue \ "fields" \ "summary").extract[String]
              val id     = (issue \ "key").extract[String]
              val status = (issue \ "fields" \ "status" \ "name").extract[String]

              if (status != "Done") {
                Some(
                  IssueEntry(title = title, id = id)
                )
              } else {
                None
              }

            })

          }
      case response =>
        logger.error(
          s"Illegal http response when creating task [${response.status}] [${Await.result(response.entity.toStrict(5 seconds), 5 seconds).data.utf8String}]"
        )
        Future.successful(None)
    }
  }

}
