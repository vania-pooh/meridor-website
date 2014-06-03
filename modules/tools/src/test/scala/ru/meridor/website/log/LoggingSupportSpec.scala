package ru.meridor.website.log

import org.specs2.mutable.Specification
import org.slf4j.Logger

class LoggingSupportSpec extends Specification with LoggingSupport {

   "logger field" should {
     "be instance of Logger" in {
       logger must beAnInstanceOf[Logger]
     }
   }

 }
