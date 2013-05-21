package ru.meridor.website

import org.scalatra._
import scalate.ScalateSupport
import org.slf4j.LoggerFactory
import ru.meridor.website.db.SlickSupport

class WebsiteServlet extends WebsiteStack with SlickSupport {

  private val logger = LoggerFactory.getLogger(this.getClass)
  
  logger.info("Starting application...")
  
  /**
   * Initializing routes...
   */
  logger.info("Initializing static routes...")
  for (staticRoute <- Array(
    //Core routes
    ("/" -> "/index"),
    ("/services" -> "/services"),
    ("/bundles" -> "/bundles"),
    ("/prices" -> "/prices"),
    ("/contact" -> "/contact"),
    ("/articles" -> "/articles"),

    //Services routes
    ("/services/electrical-works" -> "/services/electrical_works"),
    ("/services/husband-for-an-hour" -> "/services/husband_for_an_hour"),
    ("/services/technical-maintenance" -> "/services/technical_maintenance"),
    ("/services/lighting" -> "/services/lighting"),
    ("/services/electrical-appliances" -> "/services/electrical_appliances"),
    ("/services/building-equipment" -> "/services/building_equipment"),
    ("/services/telecommunication-technologies" -> "/services/telecommunication_technologies"),
    ("/services/it" -> "/services/it"),

    //Articles routes
    ("/articles/electrical-tools" -> "/articles/electrical_tools"),
    ("/articles/wires-and-cables" -> "/articles/wires_and_cables"),
    ("/articles/apartment-wiring" -> "/articles/apartment_wiring"),
    ("/articles/safe-electricity" -> "/articles/safe_electricity"),
    ("/articles/no-dust" -> "/articles/no_dust"),
    ("/articles/new-building-wiring" -> "/articles/new_building_wiring"),
    ("/articles/cottage-wiring" -> "/articles/cottage_wiring"),
    ("/articles/ground-connection" -> "/articles/ground_connection"),
    ("/articles/standards" -> "/articles/standards")
  )){
    val route = staticRoute._1
    val viewName = staticRoute._2
    logger.info("Assigning view \"" + viewName + "\" to route \"" + route + "\"...")
    get(route){
	    renderView(viewName)
    }
  }
  
  /**
   * Renders view with the specified name and set of attributes to be passed to this view
   */
  private def renderView(viewName: String, attributes: (String, Any)*): String = {
    logger.info("Rendering view \"" + viewName + "\"...")
    contentType = "text/html"
    jade(viewName, attributes:_*)
  }

}
