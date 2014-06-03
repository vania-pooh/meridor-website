package ru.meridor.website.notification.entities

import ru.meridor.website.notification.NotificationType._

/**
 * A base class for notification information object
 */
abstract class Notification {

  /**
   * Returns notification type
   * @return
   */
  def getType: NotificationType

}