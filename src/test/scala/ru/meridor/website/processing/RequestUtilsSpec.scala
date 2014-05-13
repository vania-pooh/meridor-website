package ru.meridor.website.processing

import org.specs2.mutable.Specification
import javax.servlet.http.HttpServletRequest
import org.mockito.Mockito._
import RequestUtils._
import java.net.URL
import org.joda.time.{DateTimeZone, DateTime}

class RequestUtilsSpec extends Specification {

  val randomPort = Math.floor(Math.random() * 1000).toInt

  "rootUrl" should {

    "return http://localhost/ for default port" in {
      implicit val request: HttpServletRequest = MockedRequest().get
      rootUrl must beEqualTo(new URL("http://localhost/"))
    }

    "return http://localhost:" + randomPort + "/ for port = " + randomPort in {
      implicit val request: HttpServletRequest = MockedRequest(port = randomPort).get
      rootUrl must beEqualTo(new URL("http://localhost:" + randomPort + "/"))
    }

  }

  "absoluteUrlFromRelative" should {

    "return http://localhost/ for / and default port" in {
      implicit val request: HttpServletRequest = MockedRequest().get
      absoluteUrlFromRelative("/") must beEqualTo(new URL("http://localhost/"))
    }

    "return http://localhost:" + randomPort + "/ for / and port = " + randomPort in {
      implicit val request: HttpServletRequest = MockedRequest(port = randomPort).get
      absoluteUrlFromRelative("/") must beEqualTo(new URL("http://localhost:" + randomPort + "/"))
    }

    "return http://localhost/123/456 for /123/456 and default port" in {
      implicit val request: HttpServletRequest = MockedRequest().get
      absoluteUrlFromRelative("/123/456") must beEqualTo(new URL("http://localhost/123/456"))
    }

  }

  "isRootUrl" should {

    "return true for http://localhost/ for default port" in {
      implicit val request: HttpServletRequest = MockedRequest().get
      isRootUrl(new URL("http://localhost/")) must beTrue
    }

    "return true for http://localhost:" + randomPort + "/ for port " + randomPort in {
      implicit val request: HttpServletRequest = MockedRequest(port = randomPort).get
      isRootUrl(new URL("http://localhost:" + randomPort + "/")) must beTrue
    }

    "return false for http://localhost/123 for default port" in {
      implicit val request: HttpServletRequest = MockedRequest().get
      isRootUrl(new URL("http://localhost/123")) must beFalse
    }

  }

  val someDate = new DateTime(
    2014, 4, 8,
    21, 36, 6,
    DateTimeZone.UTC
  ).toDate

  "headerStringToDate" should {

    "return Date(08.04.2014 21:36:06 GMT) for string Tue, 08 Apr 2014 21:36:06 GMT" in {
      headerStringToDate("Tue, 08 Apr 2014 21:36:06 GMT") must beEqualTo(someDate)
    }

  }

  "dateToHeaderString" should {
    
    "return Tue, 08 Apr 2014 21:36:06 GMT for Date(08.04.2014 21:36:06 GMT)" in {
      dateToHeaderString(someDate) must beEqualTo("Tue, 08 Apr 2014 21:36:06 GMT")
    }
    
  }

  "dateToHeaderString and headerStringToDate" should {
    "inverse each other" in {
      val currentDate = new DateTime().toDate
      headerStringToDate(dateToHeaderString(currentDate)) must beEqualTo(
        new DateTime(currentDate).withMillisOfSecond(0).toDate //We reset milliseconds because header only stores seconds
      )
    }
  }

  val headerName = "If-Modified-Since"
  "ifModifiedSinceDate" should {

    "return None when \"" + headerName + "\" header is missing" in {
      implicit val request: HttpServletRequest = MockedRequest().get
      ifModifiedSinceDate must beEqualTo(None)
    }

    "return Some(" + someDate + ") when \"" + headerName + "\" header equals to " + someDate in {
      implicit val request: HttpServletRequest = MockedRequest().get
      when(request.getHeader(headerName)).thenReturn(dateToHeaderString(someDate))
      ifModifiedSinceDate must beEqualTo(Some(someDate))
    }

  }

  "requestParameter" should {
    val parameterName = "someName"
    val parameterValue = "someValue"
    "return Some(value) if request parameter is present" in {
      implicit val request: HttpServletRequest = MockedRequest(parameters = Map(parameterName -> parameterValue)).get
      requestParameter(parameterName) must beEqualTo(Some(parameterValue))
    }

    "return None if request parameter is not present" in {
      implicit val request: HttpServletRequest = MockedRequest().get
      requestParameter(parameterName) must beEqualTo(None)
    }
  }

}

case class MockedRequest(
                          scheme: String = "http",
                          serverName: String = "localhost",
                          port: Int = 80,
                          uri: String = "",
                          queryString: String = "",
                          parameters: Map[String, String] = Map()){

  def get = {
    val request = mock(classOf[HttpServletRequest])
    when(request.getScheme).thenReturn(scheme)
    when(request.getServerName).thenReturn(serverName)
    when(request.getServerPort).thenReturn(port)
    when(request.getRequestURI).thenReturn(uri)
    when(request.getQueryString).thenReturn(queryString)
    parameters.foreach {
      (entry: (String, String)) => when(request.getParameter(entry._1)).thenReturn(entry._2)
    }
    request
  }

}
