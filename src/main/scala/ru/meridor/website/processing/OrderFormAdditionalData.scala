package ru.meridor.website.processing

/**
 * Stores constants with possible additional data
 */

class OrderFormAdditionalData(data: String) extends Enumeration{
  def getData = data
  override def toString: String = getData
}

object OrderFormAdditionalData {

  private val availableValues = scala.collection.mutable.Map[String, OrderFormAdditionalData]()

  val None = define("none")
  val ElectricalWorks = define("electrical_works")
  val HusbandForAnHour = define("husband_for_an_hour")
  val TechnicalMaintenance = define("technical_maintenance")
  val Lighting = define("lighting")
  val ElectricalAppliances = define("electrical_appliances")
  val Telecommunication = define("telecommunication")
  val Prices = define("prices")
  val ElectricalBundle = define("electrical_bundle")
  val ComputerBundle = define("computer_bundle")
  val LightingBundle = define("lighting_bundle")
  val HusbandForAnHourBundle = define("husband_for_an_hour_bundle")
  val Contact = define("contact")

  def getAvailableValues = availableValues.toMap

  def isDefined(data: String) = getAvailableValues.contains(data)

  def get(data: String) = getAvailableValues.getOrElse(data, None)

  private def define(data: String) = {
    val dt = new OrderFormAdditionalData(data)
    availableValues += (data -> dt)
    dt
  }

}