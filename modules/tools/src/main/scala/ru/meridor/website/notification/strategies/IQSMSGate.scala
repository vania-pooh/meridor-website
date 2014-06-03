package ru.meridor.website.notification.strategies

import java.util.Date
import ru.meridor.website.util.{PropertiesFileSupport, JSONPostRequestSupport}
import ru.meridor.website.notification.entities.{LowFundsEvent, SMSNotification}
import org.joda.time.DateTime

/**
 * Case class storing two lists - accepted and rejected messages
 * @param acceptedMessages
 * @param rejectedMessages
 */
case class AcceptedRejectedMessages(acceptedMessages: List[SMSNotification], rejectedMessages: List[SMSNotification])

object IQSMSGate {

  private lazy val gateInstance = new IQSMSGate

  def sendMessages(msgs: List[SMSNotification]): AcceptedRejectedMessages = gateInstance.sendMessages(messages = msgs)

  def getAvailableMessagesNumber: Double = gateInstance.getBalance

  def haveEnoughAccountMoney = gateInstance.haveEnoughAccountMoney

  def getSenders: List[String] = gateInstance.getSenders
}

/**
 * Encapsulates http://iqsms.ru/ JSON API for sending SMS
 */
class IQSMSGate extends JSONPostRequestSupport with PropertiesFileSupport {

  private val STATUS_OK = "ok"
  private val SMS_ACCEPTED = "accepted"

  /**
   * SMS notification properties
   */

  protected def propertiesFileName: String = "/iqsms.properties"

  private def getBaseUrl: String = getPropertyOrEmptyString("sms.url")
  private def getLogin: String = getPropertyOrEmptyString("sms.login")
  private def getPassword: String = getPropertyOrEmptyString("sms.password")
  private def getLowFundsThreshold: Double = getProperty("sms.lowFundsThreshold") match {
    case Some(threshold) => threshold.asInstanceOf[Double]
    case None => 100 * SMS_MAX_PRICE
  }

  private def canProcessRequests: Boolean = (getBaseUrl.size > 0) && (getLogin.size > 0) && (getPassword.size > 0)

  private def getUrl(uri: String): String = getBaseUrl + uri + ".json"

  private def getDefaultRequestParameters: Map[String, Any] =
    Map[String, Any]("login" -> getLogin, "password" -> getPassword)

  private def isRequestSuccessful(response: Map[String, Any]): Boolean = response.get("status") match {
    case Some(status) => status == STATUS_OK
    case None => false
  }

  /**
   * Sends a list of messages
   * @param messages
   * @param statusQueueName
   * @param scheduleTime
   * @return
   */
  def sendMessages(
      messages: List[SMSNotification],
      statusQueueName: Option[String] = None,
      scheduleTime: Option[Date] = None
  ): AcceptedRejectedMessages =
    if (messages.size > 0) {

      val requestParameters = scala.collection.mutable.Map[String, Any]()
      requestParameters ++= getDefaultRequestParameters
      statusQueueName match {
        case Some(name) => requestParameters += ("statusQueueName" -> name)
        case None => ()
      }
      scheduleTime match {
        case Some(time) => requestParameters += ("scheduleTime" -> new DateTime(time).toString())
        case None => ()
      }

      var messagesList = List[Map[String, Any]]()
      for (message <- messages){
        messagesList ::= Map(
          "clientId" -> message.id,
          "phone" -> ("+7" + message.phone.toString), //IQSMS requires +7 to be included
          "text" -> message.message,
          "sender" -> message.sender
        )
      }
      requestParameters += ("messages" -> messagesList)

      val response = sendRequest(getUrl("send"), requestParameters.toMap[String, Any])
      if (isRequestSuccessful(response)){
          response.get("messages") match {
            case Some(processedMessages) => {
              val balance = getBalance
              val messagesWithLowFundsEvent = messages filter (_.supportsLowFundsEvent)
              if (shouldSendLowFundsMessage(balance) && messagesWithLowFundsEvent.length > 0){
                messagesWithLowFundsEvent(0).raiseEvent(LowFundsEvent)(balance)
              }
              classifyProcessedMessages(messages, processedMessages.asInstanceOf[List[Map[String, Any]]])
            }
            case None => allMessagesRejected(messages)
          }
      } else allMessagesRejected(messages)
    } else noMessagesSent

  private def noMessagesSent = new AcceptedRejectedMessages(List.empty[SMSNotification], List.empty[SMSNotification])
  private def allMessagesRejected(messages: List[SMSNotification]) = new AcceptedRejectedMessages(List.empty[SMSNotification], messages)

  private def classifyProcessedMessages(messages: List[SMSNotification], processedMessages: List[Map[String, Any]]): AcceptedRejectedMessages = {
    var acceptedMessages = List[SMSNotification]()
    var rejectedMessages = List[SMSNotification]()
    for (processedMessage <- processedMessages){
      val clientId = processedMessage("clientId").asInstanceOf[Double].longValue
      val status = processedMessage("status")
      status match {
        case SMS_ACCEPTED => getMessageById(messages, clientId) match {
            case Some(msg) => acceptedMessages ::= msg
            case None => ()
          }
        case _ => getMessageById(messages, clientId) match {
          case Some(msg) => rejectedMessages ::= msg
          case None => ()
        }
      }
    }
    new AcceptedRejectedMessages(acceptedMessages.toList, rejectedMessages.toList)
  }

  private def getMessageById(messages: List[SMSNotification], id: Long): Option[SMSNotification] = {
    val filteredMessages = messages.filter(_.id == id)
    filteredMessages.size match {
      case 0 => None
      case _ => Some(filteredMessages.head)
    }
  }
  /**
   * Returns total amount of messages that can be sent with current money amount
   * @return
   */
  def getBalance: Double = {
    val response = sendRequest(getUrl("balance"), getDefaultRequestParameters)
    response.size match {
      case 2 => if (isRequestSuccessful(response)){
        val balanceDataList = response("balance").asInstanceOf[List[Map[String, Any]]]
        val firstBalanceRecord = balanceDataList(0)
        firstBalanceRecord("balance").asInstanceOf[Double]
      }
      else 0d
      case _ => 0d
    }
  }

  /**
   * Depending on selected package a single SMS can cost up to this price
   */
  val SMS_MAX_PRICE = 0.5

  /**
   * Returns whether we have enough money to send this message
   * @return
   */
  def haveEnoughAccountMoney = getBalance > 2 * SMS_MAX_PRICE

  /**
   * Returns whether we have low money and need to send respective message
   * When money amount becomes too small we need to even not send warning messages.
   * @return
   */
  private def shouldSendLowFundsMessage(balance: Double) = (balance < getLowFundsThreshold) && (balance > 0.5 * getLowFundsThreshold)

  /**
   * Returns a list of available message senders
   * @return
   */
  def getSenders: List[String] = {
    val response = sendRequest(getUrl("senders"), getDefaultRequestParameters)
    response.size match {
      case 2 => if (isRequestSuccessful(response))
        response("senders").asInstanceOf[List[String]] else List.empty[String]
      case _ => List.empty[String]
    }
  }

  protected override def sendRequest(url: String, requestParameters: Map[String, Any]): Map[String, Any] =
    if (canProcessRequests) super.sendRequest(url, requestParameters) else Map.empty[String, Any]

}
