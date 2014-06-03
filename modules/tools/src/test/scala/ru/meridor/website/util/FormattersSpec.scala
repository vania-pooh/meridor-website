package ru.meridor.website.util

import org.specs2.mutable.Specification
import ru.meridor.website.util.Formatters._

class FormattersSpec extends Specification {

  "DATE_FORMAT" should {
    "contain year" in {
      DATE_FORMAT must contain("y")
    }
    "contain month" in {
      DATE_FORMAT must contain("M")
    }
    "contain day" in {
      DATE_FORMAT must contain("d")
    }
  }

  "DATETIME_FORMAT" should {
    "contain year" in {
      DATETIME_FORMAT must contain("y")
    }
    "contain month" in {
      DATETIME_FORMAT must contain("M")
    }
    "contain day" in {
      DATETIME_FORMAT must contain("d")
    }
    "contain hours" in {
      DATETIME_FORMAT must contain("H")
    }
    "contain minutes" in {
      DATETIME_FORMAT must contain("m")
    }
    "contain seconds" in {
      DATETIME_FORMAT must contain("s")
    }
  }

  "timestampToDate(0)" should {
    "return 01.01.1970" in {
      timestampToDate(0) must equalTo("01.01.1970")
    }
  }

  "timestampToDatetime(0)" should {
    "return 01.01.1970 03:00:00" in {
      timestampToDatetime(0) must equalTo("01.01.1970 03:00:00")
    }
  }
}
