package ru.meridor.website

import org.scalatra.test.specs2.ScalatraSpec
import ru.meridor.diana.notification.NotificationSupport
import org.specs2.mock._
import org.specs2.mutable.Specification
import ru.meridor.website.processing.OrderFormAdditionalData

class ApiServletSpec extends ScalatraSpec with Mockito {

  def is = s2"""
    POST /order with correct data should return 200
  """

  addServlet(classOf[ApiServlet], "/*")

  private val notifier = mock[NotificationSupport]

}

class OrderSpec extends Specification {

  "valid order" should {
    val validPhoneNumber = "9211234567"
    val validClientName = "test-client"
    val validAdditionalFormData = OrderFormAdditionalData.CallElectrician
    val validOrder = Order("+7" + validPhoneNumber, validClientName, validAdditionalFormData.getData)

    "return true for isValid" in {
      validOrder.isValid must beTrue
    }

    "return Some(" + validPhoneNumber + ") for getPhoneNumber" in {
      validOrder.getPhoneNumber must beEqualTo(Some(validPhoneNumber))
    }

    "return true for isPhoneValid" in {
      validOrder.isPhoneValid must beTrue
    }

    "return true for isClientNameValid" in {
      validOrder.isClientNameValid must beTrue
    }

    "return true for isAdditionalDataValid" in {
      validOrder.isAdditionalDataValid must beTrue
    }

    "return " + validAdditionalFormData.getData + " for getAdditionalData" in {
      validOrder.getAdditionalData must beEqualTo(validAdditionalFormData)
    }

  }

  "invalid order" should {
    val invalidPhoneNumber = "invalidPhoneNumber"
    val invalidClientName = ""
    val invalidAdditionalFormData = "invalidFormData"
    val invalidOrder = Order(invalidPhoneNumber, invalidClientName, invalidAdditionalFormData)

    "return false for isValid" in {
      invalidOrder.isValid must beFalse
    }

    "return None for getPhoneNumber" in {
      invalidOrder.getPhoneNumber must beEqualTo(None)
    }

    "return false for isPhoneValid" in {
      invalidOrder.isPhoneValid must beFalse
    }

    "return false for isClientNameValid" in {
      invalidOrder.isClientNameValid must beFalse
    }

    "return false for isAdditionalDataValid" in {
      invalidOrder.isAdditionalDataValid must beFalse
    }

    "return " + OrderFormAdditionalData.None.getData + " for getAdditionalData" in {
      invalidOrder.getAdditionalData must beEqualTo(OrderFormAdditionalData.None)
    }

  }
}

class ServiceExportRequestSpec extends Specification {

  "exportAllServices request" should {

    val exportAllServicesRequest = ServiceExportRequest(exportAllServices = true)

    "return true for isValid" in {
      exportAllServicesRequest.isValid must beTrue
    }

    "return false for areServiceIdsPresent" in {
      exportAllServicesRequest.areServiceIdsPresent must beFalse
    }

    "return false for areQuantitiesPresent" in {
      exportAllServicesRequest.areQuantitiesPresent must beFalse
    }

  }

  "exportSelectedServicesWithoutQuantities request" should {

    val exportSelectedServicesWithoutQuantities =
      ServiceExportRequest(exportAllServices = false, serviceIds = List(1, 2, 3))

    "return true for isValid" in {
      exportSelectedServicesWithoutQuantities.isValid must beTrue
    }

    "return true for areServiceIdsPresent" in {
      exportSelectedServicesWithoutQuantities.areServiceIdsPresent must beTrue
    }

    "return false for areQuantitiesPresent" in {
      exportSelectedServicesWithoutQuantities.areQuantitiesPresent must beFalse
    }

  }
  
  "exportSelectedServicesWithQuantities request" should {

    val exportSelectedServicesWithQuantities =
      ServiceExportRequest(exportAllServices = false, serviceIds = List(1, 2, 3), quantities = List(2, 3, 4))

    "return true for isValid" in {
      exportSelectedServicesWithQuantities.isValid must beTrue
    }

    "return true for areServiceIdsPresent" in {
      exportSelectedServicesWithQuantities.areServiceIdsPresent must beTrue
    }

    "return true for areQuantitiesPresent" in {
      exportSelectedServicesWithQuantities.areQuantitiesPresent must beTrue
    }

  }

  "invalid request" should {

    for (invalidRequest <- List(
      ServiceExportRequest(exportAllServices = true, serviceIds = List(1, 2, 3), quantities = List(2, 3, 4)),
      ServiceExportRequest(exportAllServices = true, serviceIds = List(1, 2, 3), quantities = List(2, 3, 4)),
      ServiceExportRequest(exportAllServices = false, serviceIds = List(1, 2, 3), quantities = List(2, 3)),
      ServiceExportRequest(exportAllServices = false, quantities = List(2, 3))
    )){
      "return false for isValid" in {
        invalidRequest.isValid must beFalse
      }
    }

  }
}
