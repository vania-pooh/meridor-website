package ru.meridor.website.util

import org.apache.http.impl.client.DefaultHttpClient
import scala.util.parsing.json.{JSON, JSONObject}
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.Consts
import java.io.{InputStreamReader, BufferedReader}

/**
 * Support for sending POST requests with raw JSON
 */
trait JSONPostRequestSupport {
  private val USER_AGENT = "Mozilla/5.0"
  private lazy val httpClient = new DefaultHttpClient()

  private def getRequestJSON(requestParameters: Map[String, Any]): String = {
    import org.json4s.jackson.Json
    import org.json4s.DefaultFormats
    Json(DefaultFormats).write(requestParameters)
  }

  private def processRequest(url: String, requestParameters: String): String =
    if ( (url != null) && (requestParameters != null) ){
      val post = new HttpPost(url)
      post.setHeader("User-Agent", USER_AGENT)
      post.setHeader("Accept", "application/json")
      post.setHeader("Content-type", "application/json")
      post.setEntity(new StringEntity(requestParameters, Consts.UTF_8))
      val response = httpClient.execute(post)
      val rd = new BufferedReader(new InputStreamReader(response.getEntity.getContent))

      val result = new StringBuffer()
      var line = ""
      while (line != null){
        result.append(line)
        line = rd.readLine()
      }
      result.toString
    } else "{}"

  private def getResponseMap(response: String): Map[String, Any] = {
    JSON.parseFull(response) match {
      case Some(mapOrList) => mapOrList match {
        case map: Map[_,_] => map.asInstanceOf[Map[String, Any]]
        case list: List[_] => Map("list" -> list)
        case _ => Map.empty[String, Any]
      }
      case None => Map.empty[String, Any]
    }
  }

  /**
   * Executes a single request
   * @param url
   * @param requestParameters
   * @return
   */
  protected def sendRequest(url: String, requestParameters: Map[String, Any]): Map[String, Any] =
    getResponseMap(processRequest(url, getRequestJSON(requestParameters)))

}
