package ru.meridor.website.processing

/**
 * Stores constants with possible additional data
 */

class OrderFormAdditionalData(data: String) extends Enumeration{
  def getData = data
  override def toString(): String = getData
}

object OrderFormAdditionalData {

  private val availableValues = scala.collection.mutable.Map[String, OrderFormAdditionalData]()

  val None = define("none")
  val Index = define("index")
  val ElectricalWorks = define("electrical_works")
  val CallElectrician = define("call_electrician")
  val RoomRepair = define("room_repair")
  val TechnicalMaintenance = define("technical_maintenance")

  val Lighting = define("lighting")
  val LightingSystem = define("lighting_system")

  val ElectricalAppliances = define("electrical_appliances")
  val ElectricRange = define("electric_range")
  val WashingMachine = define("washing_machine")
  val DishWashingMachine = define("dishwashing_machine")
  val Oven = define("oven")
  val Hob = define("hob")
  val MicrowaveOven = define("microwave_oven")

  val Telecommunication = define("telecommunication")
  val Prices = define("prices")
  val ElectricalBundle = define("electrical_bundle")
  val ComputerBundle = define("computer_bundle")
  val LightingBundle = define("lighting_bundle")
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
