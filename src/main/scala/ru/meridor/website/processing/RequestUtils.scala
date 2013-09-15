package ru.meridor.website.processing
import org.fusesource.scalate.servlet.ServletRenderContext._
import java.net.URL

/**
 * Contains some utility methods for request
 */
object RequestUtils {
  /**
   * Returns absolute URL of current request (including request parameters)
   * @return
   */
  def absoluteUrl = {
    val requestURL = request.getRequestURL
    val queryString = request.getQueryString

    if (queryString == null)
      requestURL.toString
      else requestURL.append('?').append(queryString).toString
  }

  def absoluteUrlFromRelative(relativeUrl: String) =
    new URL(rootUrl, relativeUrl).toString

  def rootUrl = new URL(
    request.getScheme,
    request.getServerName,
    request.getServerPort,
    "/"
  )

  def isRootUrl(url: String) = url == rootUrl.toString
}
