package ru.meridor.website.processing

/**
 * An interface for classes which support internal data validation
 */
trait ValidationSupport {
  def isValid: Boolean
}
