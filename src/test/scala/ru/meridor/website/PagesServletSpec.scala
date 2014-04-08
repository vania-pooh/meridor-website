package ru.meridor.website

import org.scalatra.test.specs2._

class PagesServletSpec extends ScalatraSpec {

  def is = s2"""
    GET / on WebsiteServlet should return status 200 $root200
  """

  addServlet(classOf[PagesServlet], "/*")

  def root200 = get("/") {
    status mustEqual 200
  }
}
