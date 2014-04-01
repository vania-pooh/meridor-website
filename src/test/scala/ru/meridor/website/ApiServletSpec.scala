package ru.meridor.website

import org.scalatra.test.specs2.ScalatraSpec
import ru.meridor.diana.notification.NotificationSupport
import org.specs2.mock._

class ApiServletSpec extends ScalatraSpec with Mockito {

  def is = s2"""
    POST /order with correct data should return 200
  """

  addServlet(classOf[ApiServlet], "/*")

  private val notifier = mock[NotificationSupport]

}
