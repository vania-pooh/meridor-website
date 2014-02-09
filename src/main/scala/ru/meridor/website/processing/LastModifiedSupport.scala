package ru.meridor.website.processing

import java.util.{Calendar, TimeZone}

/**
 * This trait adds support for dynamic pages last modification date by storing data hash codes
 * and last modification dates as timestamps
 */
trait LastModifiedSupport {

  private val lastModificationDates = scala.collection.mutable.Map[String, (Long, Int)]()

  private def updateModificationDateAndHashCode(route: String, hashCode: Int): Long = {
    val newModificationDate = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis
    lastModificationDates += (route -> (newModificationDate, hashCode))
    newModificationDate
  }

  def lastModificationTimestamp(route: String, data: (String, Any)*): Long = {
    val dataMap = data.toMap[String, Any]
    val currentDataHashCode = dataMap.hashCode()
    lastModificationDates.get(route) match {
      case Some(record) => {
        val storedDataHashCode = record._2
        val storedLastModificationDate = record._1
        if (currentDataHashCode == storedDataHashCode)
          storedLastModificationDate
          else updateModificationDateAndHashCode(route, currentDataHashCode)
      }
      case None => updateModificationDateAndHashCode(route, currentDataHashCode)
    }
  }

}
