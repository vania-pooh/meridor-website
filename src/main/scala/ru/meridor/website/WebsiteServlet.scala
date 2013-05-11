package ru.meridor.website

import org.scalatra._
import scalate.ScalateSupport

class WebsiteServlet extends WebsiteStack {

  get("/"){
    contentType = "text/html"
    jade("/index")
  }

}
