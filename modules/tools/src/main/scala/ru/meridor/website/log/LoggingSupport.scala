package ru.meridor.website.log

import org.slf4j.LoggerFactory

/**
 * Adds basic logging support
 */
trait LoggingSupport {

  /**
   * Logger instance
   */
  val logger = LoggerFactory.getLogger(this.getClass)

}
