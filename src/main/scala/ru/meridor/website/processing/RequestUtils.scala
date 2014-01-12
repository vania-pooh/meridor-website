package ru.meridor.website.processing
import java.net.URL
import java.util.{TimeZone, Date}
import java.text.SimpleDateFormat
import javax.servlet.http.HttpServletRequest
import scala.Some
import org.scalatra.Control
import java.io.File

/**
 * Contains some utility methods for request
 */
object RequestUtils extends Control {

  /**
   * Returns absolute URL of current request (including request parameters)
   * @return
   */
  def absoluteUrl(implicit request: HttpServletRequest) = {
    val requestURL = request.getRequestURL
    val queryString = request.getQueryString

    if (queryString == null)
      requestURL.toString
      else requestURL.append('?').append(queryString).toString
  }

  def absoluteUrlFromRelative(relativeUrl: String)(implicit request: HttpServletRequest) =
    new URL(rootUrl, relativeUrl).toString

  def rootUrl(implicit request: HttpServletRequest) = {
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

  def isRootUrl(url: String)(implicit request: HttpServletRequest) = url == rootUrl.toString

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

  /**
   * Returns base URL of current request
   * @param request
   * @return
   */
  def baseUrl(implicit request: HttpServletRequest) = request.getRequestURL.toString.replace(request.getRequestURI.substring(1), request.getContextPath)

  /**
   * Returns 301 permanent redirect
   * @param relativeUrl
   */
  def permanentRedirect(relativeUrl: String)(implicit request: HttpServletRequest) =
    halt(status = 301,
      reason = "Moved Permanently",
      headers = Map("Location" -> absoluteUrlFromRelative(relativeUrl))
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
