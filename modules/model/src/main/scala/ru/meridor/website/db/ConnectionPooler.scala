/**
 *
 */
package ru.meridor.website.db

import java.util.Properties
import java.sql.Connection
import com.jolbox.bonecp.BoneCPDataSource
import ru.meridor.website.log.LoggingSupport

/**
 * Delivers basic connection pooler support
 */
trait ConnectionPoolerSupport extends LoggingSupport {
  
  def getDataSource = ConnectionPooler.getDataSource

  def getConnection = ConnectionPooler.getConnection
}

/**
 * An object to access database from any class
 */
object ConnectionPooler extends LoggingSupport {

  private lazy val cpds = {
    logger.info("Loading database properties...")
    val props = new Properties
    props.load(getClass.getResourceAsStream("/bonecp.properties"))
    val cpds = new BoneCPDataSource
    cpds.setProperties(props)
    logger.info("Initialized BoneCP connection pool.")
    cpds
  }

  /**
   * Returns connection pooler data source
   * @return
   */
  def getDataSource = cpds

  /**
   * Shuts down connection pool. Is expected to be called when application finishes its work (e.g. on servlet destroy).
   */
  def shutdown() {
    logger.info("Shutting down BoneCP connection pool...")
    getDataSource.close()
  }

  /**
   * Returns a single database connection
   * @return
   */
  def getConnection = {
    logger.debug("Getting connection from the pool...")
    val connection = getDataSource.getConnection
    logger.debug("Got instance " + connection + "")
    connection
  }

  /**
   * Tries to close JDBC connection
   * @param connection
   */
  def closeConnection(connection: Connection){
    if ( (connection != null) && !connection.isClosed ){
      logger.debug("Closing connection instance " + connection + "...")
      connection.close()
    }
  }

}