package ru.meridor.website.processing

import com.redfin.sitemapgenerator.{W3CDateFormat, WebSitemapGenerator, ChangeFreq, WebSitemapUrl}
import java.util.Date
import com.redfin.sitemapgenerator.WebSitemapUrl.Options
import java.io.File
import java.net.URL
import org.joda.time.format.DateTimeFormat
import javax.servlet.http.HttpServletRequest

/**
 * Adds capability to generate sitemap.xml file based on URL information
 */
trait AutoSitemapSupport {

  private val urls = scala.collection.mutable.ListBuffer[(String, Date, ChangeFreq, Double)]()

  /**
   * Adds sitemap URL where date can be specified as a string in format <em>yyyy-MM-dd</em>
   * @param url
   * @param lastMod
   * @param changeFreq
   * @param priority
   */
  def addSitemapUrl(url: String, lastMod: String, changeFreq: ChangeFreq, priority: Double){
    addSitemapUrl(url, DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(lastMod).toDate, changeFreq, priority)
  }

  def addSitemapUrl(url: String, lastMod: Date, changeFreq: ChangeFreq, priority: Double){
    val urlEntry = (url, lastMod, changeFreq, priority)
    urls += urlEntry
  }

  def generateSitemap(baseUrl: URL, outputDir: File)(implicit request: HttpServletRequest){
    val generator = WebSitemapGenerator
      .builder(baseUrl, outputDir)
      .dateFormat(new W3CDateFormat(W3CDateFormat.Pattern.DAY))
      .autoValidate(true)
      .build()
    urls.foreach(url => generator.addUrl(new WebSitemapUrl(
      new Options(RequestUtils.absoluteUrlFromRelative(url._1))
        .lastMod(url._2)
        .changeFreq(url._3)
        .priority(url._4)
    )))
    generator.write()
  }
}
