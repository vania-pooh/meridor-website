package ru.meridor.website

import ru.meridor.diana.log.LoggingSupport
import ru.meridor.diana.util.PropertiesFileSupport
import org.scalatra.json._
import java.util.Date
import ru.meridor.website.processing.ValidationSupport
import ru.meridor.diana.export.ExportSupport
import ru.meridor.diana.notification.NotificationSupport
import ru.meridor.diana.export.reader.{ServicesList, ServicesListReader}
import ru.meridor.website.processing.export.ServiceListPDFWriter
import org.json4s._
import javax.servlet.http.HttpServletRequest
import ru.meridor.website.processing.RequestUtils.requestParameter
import ru.meridor.diana.db.entities.StoredCalculation
import org.json4s.jackson.JsonMethods

/**
 * A servlet used to process JSON API requests
 */
class ApiServlet extends WebsiteStack
  with LoggingSupport with JacksonJsonSupport
  with PropertiesFileSupport with NotificationSupport
  with ExportSupport {

  /**
   * Request handlers definitions
   */
  logger.info("Initializing API routes...")

  //Place an order
  post("/order"){
      processOrderRequest(parsedBody.extract[Order])
  }

  //Export prices to PDF
  get("/prices/export/pdf"){
    val jsonData = jsonFromRequestParameter("data")
    processPdfRequest(jsonData.extract[ServiceExportRequest])
  }

  //Store price calculation
  post("/prices/store"){
    processStoredCalculationRequest(parsedBody.extract[StoredCalculationRequest])
  }

  logger.info("Done initializing API routes.")

  protected def get(url: String)(action: => Any) = {
    try{
      super.get(url)(action)
    } catch {
      case e: Exception => handleRequestException(e)
    }
  }

  protected def post(url: String)(action: => Any) = {
    try{
      super.post(url)(action)
    } catch {
      case e: Exception => handleRequestException(e)
    }
  }

  private def jsonFromRequestParameter(name: String)(implicit request: HttpServletRequest) = {
    try {
      val pv = requestParameter(name)
      pv match {
        case Some(value) =>
          if (value.length > 0)
            readJsonFromBody(value.asInstanceOf[String])
            else JNothing
        case None => JNothing
      }
    } catch {
      case _: Throwable => JNothing
    }
  }

  /**
   * Global exception handler for requests
   * @param e
   */
  private def handleRequestException(e: Exception){
    e.printStackTrace()
    Response.error("An exception while processing request: " + e.getMessage)
  }

  private def processOrderRequest(order: Order): Response = if (order.isValid) {
    import ru.meridor.diana.db.entities.Person

    order.getPhoneNumber match {
      case Some(pn) => {
        Person.createOrUpdate(new Person(cellPhone = pn, firstName = order.clientName)) match {
          case Some(person) => {
            val message = MessageProvider.clientMessage(order.getAdditionalData)
            if (
              //TODO: we also need to check whether user number is a mobile phone number by city code and send message only to mobile phones
              (getOperatorPhoneNumber match {
                case Some(opn) => sendSMS(opn, MessageProvider.operatorMessage(order))
                case None => false
              })
              && sendSMS(pn, message, warnAboutLowFunds = true)
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

  private def sendSMS(phoneNumber: Long, msg: String, warnAboutLowFunds: Boolean = false): Boolean = {
    import ru.meridor.diana.notification.entities.{SMSNotification, LowFundsEvent}
    val notification = new SMSNotification(
      id = new Date().getTime,
      phone = phoneNumber,
      sender = "Meridor",
      message = msg,
      supportsLowFundsEvent = warnAboutLowFunds
    )
    notification.addEventHandler(LowFundsEvent){
      funds: Double => {
        getOperatorPhoneNumber match {
          case Some(pn) => sendSMS(pn, "Заканчиваются средства на отправку сообщений клиентам. Текущий баланс " + funds + ".")
          case None => ()
        }
      }
    }
    sendNotification(notification)
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

  private def processPdfRequest(exportRequest: ServiceExportRequest) = if (exportRequest.isValid){
    import ru.meridor.diana.export.Job
    contentType = "application/pdf"
    response.setHeader("Content-Disposition", "attachment; filename=\"price.pdf\"")
    val outputStream = response.getOutputStream
    val servicesListReader = if (exportRequest.exportAllServices)
      ServicesListReader(List[Long]())
      else ServicesListReader(exportRequest.serviceIds.zip(exportRequest.quantities).toMap)
    export(Job[ServicesList, ServicesList](
      reader = servicesListReader,
      writer = new ServiceListPDFWriter(outputStream)
    ))
    outputStream.close()
  } else Response.error("You should specify a list of service IDs to be exported and a list of quantities for each service. Both lists should have the same length.")

  def processStoredCalculationRequest(storedCalculationRequest: StoredCalculationRequest) = if (storedCalculationRequest.isValid) {
    val storedCalculationId = StoredCalculation.insert(storedCalculationRequest.title, storedCalculationRequest.data)
    Response.ok(Map("id" -> storedCalculationId.toString))
  } else if (!storedCalculationRequest.isTitlePresent)
    Response.error("Display name can't be empty.")
  else if (!storedCalculationRequest.isCalculationDataPresent)
    Response.error("Calculation data can't be empty and should be a valid JSON with (service_id, quantity) pairs.")

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

case class Response(code: String, stringCode: Option[String] = None, message: String, data: Map[String, String] = Map())

object Response{
  import ResponseCode._
  def ok(): Response = new Response(code = Ok,  message = "")
  def ok(dataMap: Map[String, String]): Response = new Response(code = Ok,  message = "", data = dataMap)
  def error(msg: String): Response = new Response(code = Error, message = msg)
  def error(stringCode: String, msg: String): Response = new Response(Error, Some(stringCode), msg)
}

object ResponseCode {
  val Ok = "ok"
  val Error = "error"
}

case class Order(phone: String, clientName: String, additionalData: String) extends ValidationSupport {

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
      case LightingSystem => "система освещения"
      case CallElectrician => "вызов электрика"
      case RoomRepair => "ремонт квартир и офисов"
      case ElectricalAppliances => "электроприборы"
      case ElectricRange => "установка электрической плиты"
      case WashingMachine => "установка стиральной машины"
      case DishWashingMachine => "установка посудомоечной машины"
      case Oven => "установка духового шкафа"
      case Hob => "установка варочной панели"
      case MicrowaveOven => "установка микроволновой печи"
      case Telecommunication => "телекоммуникации"
      case Prices => "цены"
      case ElectricalBundle => "пакет \"Добрый электрик\""
      case ComputerBundle => "пакет \"Компьютерный гений\""
      case LightingBundle => "пакет \"Яркий мир\""
      case Contact => "контакты"
      case _ => "другое"
    }) + "."

}

case class ServiceExportRequest(exportAllServices: Boolean = true, serviceIds: List[Long] = List(), quantities: List[Double] = List()) extends ValidationSupport {

  def isValid: Boolean = exportAllServices || (
      !exportAllServices && areServiceIdsPresent && (
        !areQuantitiesPresent || (areQuantitiesPresent && (quantities.length == serviceIds.length) )
      )
    )

  def areServiceIdsPresent = serviceIds.length > 0

  def areQuantitiesPresent = quantities.length > 0

}

case class StoredCalculationRequest(title: String, data: String) extends ValidationSupport {

  def isValid: Boolean =  isTitlePresent && isCalculationDataPresent

  def isTitlePresent = title.length > 0

  def isCalculationDataPresent = {
    try {
       (data.size > 0) || {
         //I.e. string is correct and non-empty JSON
         val json = JsonMethods.parse(string2JsonInput(data))
         json.children.size > 0
       }
    }
    catch {
      case _: Exception => false
    }
  }

}