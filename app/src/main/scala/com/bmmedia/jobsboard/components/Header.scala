package com.bmmedia.jobsboard.components

import tyrian.Html.*
import tyrian.*

import com.bmmedia.jobsboard.core.*
import scala.scalajs.js.annotation.*
import scala.scalajs.js

object Header {

  def view() = div(
    ul(`class` := "flex items-center")(
      viewLogo(),
      createNav("jobs", "/jobs"),
      createNav("home", "/home"),
      createNav("google", "https://google.com")
    )
  )

  @js.native
  @JSImport("/static/images/logo.png", JSImport.Default, "image")
  private val image: String = js.native

  def viewLogo() = li(
    a(
      href    := "/",
      `class` := "p-3 text-red-500 hover:text-red-800 mx-6",
      onEvent(
        "click",
        e => {
          e.preventDefault()
          Router.ChangeLocation("/")
        }
      )
    )(
      img(
        src    := image,
        width  := "64",
        height := "64",
        alt    := "logo"
      )
    )
  )

  private def createNav(name: String, location: String) = {
    li(
      a(
        href    := location,
        `class` := "p-3 text-red-500 hover:text-red-800 mx-6",
        onEvent(
          "click",
          e => {
            e.preventDefault()
            if name == "google" then Router.ExternalRedirect(location)
            else Router.ChangeLocation(location)
          }
        )
      )(name)
    )
  }
}
