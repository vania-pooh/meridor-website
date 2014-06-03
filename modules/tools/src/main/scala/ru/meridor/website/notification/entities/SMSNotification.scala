package ru.meridor.website.notification.entities

import ru.meridor.website.notification.NotificationType._
import ru.meridor.website.event.{Event, EventSupport}

/**
 * SMS notification record
 */
case class SMSNotification(id: Long, phone: Long, sender: String = "Diana CRM", message: String, supportsLowFundsEvent: Boolean = false)
  extends Notification with EventSupport {

  override def getType: NotificationType = SMS

  def supportedEvents = List(LowFundsEvent)

}

object LowFundsEvent extends Event[Double]
