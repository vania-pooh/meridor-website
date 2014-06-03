package ru.meridor.website

import org.scalatra.test.specs2._
import scala.xml.{Elem, XML}
import org.specs2.matcher.MatchResult
import ru.meridor.website.log.LoggingSupport
import java.net.URL

class PagesServletSpec extends ScalatraSpec with LoggingSupport {

  addServlet(classOf[PagesServlet], "/*")

  def is = s2"""
    On WebsiteServlet the following conditions should be met:
    GET /sitemap.xml should return status 200 $sitemap200
    GET /sitemap.xml should return non-empty body $sitemapNonEmpty
    GET /sitemap.xml should return correct entries ${
      foreachSitemapEntry {entry => {
          logger.info("Processing sitemap entry = " + entry)
          val url = (entry \ "loc").text
          val priority: Double = (entry \ "priority").text.toDouble
          val frequency = (entry \ "changefreq").text
          url.length must beGreaterThan(0)
          priority must beGreaterThan(0d)
          priority must beLessThanOrEqualTo(1d)
          frequency.length must beGreaterThan(0)
          frequency must beOneOf(
            "always",
            "hourly",
            "daily",
            "weekly",
            "monthly",
            "yearly",
            "never"
          )
        }
      }
    }
    GET(any page in sitemap.xml) should return status 200, 301 or 302 and non-empty body for status = 200 ${
      foreachSitemapEntry {entry => {
          val url = new URL((entry \ "loc").text).getPath
          logger.info("Processing relative url = " + url)
          get(url){
            if (status == 200){
              body.length must beGreaterThan(0)
            }
            status must beOneOf(200, 301, 302)
          }
        }
      }
    }
    }"""

  def sitemap200 = get("/sitemap.xml") {
    status mustEqual 200
  }

  def sitemapNonEmpty = get("/sitemap.xml"){
    body.length must beGreaterThan(0)
  }

  implicit lazy val xml: Elem = get("/sitemap.xml"){
    XML.loadString(body)
  }

  private def foreachSitemapEntry(f: scala.xml.Node => MatchResult[_]) = {
    xml match {
      case <urlset>{urls @ _*}</urlset> =>
        for (url @ <url>{_*}</url> <- urls) yield f(url)
    }
  }

}
