package ru.meridor.website.db.entities

import ru.meridor.website.db.DB
import scala.slick.driver.PostgresDriver.simple._
import ru.meridor.website.db.tables.Tables
import scala.collection.mutable

/**
 * Encapsulates service group
 */
case class ServiceGroup(id: Int, name: String, displayName: String, sequence: Int, parentGroup: Option[ServiceGroup]) extends Ordered[ServiceGroup]{

  def getChildGroups = ServiceGroup.getByParentGroup(Some(this))

  def compare(that: ServiceGroup): Int = displayName.compareTo(that.displayName)

  override def hashCode() = toString.hashCode()

  override def equals(obj: scala.Any) = obj.isInstanceOf[ServiceGroup] && obj.asInstanceOf[ServiceGroup].id.equals(id)

  override def toString = "ServiceGroup(" + id + ", " + displayName + ")"
}

object ServiceGroup {

  private val groupIdHashTable = scala.collection.mutable.LinkedHashMap[Int, ServiceGroup]()
  private val groupNameHashTable = scala.collection.mutable.LinkedHashMap[String, ServiceGroup]()
  private val parentGroupHashTable = scala.collection.mutable.LinkedHashMap[Option[ServiceGroup], scala.collection.mutable.LinkedList[ServiceGroup]]()

  val topGroups = loadServiceGroups

  private def initGroup(recordsMap: Map[Int, Tables.ServiceGroupsRow], groupId: Int): Option[ServiceGroup] = {
    val record = recordsMap(groupId)
    val id = record.groupId
    val name = record.groupName
    val displayName = record.displayName
    val sequence = record.sequence
    val parentGroupId = record.parentGroupId
    val parentServiceGroup: Option[ServiceGroup] = parentGroupId match {
      case Some(gid) => {
        groupIdHashTable get gid match {
          case Some(psg) => Some(psg)
          case None => initGroup(recordsMap, gid)
        }
      }
      case None => None
    }
    val serviceGroup = new ServiceGroup(id, name, displayName, sequence, parentServiceGroup)
    groupIdHashTable += (id -> serviceGroup)
    groupNameHashTable += (name -> serviceGroup)
    parentGroupHashTable get parentServiceGroup match {
      case None => parentGroupHashTable put(parentServiceGroup, scala.collection.mutable.LinkedList[ServiceGroup](serviceGroup))
      case _ => parentGroupHashTable(parentServiceGroup) :+= serviceGroup
    }
    serviceGroup.parentGroup match {
      case None => Some(serviceGroup) //We return only top level groups
      case _ => None
    }
  }

  private def loadServiceGroups: List[ServiceGroup] = {
    DB withSession { implicit session =>
      val serviceGroups = Tables.ServiceGroups.sortBy(_.parentGroupId.nullsFirst).buildColl[List]
      val serviceGroupsMap = (serviceGroups map (sg => sg.groupId -> sg)).toMap
      (serviceGroups map (sg => initGroup(serviceGroupsMap, sg.groupId))).flatten
    }
  }

  def getById(id: Int): ServiceGroup = groupIdHashTable(id)

  def getByName(name: String): Option[ServiceGroup] = groupNameHashTable get name
  
  def getByParentGroup(parentGroup: Option[ServiceGroup]) =
    if (parentGroupHashTable.contains(parentGroup))
      parentGroupHashTable(parentGroup).toList
      else List[ServiceGroup]()
}

/**
 * Encapsulates a single unit of measure, like meters, barrels, inches, etc.
 */
case class UnitOfMeasure(id: Int, displayName: String){

  override def hashCode() = toString.hashCode

  override def toString = "UnitOfMeasure(" + id + ", " + displayName + ")"

}

object UnitOfMeasure {

  private lazy val searchHashTable = loadUnitsOfMeasure

  private def loadUnitsOfMeasure: Map[Int, UnitOfMeasure] = {
    DB withSession { implicit session =>
      val unitsOfMeasure = Tables.Units.buildColl[List]
      (unitsOfMeasure map (uom => uom.unitId -> new UnitOfMeasure(uom.unitId, uom.displayName))).toMap[Int, UnitOfMeasure]
    }
  }

  def getById(id: Int): UnitOfMeasure = searchHashTable(id)

}

/**
 * A single service (i.e. named action with unit of measure and price)
 */
case class Service(id: Long, displayName: String, price: Double, unitOfMeasure: UnitOfMeasure, group: ServiceGroup, quantity: Float = 0f){

  def this(data: Tables.ServicesRow, quantity: Float = 0f) =
    this(
      data.serviceId,
      data.serviceName,
      data.price,
      UnitOfMeasure.getById(data.unitId),
      ServiceGroup.getById(data.groupId),
      quantity
    )
}

case class ServiceGroupContents(childGroupsData: ServicesData, services: List[Service]) extends Iterable[Service]{

  private lazy val list = toList

  def ++(serviceGroupContents: ServiceGroupContents) = new ServiceGroupContents(
    childGroupsData ++ serviceGroupContents.childGroupsData,
    services ::: serviceGroupContents.services ::: Nil
  )

  def shuffle: ServiceGroupContents = {
    val shuffledChildGroupsData = childGroupsData map { entry => entry._1 -> entry._2.shuffle }
    val shuffledServices = scala.util.Random.shuffle(services)
    new ServiceGroupContents(shuffledChildGroupsData, shuffledServices)
  }

  /**
   * Takes n elements from services section
   * @param n
   * @return
   */
  override def take(n: Int) = new ServiceGroupContents(ServicesData.empty, services.take(n))

  /**
   * Flattens services from all child groups to the top level
   * @return
   */
  def flatten: ServiceGroupContents = new ServiceGroupContents(
    ServicesData.empty,
    (childGroupsData flatMap { entry => entry._2.flatten.services}).toList ::: services ::: Nil
  )

  def iterator = list.iterator

  override def toList: List[Service] = {
    val ret = scala.collection.mutable.ArrayBuffer[Service]()
    childGroupsData foreach {
      el => ret ++= el._2.toList
    }
    ret ++= services
    ret.toList
  }

  override def toString() = "ServiceGroupContents(" + childGroupsData + ", " + services + ")"
}

object ServiceGroupContents {

  def empty = new ServiceGroupContents(ServicesData.empty, List[Service]())

}

object Service{
  
  def getById(id: Long): Option[Service] = {
    val services = getByIds(List(id))
    if (services.length > 0)
      Some(services(0))
      else None
  }

  def getByIds(ids: List[Long], quantities: Map[Long, Float] = Map()): List[Service] = {
    DB withSession { implicit session =>
      val services = Tables.Services.filter(_.serviceId.inSetBind(ids)).buildColl[List]
      if (services.size > 0){
        val sortedRecords = scala.util.Sorting.stableSort(
          services,
          {sr: Tables.ServicesRow => ids.indexOf(sr.serviceId)}
        )
        servicesWithQuantities(sortedRecords, quantities)
      } else List[Service]()
    }
  }

  def getByGroups(groupNames: List[String], quantities: Map[Long, Float] = Map()): ServicesData = {
    import scala.collection.immutable.TreeMap
    if (groupNames.size > 0){
      DB withSession { implicit session =>
        val servicesRecords = (for {
          s <- Tables.Services
          g <- s.serviceGroupsFk if g.groupName inSetBind groupNames
        } yield s).sortBy(_.serviceName.asc).buildColl[List]
        val services = if (servicesRecords.size > 0)
          servicesWithQuantities(servicesRecords, quantities)
          else List[Service]()
        val map = scala.collection.mutable.Map[ServiceGroup, ServiceGroupContents]()
        val groups = (groupNames map(g => ServiceGroup.getByName(g))).flatten
        for (group <- groups){
          val groupServices = services filter (_.group == group)
          val childGroupNames = group.getChildGroups map (_.name)
          map += (group -> ServiceGroupContents(getByGroups(childGroupNames), groupServices))
        }
        TreeMap[ServiceGroup, ServiceGroupContents](map.toSeq:_*)
      }
    } else ServicesData.empty
  }

  private def servicesWithQuantities(records: Seq[Tables.ServicesRow], quantities: Map[Long, Float]): List[Service] = (records map {
    r => {
      val serviceId = r.serviceId
      quantities.get(serviceId) match {
        case Some(quantity) => new Service(r, quantity)
        case None => new Service(r)
      }
    }
  }).toList

  /**
   * Returns randomized services data taking no more than limit entries per group
   * @param groupNames
   * @param limit
   * @return
   */
  def getRandom(groupNames: List[String], limit: Int): ServicesData =
    if (limit > 0) {
      val randomizedServices: Map[ServiceGroup, ServiceGroupContents] =
        getByGroups(groupNames) map {
          entry => entry._1 -> entry._2.flatten.shuffle
        }
      randomizedServices map (entry => entry._1 -> entry._2.take(limit))
    } else ServicesData.empty

  def getByGroup(groupName: String): ServiceGroupContents = {
    val map: Map[ServiceGroup, ServiceGroupContents] = getByGroups(List(groupName))
    if (map.size > 0) map(map.head._1) else ServiceGroupContents.empty
  }

  /**
   * Merges all input services data to the single services data with service group name = outputServiceGroupName
   * @param outputServiceGroupName
   * @param servicesData
   * @return
   */
  def merge(outputServiceGroupName: String, servicesData: ServicesData*): ServicesData = ServiceGroup.getByName(outputServiceGroupName) match {
    case Some(serviceGroup) => {
      val allServiceGroupContents: List[ServiceGroupContents] = List(servicesData:_*) flatMap (sd => sd map (_._2))
      val mergedServiceGroupContents: ServiceGroupContents = (allServiceGroupContents.tail foldLeft allServiceGroupContents.head)(_ ++ _)
      Map(serviceGroup -> mergedServiceGroupContents)
    }
    case None => ServicesData.empty
  }

}

object ServicesData {

  def empty = Map[ServiceGroup, ServiceGroupContents]()

  def fromList(services: List[Service], parentGroups: List[ServiceGroup] = ServiceGroup.topGroups): ServicesData = {
    //Group service by their service group
    val servicesByGroup = mutable.HashMap[ServiceGroup, mutable.ArrayBuffer[Service]]()
    services foreach {
      service => {
        val group = service.group
        if (servicesByGroup.contains(group))
          servicesByGroup(group) += service
          else servicesByGroup += group -> mutable.ArrayBuffer(service)
      }
    }

    //Build recursive structure
    val ret = mutable.HashMap[ServiceGroup, ServiceGroupContents]()
    parentGroups foreach {
      gr => {
        val ownServices = if (servicesByGroup.contains(gr)) servicesByGroup(gr).toList else List[Service]()
        val childGroups = gr.getChildGroups
        val childServices = mutable.ArrayBuffer[Service]()
        childGroups foreach {
          cg => if (servicesByGroup.contains(cg)){
            childServices ++= servicesByGroup(cg)
          }
        }

        val childGroupContents = if ( (childGroups.length > 0) && (childServices.length > 0) )
          fromList(childServices.toList, childGroups)
          else empty
        if ( (childGroupContents.size > 0) || (ownServices.length > 0) ){
          ret += gr -> ServiceGroupContents(childGroupContents, ownServices)
        }
      }
    }
    ret.toMap

  }

}