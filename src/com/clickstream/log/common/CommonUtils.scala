package com.clickstream.log.common

import scala.io.Source
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import java.util.Calendar
import com.google.gson.Gson
import java.io.PrintWriter
import java.io.FileOutputStream
import java.io.File
import java.text.SimpleDateFormat
import scala.collection.SortedMap
import com.clickstream.log.generator.sessionidgenerator

object commonutils {
  val inputDataFile = "/home/impadmin/work/Impetus/ServiceSource/scala/work/inputdata.csv"
  val DATE_FORMAT = "yyyy-MM-dd hh:mm:ss:SSS"

  /*
   * This method is to read raw data from input data file
   *
   * @return HashMap with user action, and http method/url as List
   */
  def getInputDataFromFile() = {
    val input_data_file = Source.fromFile(inputDataFile).getLines.drop(1)
    val action_method_url_map = new HashMap[String, List[String]]
    for (inputData <- input_data_file) {
      val columnData = inputData.split(",")
      val column = InputFile(columnData(0), columnData(1), columnData(2))
      val user_action = column.action
      val http_method = column.http_method
      val url = column.url
      val tmp_list = List(http_method, url)
      action_method_url_map.put(user_action, tmp_list)
    }
    action_method_url_map
  }

  /*
   * This method is to generate random MD5 session id token for every userid
   *
   * @return Map with user id as key and session_id as value
   */
  def getSessionIdMap(distinct_user_id_list: ListBuffer[String]) = {
    var user_session_id_map = Map[String, String]()
    for (user_id <- distinct_user_id_list) {
      var session_id = sessionidgenerator.generateMD5Token(user_id)
      user_session_id_map.put(user_id, session_id)
    }
    user_session_id_map
  }

  /*
   * This method is get HTTPMethod and URL from Map of input data based on randomly generated user action
   *
   * @return List which has http method and url
   */
  def getHttpMethodAndUrl(user_action: String, action_method_url_map: HashMap[String, List[String]], index: Integer) = {
    var user_http_method_list = action_method_url_map(user_action)
    var output = user_http_method_list.slice(index, index + 1)
    output
  }

  /*
   * This method is map all corresponding HTTPMethods and urls based on user action
   *
   * @return List which has http method and url list
   */
  def getAllHttpMethodAndUrlList(user_action: String, action_method_url_map: HashMap[String, List[String]]) = {
    var list = ListBuffer[HTTPMethodURLMapper]()

    if (user_action == "login") {
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("login", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("login", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper("get", "/logout"))
    }
    if (user_action == "view") {
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("login", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("login", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("view", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("view", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper("get", "/logout"))
    }
    if (user_action == "add") {
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("login", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("login", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("view", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("view", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("add", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("add", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper("get", "/logout"))
    }
    if (user_action == "delete") {
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("login", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("login", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("view", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("view", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("add", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("add", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("delete", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("delete", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper("get", "/logout"))
    }
    if (user_action == "purchase") {
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("login", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("login", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("view", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("view", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("add", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("add", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper(getHttpMethodAndUrl("purchase", action_method_url_map, 0).mkString(""), getHttpMethodAndUrl("purchase", action_method_url_map, 1).mkString("")))
      list.append(HTTPMethodURLMapper("get", "/logout"))
    }
    list
  }

  /*
   * This method is to write generated log data into a log file
   *
   * @return boolean True if done and False if not
   */
  def writeLog(user_id: String, user_action: String, country: String, session_id: String, httpMethodsAndUrlsList: ListBuffer[HTTPMethodURLMapper], days: Integer) = {
    val log_file = "/home/impadmin/work/Impetus/ServiceSource/scala/work/clickstream.log"
    var iswritten = false
    for (list <- httpMethodsAndUrlsList) {
      val http_method = list.http_method
      val url = list.url
      var date = daysAgo(days)
      println("userid = " + user_id + " user_action = " + user_action + " http_method = " + http_method + " url = " + url + " country = " + country + " date = " + date)
      var log = LogObject(user_id, user_action, http_method, url, country, session_id.toString(), date.toString())
      val gson = new Gson();
      var json_string = gson.toJson(log)
      var pw = new PrintWriter(new FileOutputStream(
        new File(log_file), true /* append = true */ ));
      pw.write(json_string + "\n")
      pw.close()
      iswritten = true
    }

  }

  /*
   * This method is to write analytics result of Total count of  distinct user ( at  day level )
   *
   * @return boolean True if done and False if not
   */
  def writeAnalyticsResult(map: SortedMap[String, Integer], filename: String) = {
    val output_filename = "/home/impadmin/work/Impetus/ServiceSource/scala/work/" + filename
    var iswritten = false
    var pw = new PrintWriter(new FileOutputStream(
      new File(output_filename), false /* append = true */ ));
    pw.write(map.mkString("\n"))
    pw.close()
    iswritten = true
  }

  /*
   * This method is generate dates based on the given user input
   *
   * @return date
   */
  def daysAgo(days: Int): String = {
    val calender = Calendar.getInstance()
    calender.roll(Calendar.DAY_OF_YEAR, -days)
    val sdf = new SimpleDateFormat(DATE_FORMAT)
    sdf.format(calender.getTime())
  }
}