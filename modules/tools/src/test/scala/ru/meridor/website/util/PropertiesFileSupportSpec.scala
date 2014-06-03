package ru.meridor.website.util

import org.specs2.mutable.Specification

class PropertiesFileSupportSpec extends Specification with PropertiesFileSupport {

   "getProperty" should {
     "return Some(existing_value) for existing_key" in {
       getProperty("existing_key") must beEqualTo(Some("existing_value"))
     }
     "return None for missing_key" in {
       getProperty("missing_key") must beEqualTo(None)
     }
   }

  "getPropertyOrEmptyString" should {
    "return existing_value for existing_key" in {
      getPropertyOrEmptyString("existing_key") must beEqualTo("existing_value")
    }
    "return empty string for missing_key" in {
      getPropertyOrEmptyString("missing_key") must beEqualTo("")
    }
  }

  protected def propertiesFileName = "/properties_file_support_test.properties"
}
