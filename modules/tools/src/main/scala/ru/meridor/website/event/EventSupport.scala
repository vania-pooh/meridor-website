package ru.meridor.website.event

import scala.collection.mutable.ArrayBuffer

trait EventSupport {

  type EventHandler[T] = T => Unit

  private lazy val eventHandlers = {
    val ret = scala.collection.mutable.HashMap[String, ArrayBuffer[EventHandler[_]]]()
    supportedEvents.map(e => e.id).foreach {
      e => ret += e -> ArrayBuffer()
    }
    ret.toMap
  }

  def supportedEvents: List[Event[_]]

  private def checkEventSupported(event: Event[_]) = if (!supportedEvents.contains(event)){
    throw new IllegalArgumentException("Event " + event + " is not supported")
  }

  def addEventHandler[T](event: Event[T])(handler: EventHandler[T]) {
    checkEventSupported(event)
    eventHandlers(event.id) += handler
  }

  def raiseEvent[T](event: Event[T])(data: T) {
    checkEventSupported(event)
    eventHandlers(event.id).foreach {
      h => h.asInstanceOf[EventHandler[T]](data)
    }
  }

}

trait Event[+T] {

  def id: String = getClass.getName

  override def equals(obj: Any) =
    obj.isInstanceOf[Event[_]] && obj.asInstanceOf[Event[_]].id == id

  override def toString = id

  override def hashCode() = id.hashCode

}