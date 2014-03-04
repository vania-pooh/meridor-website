package ru.meridor.website.processing

import scala.language.implicitConversions

object AvailableComplexServices {
  val ElectricRange = AvailableComplexService("electric_range")
  val WashingMachine = AvailableComplexService("washing_machine")
  val DishWashingMachine = AvailableComplexService("dishwashing_machine")
  val Oven = AvailableComplexService("oven")
  val Hob = AvailableComplexService("hob")
  val MicrowaveOven = AvailableComplexService("microwave_oven")

  implicit def availableComplexServiceToString(acs: AvailableComplexService): String = acs.name
}

case class AvailableComplexService(name: String)
