package ru.meridor.website.notification.strategies

import ru.meridor.website.notification.entities.SMSNotification

/**
 * SMS sending strategy
 */
class SMSStrategy extends NotificationStrategy[SMSNotification] {
  def doNotify[U >: SMSNotification](notification: U): Boolean =
    IQSMSGate.haveEnoughAccountMoney &&
    (IQSMSGate.sendMessages(
        List.empty[SMSNotification] :+ notification.asInstanceOf[SMSNotification]
    ).acceptedMessages.size > 0)
}
