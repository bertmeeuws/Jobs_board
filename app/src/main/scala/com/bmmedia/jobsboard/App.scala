package com.bmmedia.jobsboard

import scala.scalajs.js.annotation.*
import org.scalajs.dom.document

@JSExportTopLevel("BMMediaApp")
class App {
  @JSExport
  def doSomething(containerId: String): Unit = {
    document.getElementById(containerId).innerHTML = "Hello World!"
  }
}
