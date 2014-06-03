package ru.meridor.website.ui

/**
 * An enum with possible page categories
 */
object PageCategory extends Enumeration {
  type PageCategory = Value
  val Home, Services, Bundles, Contact, Prices, Articles, Help, News = Value //Possible active menu items

  def getPageTitleCategory(pageCategory: PageCategory): String = pageCategory match{
      case Services => "Услуги"
      case Articles => "Статьи"
      case _ => ""
  }
}
