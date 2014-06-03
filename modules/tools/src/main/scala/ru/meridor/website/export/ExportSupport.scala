package ru.meridor.website.export

import java.io.{OutputStreamWriter, BufferedWriter}

/**
 * An entry point to various export actions
 */
trait ExportSupport {

  /**
   * Does export actions described by the job
   * @param job
   * @return
   */
  def export[U <: Data, V <: Data](job: Job[U,V]): Boolean = job.execute

}

/**
 * Describes all stages of a single export action: how to read the data, process it and output to the file
 */
case class Job[U <: Data, V <: Data](reader: Reader[U], processor: Processor[U, V] = NoOpProcessor[U](), writer: Writer[V] = StdOutWriter[V]()){

  /**
   * Executes job logic
   * @return
   */
  def execute: Boolean = writer.write(processor.process(reader.read))

}

/**
 * Reads the data to be processed
 */
trait Reader[T <: Data] {

  /**
   * Reads raw data from any desired source
   * @return
   */
  def read: T

}

/**
 * Does some transformations on the raw data provided by [[ru.meridor.website.export.Reader]]
 */
trait Processor[U <: Data, V <: Data] {

  /**
   * Does all required transformations on the data and returns the result
   * @param data data object to be processed
   * @return
   */
  def process(data: U): V

}

case class NoOpProcessor[U <: Data]() extends Processor[U, U] {

  /**
   * Does no processing at all
   * @param data data object to be processed
   * @return
   */
  def process(data: U) = data

}

/**
 * Writes final export data set to the format specified
 */
trait Writer[V <: Data] {

  /**
   * Writes prepared data to any desired destination
   * @param data data to be written
   * @return whether writing was successful
   */
  def write(data: V): Boolean

}

case class StdOutWriter[V <: Data]() extends Writer[V] {

  /**
   * Writes data to standard output using buffered writer
   * @param data data to be written
   * @return whether writing was successful
   */
  def write(data: V): Boolean = {
    try {
      val writer = new BufferedWriter(new OutputStreamWriter(System.out))
      writer.write(data.toString)
      writer.flush()
      true
    } catch {
      case e: Exception => false
    }
  }

}

/**
 * Trait for data container
 */
trait Data {

  /**
   * Returns data length in its proper sense. This can be length in bytes, paragraphs, pages and so on.
   * @return
   */
  def length: Int

}