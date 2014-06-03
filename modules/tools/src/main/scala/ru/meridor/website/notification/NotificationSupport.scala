package ru.meridor.website.notification

import ru.meridor.website.notification.strategies.NotificationStrategyFactory
import ru.meridor.website.notification.entities.Notification

/**
 * A base interface for any notification subsystem. notify() is reserved name for java.lang.Object method.
 */
trait NotificationSupport {
  /**
   * Notifies user about any events in the system
   * @param notification an object with notification information
   * @return whether notification was sent successfully
   */
  def sendNotification[U <: Notification](notification: U) : Boolean = {
    if (notification == null) false
      else NotificationStrategyFactory.get(notification).doNotify(notification)
  }
}
