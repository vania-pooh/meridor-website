package ru.meridor.website

import org.fusesource.scalate.scaml.ScamlOptions
import ru.meridor.website.processing.{AutoSitemapSupport, LastModifiedSupport, AvailableServiceGroups}
import ru.meridor.diana.log.LoggingSupport
import ru.meridor.website.processing.RequestUtils._
import ru.meridor.website.processing.AvailableComplexServices._
import java.util.Date
import ru.meridor.diana.db.entities._
import ru.meridor.diana.db.entities.ServiceGroup
import com.redfin.sitemapgenerator.ChangeFreq
import org.joda.time.format.DateTimeFormat
import java.io.File
import scala.Some

/**
 * A servlet used to process HTML pages requests
 */
class PagesServlet extends WebsiteStack with LoggingSupport with LastModifiedSupport with AutoSitemapSupport {

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
    "/help" -> "/help",

    //Articles routes
    "/articles/electrical-tools" -> "/articles/electrical_tools",
    "/articles/wires-and-cables/classification" -> "/articles/wires_and_cables/classification",
    "/articles/wires-and-cables/marking" -> "/articles/wires_and_cables/marking",
    "/articles/wires-and-cables/connection" -> "/articles/wires_and_cables/connection",
    "/articles/no-dust" -> "/articles/no_dust",
    "/articles/ground-connection/classification" -> "/articles/ground_connection/classification",
    "/articles/lighting/classification" -> "/articles/lighting/classification"
  )){
    val route = staticRoute._1
    val viewName = staticRoute._2
    get(route, lastMod = "2014-03-04"){
	    processView(viewName)
    }
  }

  get("/contact", priority = 0.9, lastMod = "2014-03-04"){
    processView("/contact")
  }

  get("/bundles", priority = 0.8, lastMod = "2014-03-04"){
    processView("/bundles")
  }

  logger.info("Initializing dynamic pages routes...")
  get("/prices", priority = 0.9, lastMod = "2014-03-04"){
    processView("/prices", "servicesMap" -> loadServices(AvailableServiceGroups.*))
  }

  get("/services/electrical-works", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/electrical_works", "servicesMap" -> loadServices(AvailableServiceGroups.ElectricalWorks :: Nil))
  }

  get("/services/husband-for-an-hour", priority = 0.8, lastMod = "2013-11-03"){
    permanentRedirect("/services/call-electrician")
  }

  get("/services/call-electrician", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/call_electrician", "servicesMap" -> loadServices(AvailableServiceGroups.CallElectrician :: Nil))
  }

  get("/news", priority = 0.8, lastMod = "2014-03-04"){
    processView("/news", "newsList" -> News.get)
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
    get("/services/call-electrician/spb/" + district, lastMod = "2014-03-04"){
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
    get("/services/call-electrician/lo/" + district, lastMod = "2014-03-04"){
      processView("/services/call_electrician/district/lo/" + district, "servicesMap" -> loadCallElectricianDistrictServices)
    }
  }

  get("/services/technical-maintenance", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/technical_maintenance", "servicesMap" -> loadServices(AvailableServiceGroups.TechnicalMaintenance :: Nil))
  }

  get("/services/lighting", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/lighting", "servicesMap" -> loadServices(AvailableServiceGroups.Lighting :: Nil))
  }

  get("/services/electrical-appliances", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/electrical_appliances", "servicesMap" -> loadServices(AvailableServiceGroups.ElectricalAppliances :: Nil))
  }

  get("/services/electrical-appliances/electric-range", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/electrical_appliances/electric_range", "servicesMap" -> ComplexService.getByName(ElectricRange))
  }

  get("/services/electrical-appliances/washing-machine", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/electrical_appliances/washing_machine", "servicesMap" -> ComplexService.getByName(WashingMachine))
  }

  get("/services/electrical-appliances/dishwashing-machine", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/electrical_appliances/dishwashing_machine", "servicesMap" -> ComplexService.getByName(DishWashingMachine))
  }

  get("/services/electrical-appliances/oven", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/electrical_appliances/oven", "servicesMap" -> ComplexService.getByName(Oven))
  }

  get("/services/electrical-appliances/hob", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/electrical_appliances/hob", "servicesMap" -> ComplexService.getByName(Hob))
  }

  get("/services/electrical-appliances/microwave-oven", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/electrical_appliances/microwave_oven", "servicesMap" -> ComplexService.getByName(MicrowaveOven))
  }

  get("/services/telecommunication-technologies", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/telecommunication_technologies", "servicesMap" -> loadServices(AvailableServiceGroups.TelecommunicationTechnologies :: Nil))
  }

  get("/services/room-repair", priority = 0.8, lastMod = "2014-03-04"){
    processView("/services/room_repair", "servicesMap" -> loadServices(AvailableServiceGroups.RoomRepair :: Nil))
  }

  //We would like to avoid linking to itself in the sitemap.xml file. That's why we call super.get() instead of get().
  super.get("/sitemap.xml"){
    val file = new File(rootPath, "sitemap.xml")
    if (!file.exists()){
      logger.info("Generating sitemap.xml...")
      addSitemapUrl(url = absoluteUrlFromRelative("/doc/oferta.pdf"), lastMod = "2013-09-24", changeFreq = ChangeFreq.MONTHLY, priority = 0.6)
      addSitemapUrl(url = absoluteUrlFromRelative("/services/husband-for-an-hour"), lastMod = "2013-11-03", changeFreq = ChangeFreq.WEEKLY, priority = 0.8)
      generateSitemap(rootUrl, rootPath)
      logger.info("Saved generated sitemap.xml to " + file.toString + ".")
    }
    contentType = "text/xml"
    file
  }

  logger.info("Done initializing pages routes.")

  /**
   * An extended version of get() which adds URLs to sitemap
   * @param url should be relative
   * @param lastMod
   * @param changeFreq
   * @param priority
   * @param action
   * @return
   */
  protected def get(url: String, lastMod: String = DateTimeFormat.forPattern("yyyy-MM-dd").print(new Date().getTime), changeFreq: ChangeFreq = ChangeFreq.WEEKLY, priority: Double = 0.7)(action: => Any) = {
    super.get(url)(action)
    addSitemapUrl(url, lastMod, changeFreq, priority)
  }

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
