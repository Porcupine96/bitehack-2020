package com.guys.coding.bitehack.infrastructure.akka.persistence

import akka.persistence.journal.{EventAdapter, EventSeq, Tagged}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.immutable.Set

class JavaTaggingEventAdapter extends EventAdapter with StrictLogging {

  override def manifest(event: Any): String = ""

  override def fromJournal(event: Any, manifest: String): EventSeq =
    EventSeq.single {
      event
    }

  override def toJournal(event: Any): Any = event match {
    case other => Tagged(other, Set("all"))
  }

}
