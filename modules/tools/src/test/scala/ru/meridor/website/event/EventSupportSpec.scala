package ru.meridor.website.event

import org.specs2.mutable.Specification
import java.lang.IllegalArgumentException

class EventSupportSpec extends Specification {

  "EventSupport" should {
    "call handler on each supported event" in {

      object IncrementCounterEvent extends Event[Int]

      class TestClass extends EventSupport {
        def supportedEvents = List(IncrementCounterEvent)
      }

      val test = new TestClass
      var counter = 0
      test.addEventHandler(IncrementCounterEvent) {
        _ => counter += 1
      }

      1 to 10 foreach {
        number => {
          test.raiseEvent(IncrementCounterEvent)(number)
          counter must beEqualTo(number)
        }
      }

    }

    "throw an IllegalArgumentException when using unsupported event" in {

      object UnsupportedEvent extends Event[Any]

      class TestClass extends EventSupport {
        def supportedEvents = List()
      }

      val test = new TestClass
      test.addEventHandler(UnsupportedEvent)(_ => None) must throwA[IllegalArgumentException]
      test.raiseEvent(UnsupportedEvent)(None) must throwA[IllegalArgumentException]

    }
  }

  "Event" should {

    object Event extends Event[Int]

    object EventWithTheSameId extends Event[Int] {
      override def id = Event.id
    }

    "correctly work with equals()" in {
      Event must beEqualTo(EventWithTheSameId)
    }

    "correctly work with hashCode()" in {
      Event.hashCode() must beEqualTo(EventWithTheSameId.hashCode())
    }

  }

}
