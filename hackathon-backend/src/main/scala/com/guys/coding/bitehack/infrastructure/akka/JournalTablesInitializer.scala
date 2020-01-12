package com.guys.coding.bitehack.infrastructure.akka

import slick.jdbc.PostgresProfile.api._
import slick.jdbc.PostgresProfile.backend.DatabaseDef

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object JournalTablesInitializer {

  def createJournalSchema(db: DatabaseDef) = {
    val create = db.run(sqlu"""
      CREATE TABLE IF NOT EXISTS public.journal (
        ordering BIGSERIAL,
        persistence_id VARCHAR(255) NOT NULL,
        sequence_number BIGINT NOT NULL,
        deleted BOOLEAN DEFAULT FALSE,
        tags VARCHAR(255) DEFAULT NULL,
        message BYTEA NOT NULL,
        PRIMARY KEY(persistence_id, sequence_number)
      );

      CREATE UNIQUE INDEX IF NOT EXISTS journal_ordering_idx ON public.journal(ordering);

      CREATE TABLE IF NOT EXISTS public.snapshot (
        persistence_id VARCHAR(255) NOT NULL,
        sequence_number BIGINT NOT NULL,
        created BIGINT NOT NULL,
        snapshot BYTEA NOT NULL,
        PRIMARY KEY(persistence_id, sequence_number)
      );""")

    Await.result(create, 10 seconds)
  }

}
