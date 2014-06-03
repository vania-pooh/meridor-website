package ru.meridor.website.db.entities

import ru.meridor.website.db.tables.Tables
import ru.meridor.website.db.DB
import scala.slick.driver.PostgresDriver.simple._
import java.sql.Timestamp
import java.util.Date

case class Contact(
    id: Option[Long] = None,
    contactType: ContactType.ContactType = ContactType.Person,
    numRequests: Int = 1, //We create contact on first request
    created: Timestamp = new Timestamp(new Date().getTime)
)

object ContactType extends Enumeration {
  type ContactType = Value
  val Person = Value(0)
  val Organization = Value(1)
  def fromNumber(number: Int): ContactType = number match {
    case 0 => Person
    case 1 => Organization
  }
}

object Contact extends Entity[Contact, Long]{


  /**
   * Returns unique identifier of an entity
   * @param contact
   * @return
   */
  def id(contact: Contact): Long = contact.id match {
    case Some(id) => id
    case None => 0
  }

  /**
   * Searches for an entity by its contactId
   * @param contactId
   * @return None if not exists, Some(entity instance) if exists
   */
  def exists(contactId: Long): Option[Contact] = {
    try{
      DB withSession { implicit session =>
        val contactRows = Tables.Contacts.filter(_.contactId === contactId).buildColl[List]
        contactRows.size match {
          case 0 => None
          case _ => {
            val contactRow = contactRows(0)
            Some(new Contact(
              Some(contactRow.contactId),
              ContactType.fromNumber(contactRow.contactType),
              contactRow.numRequests,
              contactRow.created
            ))
          }
        }
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        None
      }
    }
  }

  /**
   * Creates a new entity in the database
   * @param contact
   * @return None on failure and Some(entity) on success
   */
  def create(contact: Contact): Option[Contact] = {
    try{
      DB withSession { implicit session =>
        val contactId = ( Tables.Contacts returning Tables.Contacts.map(_.contactId) )  += Tables.ContactsRow(0, contact.contactType.id.toShort, contact.numRequests, contact.created)
        Some(new Contact(Some(contactId), contact.contactType, contact.numRequests, contact.created))
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        None
      }
    }
  }

  /**
   * Updates entity columns
   * @param contact an entity with updated fields
   * @return
   */
  def update(contact: Contact): Option[Contact] = Some(contact)

}

case class Person(
  contact: Option[Contact] = None,
  firstName: String,
  middleName: Option[String] = None,
  lastName: Option[String] = None,
  position: Option[String] = None,
  cellPhone: Long,
  fixedPhone: Option[Long] = None,
  passport: Option[String] = None,
  address: Option[String] = None,
  district: Option[String] = None,
  age: Option[Int] = None,
  profession: Option[String] = None,
  averagePayment: Option[Double] = None,
  misc: Option[String] = None
)

object Person extends Entity[Person, Long]{


  /**
   * Returns unique identifier of an entity
   * @param person
   * @return
   */
  def id(person: Person): Long = person.cellPhone

  /**
   * Searches for an entity by its contactId
   * @param cellPhone
   * @return None if not exists, Some(entity instance) if exists
   */
  def exists(cellPhone: Long): Option[Person] = {
    try{
      DB withSession { implicit session =>
        val persons = Tables.Persons.filter(_.cellPhone === cellPhone).buildColl[List]
        persons.size match {
          case 0 => None
          case _ => {
            val person = persons(0)
            Contact.exists(person.contactId) match {
              case Some(contact) => Some(new Person(
                Some(contact),
                person.firstName,
                person.middleName,
                person.lastName,
                person.position,
                person.cellPhone,
                person.fixedPhone,
                person.passport,
                person.address,
                person.district,
                person.age,
                person.profession,
                person.averagePayment,
                person.misc
              ))
              case None => None
            }
          }
        }
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        None
      }
    }
  }

  /**
   * Creates a new entity in the database
   * @param person
   * @return None on failure and Some(entity) on success
   */
  def create(person: Person): Option[Person] = {
    try{
      DB withTransaction { implicit session =>
        val contact = Contact.create(new Contact())
        contact match {
          case Some(ct) => ct.id match {
            case Some(id) => {
              Tables.Persons += Tables.PersonsRow(
                id,
                person.firstName,
                person.middleName,
                person.lastName,
                person.position,
                person.cellPhone,
                person.fixedPhone,
                person.passport,
                person.address,
                person.district,
                person.age,
                person.profession,
                person.averagePayment,
                person.misc
              )

              Some(new Person(
                Some(ct),
                person.firstName,
                person.middleName,
                person.lastName,
                person.position,
                person.cellPhone,
                person.fixedPhone,
                person.passport,
                person.address,
                person.district,
                person.age,
                person.profession,
                person.averagePayment,
                person.misc
              ))
            }
            case None => None
          }
          case None => None
        }
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        None
      }
    }
  }

  /**
   * Updates entity columns
   * @param person an entity with updated fields
   * @return
   */
  def update(person: Person): Option[Person] = Some(person)

}