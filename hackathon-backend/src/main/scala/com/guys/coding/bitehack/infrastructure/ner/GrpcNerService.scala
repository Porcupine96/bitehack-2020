package com.guys.coding.bitehack.infrastructure.ner

import cats.data.{NonEmptyList => NEL}
import io.grpc.ManagedChannelBuilder

import scala.concurrent.{ExecutionContext, Future}
import com.bitehack.ner.proto.api.NerServiceGrpc
import com.bitehack.ner.proto.api.{Extraction, ExtractionType, GetExtractionsRequest}
import com.typesafe.scalalogging.StrictLogging
import com.bitehack.ner.proto.api.IssueEntry
import com.bitehack.ner.proto.api.GetOddlySimilarRequest

class GrpcNerService(host: String, port: Int)(implicit ec: ExecutionContext) extends StrictLogging {

  private val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
  private val stub    = NerServiceGrpc.stub(channel)

  def getExtractions(text: String): Future[Map[ExtractionType, NEL[String]]] =
    stub
      .getExtractions(GetExtractionsRequest(text, computeUrgency = Some(true)))
      .map(
        _.extractions
          .flatMap(Extraction.unapply)
          .flatMap {
            case (exts, t) =>
              logger.info(s"For type [$t] got extractions [$exts]")
              NEL.fromList(exts.toList).map(e => (t, e))
          }
          .toMap
      )

  def getOddlySimilar(title: String, others: List[IssueEntry]): Future[List[IssueEntry]] =
    stub
      .getOddlySimilar(GetOddlySimilarRequest(inQuestion = IssueEntry(title = title, id = ""), entries = others))
      .map(_.similar.toList)
}
