package ru.meridor.website.util

object StringUtils {

  /**
   * Prepares multiline string to be used in space sensitive environment, e.g. when rendering markdown.
   * | symbols can be used to denote line start points.
   * @param str
   * @return
   */
  def prepareMultilineString(str: String): String = StringContext.treatEscapes(str.stripMargin)

}
