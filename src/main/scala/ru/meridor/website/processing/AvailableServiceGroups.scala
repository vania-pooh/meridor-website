package ru.meridor.website.processing

/**
 * Stores a list of available service group names
 */
object AvailableServiceGroups {

  private var availableGroups = List[String]()

  val ElectricalWorks = defineGroup("electrical_works")
  val CallElectrician = defineGroup("call_electrician")
  val TechnicalMaintenance = defineGroup("technical_maintenance")
  val Lighting = defineGroup("lighting")
  val ElectricalAppliances = defineGroup("electrical_appliances")
  val TelecommunicationTechnologies = defineGroup("telecommunication_technologies")
  val RoomRepair = defineGroup("room_repair")

  private def defineGroup(groupName: String): String = {
    if (!availableGroups.contains(groupName)){
      availableGroups :+= groupName
    }
    groupName
  }

  def * = availableGroups

  def headerClass(serviceGroupName: String): String = serviceGroupName match {
    case ElectricalWorks => "electrical_works"
    case CallElectrician => "call_electrician"
    case TechnicalMaintenance => "technical_maintenance"
    case Lighting => "lighting"
    case ElectricalAppliances => "electrical_appliances"
    case TelecommunicationTechnologies => "telecommunication_technologies"
    case RoomRepair => "room_repair"
    case _ => ""
  }
}
