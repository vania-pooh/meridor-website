package ru.meridor.website.export.reader

import ru.meridor.website.export.{Data, Reader}
import ru.meridor.website.db.entities.{ServiceGroup, ServicesData, Service}

/**
 * Reads a list of by a list of (ID, quantity) pairs
 */
case class ServicesListReader(quantities: Map[Long, Double]) extends Reader[ServicesList] {

  def this(serviceIds: List[Long]) = this(serviceIds.map(id => id -> 0d).toMap)

  def read = {
    val serviceIds: List[Long] = if (quantities.size > 0) (quantities map (q => q._1)).toList else List()
    val servicesData = if (serviceIds.size > 0)
      ServicesData.fromList(Service.getByIds(serviceIds))
      else Service.getByGroups(ServiceGroup.topGroups map (_.name))
    ServicesList(servicesData, quantities)
  }

}

object ServicesListReader {

  def apply(serviceIds: List[Long]) = new ServicesListReader(serviceIds)

}

/**
 * A tree of services and their potential mapping to quantities from the client side
 * @param servicesData a tree of services with their groups
 * @param quantities a map from serviceId to quantity
 */
case class ServicesList(servicesData: ServicesData, quantities: Map[Long, Double]) extends Data {

  def this(services: ServicesData) = this(services, Map[Long, Double]())

  def length = servicesData.size

  def quantity(service: Service) = quantities.get(service.id) match {
    case Some(q) => q
    case None => 0d
  }

}

object ServicesList {

  def apply(servicesData: ServicesData) = new ServicesList(servicesData)

}