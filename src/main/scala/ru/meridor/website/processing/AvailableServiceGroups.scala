package ru.meridor.website.processing

/**
 * Stores a list of available service group names
 */
object AvailableServiceGroups {

  private var availableGroups = List[String]()

  val ElectricalWorks = defineGroup("electrical_works")
  val HusbandForAnHour = defineGroup("husband_for_an_hour")
  val TechnicalMaintenance = defineGroup("technical_maintenance")
  val Lighting = defineGroup("lighting")
  val ElectricalAppliances = defineGroup("electrical_appliances")
  val TelecommunicationTechnologies = defineGroup("telecommunication_technologies")

  private def defineGroup(groupName: String): String = {
    if (!availableGroups.contains(groupName)){
      availableGroups :+= groupName
    }
    groupName
  }

  def * = availableGroups

  def headerClass(serviceGroupName: String): String = serviceGroupName match {
    case ElectricalWorks => "sprite-_07"
    case HusbandForAnHour => "sprite-_15"
    case TechnicalMaintenance => "sprite-_19"
    case Lighting => "sprite-_07"
    case ElectricalAppliances => "sprite-_07"
    case TelecommunicationTechnologies => "sprite-_07"
    case _ => ""
  }
}
