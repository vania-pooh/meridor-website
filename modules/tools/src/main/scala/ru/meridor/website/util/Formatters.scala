package ru.meridor.website.util

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Contains various formatters
 */
object Formatters {

  val DATETIME_FORMAT = "dd.MM.yyyy HH:mm:ss"

  val DATE_FORMAT = "dd.MM.yyyy"

  def timestampToDatetime(ts: Long): String = formatTimestamp(ts, DATETIME_FORMAT)

  def timestampToDate(ts: Long): String = formatTimestamp(ts, DATE_FORMAT)

  private def formatTimestamp(ts: Long, fmt: String) = new SimpleDateFormat(fmt).format(new Date(ts))

}
