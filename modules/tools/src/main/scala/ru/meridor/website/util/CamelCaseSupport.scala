package ru.meridor.website.util

/**
 * Delivers basic methods to work with camel case
 */
trait CamelCaseSupport {
  /**
   * Converts UNDERSCORED_STRING to camelCaseString
   * @param s
   * @param lcFirst whether to make first letter lowercase
   * @return
   */
  protected def underscoredToCamelCase(s: String, lcFirst: Boolean = false): String = {
    val camelCaseString = new StringBuilder("")
    for (part <- s.split("_")) {
      camelCaseString.append(toProperCase(part))
    }
    if (lcFirst)
      Character.toLowerCase(camelCaseString.charAt(0)) + (if (camelCaseString.length > 1) camelCaseString.substring(1) else "")
      else camelCaseString.toString()
  }

  /**
   * Converts camelCaseString to UNDERSCORED_STRING
   * @param camelCaseString
   * @return
   */
  protected def camelCaseToUnderscored(camelCaseString: String): String = {
    val parts = camelCaseString.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")
    var ret = ""
    var partNumber: Int = 0
    for (part <- parts) {
      if (partNumber != 0) {
        ret += "_"
      }
      ret += part.toUpperCase
      partNumber += 1
    }
    ret
  }

  /**
   * Converts somestring to Somestring
   * @param s
   * @return
   */
  protected def toProperCase(s: String): String = s.substring(0, 1).toUpperCase + s.substring(1).toLowerCase
}
