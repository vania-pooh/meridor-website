package ru.meridor.website

import org.fusesource.scalate.scaml.ScamlOptions
import ru.meridor.diana.db.entities.{Service, ServiceGroup}
import ru.meridor.website.processing.AvailableServiceGroups
import ru.meridor.diana.log.LoggingSupport

/**
 * A servlet used to process HTML pages requests
 */
class PagesServlet extends WebsiteStack with LoggingSupport {

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
    ("/" -> "/index"),
    ("/bundles" -> "/bundles"),
    ("/contact" -> "/contact"),

    //Articles routes
    ("/articles/electrical-tools" -> "/articles/electrical_tools")
//    ("/articles/wires-and-cables" -> "/articles/wires_and_cables"),
//    ("/articles/apartment-wiring" -> "/articles/apartment_wiring"),
//    ("/articles/safe-electricity" -> "/articles/safe_electricity"),
//    ("/articles/no-dust" -> "/articles/no_dust"),
//    ("/articles/new-building-wiring" -> "/articles/new_building_wiring"),
//    ("/articles/cottage-wiring" -> "/articles/cottage_wiring"),
//    ("/articles/ground-connection" -> "/articles/ground_connection"),
//    ("/articles/standards" -> "/articles/standards")
  )){
    val route = staticRoute._1
    val viewName = staticRoute._2
    get(route){
	    renderView(viewName)
    }
  }

  logger.info("Initializing dynamic pages routes...")
  get("/prices"){
    renderView("/prices", ("servicesMap" -> loadServices(AvailableServiceGroups.*)))
  }

  get("/services/electrical-works"){
    renderView("/services/electrical_works", ("servicesMap" -> loadServices(List[String](AvailableServiceGroups.ElectricalWorks))))
  }

  get("/services/husband-for-an-hour"){
    renderView("/services/husband_for_an_hour", ("servicesMap" -> loadServices(List[String](AvailableServiceGroups.HusbandForAnHour))))
  }

  get("/services/technical-maintenance"){
    renderView("/services/technical_maintenance", ("servicesMap" -> loadServices(List[String](AvailableServiceGroups.TechnicalMaintenance))))
  }

  get("/services/lighting"){
    renderView("/services/lighting", ("servicesMap" -> loadServices(List[String](AvailableServiceGroups.Lighting))))
  }

  get("/services/electrical-appliances"){
    renderView("/services/electrical_appliances", ("servicesMap" -> loadServices(List[String](AvailableServiceGroups.ElectricalAppliances))))
  }

  get("/services/telecommunication-technologies"){
    renderView("/services/telecommunication_technologies", ("servicesMap" -> loadServices(List[String](AvailableServiceGroups.TelecommunicationTechnologies))))
  }
  logger.info("Done initializing routes.")


  /**
   * Renders view with the specified name and set of attributes to be passed to this view
   */
  private def renderView(viewName: String, attributes: (String, Any)*): String = {
    logger.info("Rendering view \"" + viewName + "\"...")
    contentType = "text/html"
    jade(viewName, attributes:_*)
  }

  private def loadServices(groups: List[String]): Map[ServiceGroup, List[Service]] = Service.getByGroups(groups)

}
