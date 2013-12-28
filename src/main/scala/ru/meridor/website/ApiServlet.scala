package ru.meridor.website

import ru.meridor.diana.log.LoggingSupport
import org.json4s.{DefaultFormats, Formats}
import ru.meridor.diana.util.PropertiesFileSupport
import org.scalatra.json._
import org.json4s.JValue
import java.util.Date

/**
 * A servlet used to process JSON API requests
 */
class ApiServlet extends WebsiteStack with LoggingSupport with JacksonJsonSupport with PropertiesFileSupport {

  /**
   * Request handlers definitions
   */
  post("/order"){
    try{
      processOrderRequest(parsedBody.extract[Order])
    } catch {
      case e: Exception => {
        e.printStackTrace()
        Response.error("An exception while processing request: " + e.getMessage)
      }
    }
  }

  private def processOrderRequest(order: Order): Response = if (order.isValid) {
    import ru.meridor.diana.db.entities.Person

    order.getPhoneNumber match {
      case Some(pn) => {
        Person.createOrUpdate(new Person(cellPhone = pn, firstName = order.clientName)) match {
          case Some(person) => {
            val message = MessageProvider.clientMessage(order.getAdditionalData)
            if (
              //TODO: we also need to send a single message to operator if account balance < 100 RUB
              //TODO: we also need to check whether user number is a mobile phone number by city code and send message only to mobile phones
              (getOperatorPhoneNumber match {
                case Some(opn) => sendSMS(opn, MessageProvider.operatorMessage(order))
                case None => false
              })
              && sendSMS(pn, message)
            )
              Response.ok()
              else Response.error("An error while sending SMS")
          }
          case None => Response.error("An error while creating / updating person")
        }
      }
      case _ => Response.error("Invalid phone number")
    }
  } else if (!order.isClientNameValid) Response.error("Invalid client name")
    else if (!order.isAdditionalDataValid) Response.error("Invalid additional data")
    else Response.error("Unknown error")

  private def sendSMS(phoneNumber: Long, msg: String): Boolean = {
    import ru.meridor.diana.notification.Notifier
    import ru.meridor.diana.notification.entities.SMSNotification
    Notifier.sendNotification(
      new SMSNotification(
        id = new Date().getTime,
        phone = phoneNumber,
        sender = "Meridor",
        message = msg
      )
    )
  }

  private def getOperatorPhoneNumber: Option[Long] = {
    try {
      getProperty("ws.orderPhoneNumber") match {
        case Some(pn) => Some(pn.toLong)
        case None => None
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        None
      }
    }
  }

  /**
   * Sets up automatic case class to JSON output serialization, required by the JValueResult trait.
   */
  protected implicit val jsonFormats: Formats = DefaultFormats

  /**
   * Before every action runs, set the content type to be in JSON format.
   */
  before() {
    contentType = formats("json")
  }

  /**
   * JSON key names in request are sent underscored, i.e. my_key and automatically transformed to myKey format
   * @param body
   * @return
   */
  protected override def transformRequestBody(body: JValue): JValue = body.camelizeKeys

  /**
   * The reverse action - transform camelCase Scala names to underscored JSON names
   * @param body
   * @return
   */
  protected override def transformResponseBody(body: JValue): JValue = body.underscoreKeys

  protected override def propertiesFileName: String = "/website.properties"

}

/**
 * A set of utility classes used in the API
 */

case class Response(code: String, stringCode: Option[String] = None, message: String)

object Response{
  import ResponseCode._
  def ok(): Response = new Response(code = Ok,  message = "")
  def error(msg: String): Response = new Response(code = Error, message = msg)
  def error(stringCode: String, msg: String): Response = new Response(Error, Some(stringCode), msg)
}

object ResponseCode {
  val Ok = "ok"
  val Error = "error"
}

case class Order(phone: String, clientName: String, additionalData: String){

  import ru.meridor.website.processing.OrderFormAdditionalData
  import java.util.regex.{Matcher, Pattern}

  def isValid: Boolean = isPhoneValid && isClientNameValid && isAdditionalDataValid

  def getPhoneNumber: Option[Long] = {
    val matcher: Matcher = Pattern.compile("(\\+7|8)(\\d{10})").matcher(phone)
    if (matcher.find()){
      try{
        Some(matcher.group(2).toLong) //We get phone number without +7 or 8, e.g. +79211234567 -> 9211234567
      } catch {
        case _: Exception => None
      }
    }
    else None
  }

  def isPhoneValid: Boolean = getPhoneNumber match {
    case Some(_) => true
    case None => false
  }

  def isClientNameValid: Boolean = clientName.length > 0

  def isAdditionalDataValid: Boolean = OrderFormAdditionalData.getAvailableValues.keySet.contains(additionalData)

  def getAdditionalData: OrderFormAdditionalData =
    OrderFormAdditionalData.getAvailableValues.getOrElse(additionalData, OrderFormAdditionalData.None)
}

object MessageProvider {
  import ru.meridor.website.processing.OrderFormAdditionalData
  import ru.meridor.website.processing.OrderFormAdditionalData._
  def clientMessage(additionalData: OrderFormAdditionalData): String = additionalData match {
//    case ElectricalWorks => ""
    case _ => "Благодарим вас за заказ. Мы свяжемся с вами в ближайшее время."
  }

  def operatorMessage(order: Order): String = "Номер: " + order.phone + ", клиент: " + order.clientName + ", информация: " +
    (order.getAdditionalData match {
      case Index => "главная страница"
      case ElectricalWorks => "электромонтажные работы"
      case TechnicalMaintenance => "техническое обслуживание"
      case Lighting => "освещение"
      case CallElectrician => "вызов электрика"
      case RoomRepair => "ремонт квартир и офисов"
      case ElectricalAppliances => "электроприборы"
      case Telecommunication => "телекоммуникации"
      case Prices => "цены"
      case ElectricalBundle => "пакет \"Добрый электрик\""
      case ComputerBundle => "пакет \"Компьютерный гений\""
      case LightingBundle => "пакет \"Яркий мир\""
      case Contact => "контакты"
      case _ => "другое"
    }) + "."

}
