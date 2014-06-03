package ru.meridor.website.util

import java.net.URL
import java.util.{TimeZone, Date}
import java.text.SimpleDateFormat
import scala.Some
import java.io.File
import org.scalatra.Control
import javax.servlet.http.HttpServletRequest

/**
 * Contains some utility methods for request
 */
object RequestUtils extends Control {

  /**
   * Returns absolute URL of current request (including request parameters)
   * @return
   */
  def absoluteUrl(implicit request: HttpServletRequest): URL = {
    val requestURL = request.getRequestURL
    val queryString = request.getQueryString

    if (queryString == null)
      new URL(requestURL.toString)
      else new URL(requestURL.append('?').append(queryString).toString)
  }

  def absoluteUrlFromRelative(relativeUrl: String)(implicit request: HttpServletRequest): URL =
    new URL(rootUrl, relativeUrl)

  def rootUrl(implicit request: HttpServletRequest): URL = {
    val serverPort = request.getServerPort
    if (serverPort == 80)
      new URL(
        request.getScheme,
        request.getServerName,
        "/"
      )
      else new URL(
        request.getScheme,
        request.getServerName,
        request.getServerPort,
        "/"
      )
  }

  def rootPath(implicit request: HttpServletRequest) = new File(request.getServletContext.getRealPath("/"))

  def isRootUrl(url: URL)(implicit request: HttpServletRequest) = url == rootUrl

  private val dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz"){
    {
      setTimeZone(TimeZone.getTimeZone("GMT"))
    }
  }

  def headerStringToDate(dateString: String): Date = dateFormat.parse(dateString)

  def dateToHeaderString(date: Date): String = dateFormat.format(date)

  def ifModifiedSinceDate(implicit request: HttpServletRequest): Option[Date] = {
    val ifModifiedSinceHeader = request.getHeader("If-Modified-Since")
    if (ifModifiedSinceHeader != null)
      Some(headerStringToDate(ifModifiedSinceHeader))
    else None
  }

  def requestParameter(name: String)(implicit request: HttpServletRequest): Option[String] = {
    val value = request.getParameter(name)
    if (value != null)
      Some(value)
      else None
  }

  /**
   * Returns 301 permanent redirect
   * @param relativeUrl
   */
  def permanentRedirect(relativeUrl: String)(implicit request: HttpServletRequest) =
    halt(status = 301,
      reason = "Moved Permanently",
      headers = Map("Location" -> absoluteUrlFromRelative(relativeUrl).toString)
    )

  /**
   * Returns 304 not modified status
   * @return
   */
  def notModified = halt(
    status = 304,
    reason = "Not Modified"
  )

}
