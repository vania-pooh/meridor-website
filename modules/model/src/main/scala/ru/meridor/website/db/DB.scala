package ru.meridor.website.db

import scala.slick.jdbc.JdbcBackend

object DB extends JdbcBackend {

  private val db = Database.forDataSource(ConnectionPooler.getDataSource)

  def withSession[T](f: Session => T): T = db.withSession(f)

  def withTransaction[T](f: Session => T): T = db.withTransaction(f)

}