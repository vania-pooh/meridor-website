/**
 *
 */
package ru.meridor.website.db

import org.scalatra.ScalatraServlet
import org.slf4j.LoggerFactory

import java.util.Properties

import com.jolbox.bonecp.BoneCPDataSource
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

/**
 * Delivers basic Slick and BoneCP support
 */
trait SlickSupport extends ScalatraServlet {
  
  private val logger = LoggerFactory.getLogger(getClass)

  private val cpds = {
    logger.info("Loading database properties...")
    val props = new Properties
    props.load(getClass.getResourceAsStream("/bonecp.properties"))
    val cpds = new BoneCPDataSource
    cpds.setProperties(props)
    logger.info("Initialized BoneCP connection pool.")
    cpds
  }

  /**
   * This will be used for database queries
   */
  val db = Database.forDataSource(cpds)

  /**
   * Connection pooler will be automatically shut down on servlet destroy
   */
  override def destroy() {
    super.destroy()
    shutdownConnectionPooler
  }
  
  def shutdownConnectionPooler() {
    logger.info("Shutting down BoneCP connection pool...")
    cpds.close
  }

}