package ru.meridor.website.processing

import java.util.{TimeZone, Date}
import java.text.SimpleDateFormat
import javax.servlet.http.HttpServletRequest

/**
 * Utility methods to work with headers
 */
object HeaderUtils {

  private val dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz"){
    {
      setTimeZone(TimeZone.getTimeZone("GMT"))
    }
  }

  def headerStringToDate(dateString: String): Date = dateFormat.parse(dateString)

  def dateToHeaderString(date: Date): String = dateFormat.format(date)

  def ifModifiedSinceDate(request: HttpServletRequest): Option[Date] = {
    val ifModifiedSinceHeader = request.getHeader("If-Modified-Since")
    if (ifModifiedSinceHeader != null)
      Some(headerStringToDate(ifModifiedSinceHeader))
      else None
  }

}
