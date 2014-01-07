package ru.meridor.website

import org.fusesource.scalate.scaml.ScamlOptions
import ru.meridor.diana.db.entities.Service
import ru.meridor.website.processing.{LastModifiedSupport, AvailableServiceGroups}
import ru.meridor.diana.log.LoggingSupport
import ru.meridor.website.processing.RequestUtils._
import java.util.Date
import ru.meridor.diana.db.entities.ServiceGroup
import ru.meridor.diana.db.entities.ServiceGroupContents
import scala.Some

/**
 * A servlet used to process HTML pages requests
 */
class PagesServlet extends WebsiteStack with LoggingSupport with LastModifiedSupport {

  /**
   * HTML minification settings
   */
  ScamlOptions.indent = ""
  ScamlOptions.nl = ""

  logger.info("Starting application...")

  /**
   * Initializing routes...
   */
  logger.info("Initializing static pages routes...")
  for (staticRoute <- Array(
    //Core routes
    "/" -> "/index",
    "/bundles" -> "/bundles",
    "/contact" -> "/contact",

    //Articles routes
    "/articles/electrical-tools" -> "/articles/electrical_tools",
    "/articles/wires-and-cables/classification" -> "/articles/wires_and_cables/classification",
    "/articles/wires-and-cables/marking" -> "/articles/wires_and_cables/marking",
    "/articles/wires-and-cables/connection" -> "/articles/wires_and_cables/connection",
//    ("/articles/apartment-wiring" -> "/articles/apartment_wiring"),
//    ("/articles/safe-electricity" -> "/articles/safe_electricity"),
    "/articles/no-dust" -> "/articles/no_dust",
//    ("/articles/new-building-wiring" -> "/articles/new_building_wiring"),
//    ("/articles/cottage-wiring" -> "/articles/cottage_wiring"),
    "/articles/ground-connection/classification" -> "/articles/ground_connection/classification",
    "/articles/lighting/classification" -> "/articles/lighting/classification"
//    ("/articles/standards" -> "/articles/standards")
  )){
    val route = staticRoute._1
    val viewName = staticRoute._2
    get(route){
	    processView(viewName)
    }
  }

  logger.info("Initializing dynamic pages routes...")
  get("/prices"){
    processView("/prices", "servicesMap" -> loadServices(AvailableServiceGroups.*))
  }

  get("/services/electrical-works"){
    processView("/services/electrical_works", "servicesMap" -> loadServices(AvailableServiceGroups.ElectricalWorks :: Nil))
  }

  get("/services/husband-for-an-hour"){
    permanentRedirect("/services/call-electrician")
  }

  get("/services/call-electrician"){
    processView("/services/call_electrician", "servicesMap" -> loadServices(AvailableServiceGroups.CallElectrician :: Nil))
  }

  //St-Petersburg
  for (district <- List(
    "admiralteyskiy",
    "vasileostrovskiy",
    "viborgskiy",
    "kalininskiy",
    "kirovskiy",
    "kolpinskiy",
    "krasnogvardeyskiy",
    "krasnoselskiy",
    "kronshtadtskiy",
    "kurortniy",
    "moskovskiy",
    "nevskiy",
    "petrogradskiy",
    "petrodvortsoviy",
    "primorskiy",
    "pushkinskiy",
    "frunzenskiy",
    "centralniy"
  )){
    get("/services/call-electrician/spb/" + district){
      processView("/services/call_electrician/district/spb/" + district, "servicesMap" -> loadCallElectricianDistrictServices)
    }
  }

  //Leningrad Oblast
  for (district <- List(
    "vsevologskiy",
    "gatchinskiy",
    "kirovskiy",
    "tosnenskiy"
  )){
    get("/services/call-electrician/lo/" + district){
      processView("/services/call_electrician/district/lo/" + district, "servicesMap" -> loadCallElectricianDistrictServices)
    }
  }

  get("/services/technical-maintenance"){
    processView("/services/technical_maintenance", "servicesMap" -> loadServices(AvailableServiceGroups.TechnicalMaintenance :: Nil))
  }

  get("/services/lighting"){
    processView("/services/lighting", "servicesMap" -> loadServices(AvailableServiceGroups.Lighting :: Nil))
  }

  get("/services/electrical-appliances"){
    processView("/services/electrical_appliances", "servicesMap" -> loadServices(AvailableServiceGroups.ElectricalAppliances :: Nil))
  }

  get("/services/telecommunication-technologies"){
    processView("/services/telecommunication_technologies", "servicesMap" -> loadServices(AvailableServiceGroups.TelecommunicationTechnologies :: Nil))
  }

  get("/services/room-repair"){
    processView("/services/room_repair", "servicesMap" -> loadServices(AvailableServiceGroups.RoomRepair :: Nil))
  }

  logger.info("Done initializing routes.")


  /**
   * Processes view with the specified name and set of attributes to be passed to this view.
   * Processing includes sending Last-Modified header and handling If-Modified-Since header
   */
  private def processView(viewName: String, attributes: (String, Any)*): String = {
    ifModifiedSinceDate match {
      case Some(date) => {
        val lastModificationDate: Date = new Date(lastModificationTimestamp(viewName, attributes:_*))
        if (date after lastModificationDate)
          notModified
          else renderViewWithLastModifiedHeader(viewName, attributes:_*)
      }
      case None => renderViewWithLastModifiedHeader(viewName, attributes:_*)
    }
  }

  private def renderViewWithLastModifiedHeader(viewName: String, attributes: (String, Any)*): String = {
    response.setHeader("Last-Modified", dateToHeaderString(
      new Date(lastModificationTimestamp(viewName, attributes:_*)))
    )
    renderView(viewName, attributes:_*)
  }

  /**
   * Renders view with the specified name and set of attributes to be passed to this view
   */
  private def renderView(viewName: String, attributes: (String, Any)*): String = {
    logger.info("Rendering view \"" + viewName + "\"...")
    contentType = "text/html"
    jade(viewName, attributes:_*)
  }

  type ServicesData = Map[ServiceGroup, ServiceGroupContents]

  private def loadServices(topGroups: List[String], randomize: Boolean = false, limit: Int = 10): ServicesData = {
    val groupNames = ServiceGroup.topGroups filter (tg => topGroups.contains(tg.name)) map (g => g.name)
    if (randomize)
      Service.getRandom(groupNames, limit)
    else
      Service.getByGroups(groupNames)
  }

  private def loadCallElectricianDistrictServices: ServicesData =
    Service.merge(
      AvailableServiceGroups.CallElectrician,
      loadServices(AvailableServiceGroups.CallElectrician :: Nil),
      loadServices(AvailableServiceGroups.ElectricalWorks :: Nil, randomize = true)
    )
}
