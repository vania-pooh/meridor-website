package ru.meridor.website.util

import org.specs2.mutable.Specification

/**
 *
 */
class CamelCaseSupportSpec extends Specification with CamelCaseSupport {

  "underscoredToCamelCase with lcFirst disabled" should {
    "convert SOME_STRING to SomeString" in {
      underscoredToCamelCase("SOME_STRING") must beEqualTo("SomeString")
    }
    "convert some_string to SomeString" in {
      underscoredToCamelCase("some_string") must beEqualTo("SomeString")
    }
    "convert somestring to Somestring" in {
      underscoredToCamelCase("somestring") must beEqualTo("Somestring")
    }
  }

  "underscoredToCamelCase with lcFirst enabled" should {
    "convert SOME_STRING to someString" in {
      underscoredToCamelCase("SOME_STRING", lcFirst = true) must beEqualTo("someString")
    }
    "convert some_string to someString" in {
      underscoredToCamelCase("some_string", lcFirst = true) must beEqualTo("someString")
    }
    "leave somestring unchanged" in {
      underscoredToCamelCase("somestring", lcFirst = true) must beEqualTo("somestring")
    }
  }

  "camelCaseToUnderscored" should {
    "convert SomeString to SOME_STRING" in {
      camelCaseToUnderscored("SomeString") must beEqualTo("SOME_STRING")
    }
    "convert someString to SOME_STRING" in {
      camelCaseToUnderscored("someString") must beEqualTo("SOME_STRING")
    }
    "convert somestring to SOMESTRING" in {
      camelCaseToUnderscored("somestring") must beEqualTo("SOMESTRING")
    }
  }

  "toProperCase" should {
    "convert somestring to Somestring" in {
      toProperCase("somestring") must beEqualTo("Somestring")
    }
    "convert SOMESTRING to Somestring" in {
      toProperCase("SOMESTRING") must beEqualTo("Somestring")
    }
    "convert some_string to Some_string" in {
      toProperCase("some_string") must beEqualTo("Some_string")
    }
  }

}
