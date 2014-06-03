package ru.meridor.website.notification.strategies

import ru.meridor.website.notification.NotificationType
import NotificationType._
import ru.meridor.website.notification.entities.Notification

/**
 * Produces notification strategy factory corresponding to notification type
 */
object NotificationStrategyFactory {
  /**
   * Stores already instantiated strategies
   */
  private val notificationStrategies = scala.collection.mutable.HashMap[NotificationType, NotificationStrategy[Notification]]()

  /**
   * Returns corresponding notification strategy by notification
   * @param notification
   * @return
   */
  def get(notification: Notification): NotificationStrategy[Notification] =
    notificationStrategies.get(notification.getType).getOrElse(instanciateNotificationStrategy(notification))

  private def instanciateNotificationStrategy(notification: Notification): NotificationStrategy[Notification] = {
    val strategy = notification.getType match  {
      case SMS => new SMSStrategy()
    }

    notificationStrategies += notification.getType -> strategy
    strategy
  }
}