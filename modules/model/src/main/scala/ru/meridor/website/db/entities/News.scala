package ru.meridor.website.db.entities

import ru.meridor.website.db.DB
import scala.slick.driver.PostgresDriver.simple._
import ru.meridor.website.db.tables.Tables

object News {

  /**
   * Returns all or some last news in the table
   * @return
   */
  def get(limit: Option[Int] = None): List[Tables.NewsRow] = {
    DB withSession { implicit session =>
      limit match {
        case Some(l) => Tables.News.sortBy(_.date.desc).take(l).buildColl[List]
        case None => Tables.News.sortBy(_.date.desc).buildColl[List]
      }
    }
  }

}
