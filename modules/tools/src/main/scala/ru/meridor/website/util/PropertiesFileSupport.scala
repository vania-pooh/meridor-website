package ru.meridor.website.util

import java.util.Properties

/**
 * Gives a simple interface to *.properties file
 */
trait PropertiesFileSupport {

  private lazy val properties: Option[Properties] = {
    val props = new Properties
    try{
      props.load(getClass.getResourceAsStream(propertiesFileName))
      Some(props)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        None
      }
    }
  }

  /**
   * Name of the file to use. For files from resources folder it's recommended to use / prefix,
   * e.g. "/somefile" instead of "somefile".
   * @return
   */
  protected def propertiesFileName: String

  protected def getProperty(name: String): Option[String] = {
    properties match {
      case Some(pr) => if (pr.get(name) != null)
        Some(pr.get(name).toString)
        else None

      case None => None
    }
  }

  protected def getPropertyOrEmptyString(name: String): String = getProperty(name) match {
    case Some(str) => str
    case None => ""
  }

}
