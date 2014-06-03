package ru.meridor.website.db.entities

/**
 * An interface for a database entity.
 * T = entity type, I = identifier type (can be a structured type too)
 */
trait Entity[T, I] {

  /**
   * Returns unique identifier of an entity
   * @param entity
   * @return
   */
  def id(entity: T): I

  /**
   * Searches for an entity by its contactId
   * @param id
   * @return None if not exists, Some(entity instance) if exists
   */
  def exists(id: I): Option[T]

  /**
   * Creates a new entity in the database
   * @param entity
   * @return None on failure and Some(entity) on success
   */
  def create(entity: T): Option[T]

  /**
   * Updates entity columns
   * @param entity an entity with updated fields
   * @return
   */
  def update(entity: T): Option[T]

  /**
   * Creates a new entity if not exists or updates a new one if exists
   * @param entity
   */
  def createOrUpdate(entity: T): Option[T] = exists(id(entity)) match {
    case None => create(entity)
    case Some(e) => update(e)
  }
}