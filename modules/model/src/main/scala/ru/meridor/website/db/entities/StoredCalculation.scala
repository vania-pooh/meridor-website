package ru.meridor.website.db.entities

import ru.meridor.website.db.DB
import scala.slick.driver.PostgresDriver.simple._
import ru.meridor.website.db.tables.Tables

object StoredCalculation {

  def getById(id: Long): Option[Tables.StoredCalculationsRow] = {
    DB withSession { implicit session =>
      val storedCalculations = Tables.StoredCalculations.filter(_.storedCalculationId === id).buildColl[List]
      if (storedCalculations.size > 0)
        Some(storedCalculations(0))
      else None
    }
  }

  def insert(displayName: String, data: String): Long = {
    DB withSession { implicit session =>
      ( Tables.StoredCalculations returning Tables.StoredCalculations.map(_.storedCalculationId) ) += Tables.StoredCalculationsRow(0, displayName, data)
    }
  }

}
