package ru.meridor.website.processing

import org.specs2.mutable.Specification
import java.util.Date
import ru.meridor.website.util.LastModifiedSupport

class LastModifiedSupportSpec extends Specification with LastModifiedSupport {

  var timestamp: Long = new Date().getTime
  val route = "notExistingRoute"
  val data = "nonExistingKey" -> "value"
  val updatedData = "anotherNonExistingKey" -> "anotherValue"

  sequential

  "lastModificationTimestamp" should {

    "return current timestamp on first call" in {
      val initialTimestamp = new Date().getTime
      val returnedTimestamp = lastModificationTimestamp(route, data)
      timestamp = returnedTimestamp
      returnedTimestamp must beGreaterThan(initialTimestamp)
    }

    "return stored timestamp on next call" in {
      lastModificationTimestamp(route, data) must beEqualTo(timestamp)
    }

    "return updated timestamp on next call with different data" in {
      lastModificationTimestamp(route, updatedData) must beGreaterThan(timestamp)
    }

  }

}
