package com.guys.coding.bitehack.api.core

import java.time.LocalDate

import enumeratum.Json4s
import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JInt, JLong, JString}

object DomainFormatters {

  // import GouTimeUtils._

  val all: List[CustomSerializer[_]] = Nil
  // val all = List(
  //   UserIdFormatter,
  //   GameIdFormatter,
  //   ReservationIdFormatter,
  //   GameReservationIdFormatter,
  //   Json4s.serializer(ReservationType),
  //   Json4s.serializer(ReservationStatus),
  //   Json4s.serializer(CreateReservationError),
  //   Json4s.serializer(CancelReservationError),
  //   Json4s.serializer(DeclineReservationError),
  //   LocalDateFormatter
  // )

  // case object LocalDateFormatter
  //     extends CustomSerializer[LocalDate](_ =>
  //       ({
  //         case JLong(value) => localDateFromMillis(value.longValue())
  //       }, {
  //         case date: LocalDate => JLong(localDateToMillis(date))
  //       }))

  // case object UserIdFormatter
  //     extends CustomSerializer[UserId](_ =>
  //       ({
  //         case JInt(value) => UserId(value.intValue())
  //       }, {
  //         case UserId(value) => JInt(value)
  //       }))

  // case object GameIdFormatter
  //     extends CustomSerializer[GameId](_ =>
  //       ({
  //         case JString(value) => GameId(value)
  //       }, {
  //         case GameId(value) => JString(value)
  //       }))

  // case object ReservationIdFormatter
  //     extends CustomSerializer[ReservationId](_ =>
  //       ({
  //         case JString(value) => ReservationId(value)
  //       }, {
  //         case ReservationId(value) => JString(value)
  //       }))

  // case object GameReservationIdFormatter
  //     extends CustomSerializer[GameReservationId](_ =>
  //       ({
  //         case JString(value) => GameReservationId(value)
  //       }, {
  //         case GameReservationId(value) => JString(value)
  //       }))

}
