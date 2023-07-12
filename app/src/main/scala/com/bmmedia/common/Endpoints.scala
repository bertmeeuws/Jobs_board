package com.bmmedia.common

import tyrian.http.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.generic.auto.*
import cats.effect.*
import tyrian.*
import io.circe.Encoder
import com.bmmedia.common.Constants.Endpoints.*

trait Endpoint[M] {
  val location: String
  val method: Method
  val headers: List[Header]
  def onSuccess: Response => M
  def onFailure: HttpError => M

  def call[A: Encoder](payload: A): Cmd[IO, M] = {
    Http.send(
      Request(
        url = s"$root$location",
        method = method,
        headers = headers,
        body = Body.json(payload.asJson.toString()),
        timeout = Request.DefaultTimeOut,
        withCredentials = false
      ),
      Decoder[M](onSuccess, onFailure)
    )
  }
}
