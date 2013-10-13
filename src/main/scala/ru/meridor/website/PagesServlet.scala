package ru.meridor.website

import org.fusesource.scalate.scaml.ScamlOptions
import ru.meridor.diana.db.entities.Service
import ru.meridor.website.processing.{LastModifiedSupport, AvailableServiceGroups}
import ru.meridor.diana.log.LoggingSupport
import ru.meridor.website.processing.RequestUtils._
import java.util.Date
import ru.meridor.diana.db.entities.ServiceGroup
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
    "/articles/electrical-tools" -> "/articles/electrical_tools"
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
	    processView(viewName)
    }
  }

  logger.info("Initializing dynamic pages routes...")
  get("/prices"){
    processView("/prices", "servicesMap" -> loadServices(AvailableServiceGroups.*))
  }

  get("/services/electrical-works"){
    processView("/services/electrical_works", "servicesMap" -> loadServices(List[String](AvailableServiceGroups.ElectricalWorks)))
  }

  get("/services/husband-for-an-hour"){
    permanentRedirect("/services/call-electrician")
  }

  get("/services/call-electrician"){
    processView("/services/call_electrician", "servicesMap" -> loadServices(List[String](AvailableServiceGroups.CallElectrician)))
  }

  get("/services/technical-maintenance"){
    processView("/services/technical_maintenance", "servicesMap" -> loadServices(List[String](AvailableServiceGroups.TechnicalMaintenance)))
  }

  get("/services/lighting"){
    processView("/services/lighting", "servicesMap" -> loadServices(List[String](AvailableServiceGroups.Lighting)))
  }

  get("/services/electrical-appliances"){
    processView("/services/electrical_appliances", "servicesMap" -> loadServices(List[String](AvailableServiceGroups.ElectricalAppliances)))
  }

  get("/services/telecommunication-technologies"){
    processView("/services/telecommunication_technologies", "servicesMap" -> loadServices(List[String](AvailableServiceGroups.TelecommunicationTechnologies)))
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

  private def loadServices(groups: List[String]): Map[ServiceGroup, List[Service]] = Service.getByGroups(groups)

}
