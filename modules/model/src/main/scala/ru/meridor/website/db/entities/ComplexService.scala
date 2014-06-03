package ru.meridor.website.db.entities

import ru.meridor.website.db.DB
import scala.slick.driver.PostgresDriver.simple._
import ru.meridor.website.db.tables.Tables

case class ComplexService(id: Long, name: String, displayName: String){

  def toServiceGroup = new ServiceGroup(0, name, displayName, 1, None)

}

/**
 * Allows to fetch complex service contents as ServicesData object
 */
object ComplexService {

  def getByName(name: String): ServicesData = {
    DB withSession { implicit session =>
      val rawRecords = for {
        (csc, sq) <- Tables.ComplexServiceContents leftJoin Tables.ServiceQuantities on (
          (c, s) => (c.serviceId === s.serviceId) && (c.complexServiceId === s.complexServiceId)
        )
        cs <- Tables.ComplexServices if (cs.serviceId === csc.complexServiceId) && (cs.serviceName === name)
        st <- Tables.ComplexServiceStages if st.stageId === csc.stageId
        s <- Tables.Services if s.serviceId === csc.serviceId
      } yield (
            //Sequence columns should be always on the first two places for correct sorting!
            st.sequence, csc.sequence,
            s.serviceId, st.stageId,
            cs.serviceId, cs.serviceName, cs.displayName,
            sq.quantity.?
          )
      val records = rawRecords.sortBy(_._1).sortBy(_._2).list
      if (records.size > 0){
        val firstRecord = records(0)
        val complexService = ComplexService(firstRecord._4, firstRecord._6, firstRecord._7)
        val map = scala.collection.mutable.LinkedHashMap[ServiceGroup, ServiceGroupContents]()
        val serviceIds = records map (r => r._3)
        val quantities = scala.collection.mutable.Map[Long, Float]()
        records foreach {
          r => r._8 match {
            case Some(quantity) => quantities += r._3 -> quantity.asInstanceOf[Float]
            case None => ()
          }
        }
        val services = Service.getByIds(serviceIds, quantities.toMap)
        val stageIds = records map (r => r._4)
        stageIds foreach {
          stageId => {
            val stage = ComplexServiceStage.getById(stageId)
            val stageServiceIds = records.filter(_._4 == stageId).map(_._3)
            val stageServices = services.filter(s => stageServiceIds.contains(s.id))
            map += stage.toServiceGroup -> new ServiceGroupContents(ServicesData.empty, stageServices)
          }
        }
        Map[ServiceGroup, ServiceGroupContents]() + (complexService.toServiceGroup -> new ServiceGroupContents(map.toMap, List()))
      }
      else ServicesData.empty
    }
  }

}

case class ComplexServiceStage(id: Int, name: String, displayName: String, sequence: Int){

  override def hashCode() = toString.hashCode

  override def toString = "ComplexServiceStage(" + id + ", " + name + ", " + displayName + ")"

  def toServiceGroup = new ServiceGroup(id, name, displayName, sequence, None)

}

object ComplexServiceStage {

  private lazy val searchHashTable = load

  private def load: Map[Int, ComplexServiceStage] = {
    DB withSession { implicit session =>
      val complexServiceStages = scala.collection.mutable.HashMap[Int, ComplexServiceStage]()
      Tables.ComplexServiceStages.buildColl[List] foreach {
        record: Tables.ComplexServiceStagesRow =>
          record.stageId -> new ComplexServiceStage(record.stageId, record.stageName, record.displayName, record.sequence)
      }
      complexServiceStages.toMap[Int, ComplexServiceStage]
    }
  }

  def getById(id: Int): ComplexServiceStage = searchHashTable(id)

}