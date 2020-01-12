package com.guys.coding.bitehack.domain.jira

import java.time.ZonedDateTime

case class Task(
    id: String,
    key: String,
    url: String,
    taskData: TaskData
)

case class TaskData(
    summary: String,
    issueType: IssueType = IssueType.Bug,
    priority: Priority,
    precondition: String,
    expectedBehavior: String,
    actualBehavior: String,
    partOfSystem: String,
    errorDate: ZonedDateTime,
    description: String
)

sealed trait IssueType

object IssueType {
  case object Bug extends IssueType
}

sealed trait Priority

object Priority {
  case object Low    extends Priority
  case object Medium extends Priority
  case object High   extends Priority
}
