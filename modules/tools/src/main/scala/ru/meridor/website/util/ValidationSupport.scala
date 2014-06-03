package ru.meridor.website.util

/**
 * An interface for classes which support internal data validation
 */
trait ValidationSupport {
  def isValid: Boolean
}
