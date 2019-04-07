package com.clickstream.log.analysis

import scala.io.Source
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.clickstream.log.common.LogObject
import com.google.gson.stream.JsonReader
import java.io.StringReader

import scala.collection.immutable.Map
import scala.collection.mutable.SortedMap
import scala.collection.mutable.ListBuffer
import com.clickstream.log.common.commonutils

import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggerFactory

/**
 * This object is an entry point for performing analytics on log
 *
 */
object LogAnalyzer {
  private val logger: Logger = Logger.getLogger(LogAnalyzer.getClass)
  val logFile = "/home/impadmin/work/Impetus/ServiceSource/scala/work/clickstream.log"

  /**
   * main method which accepts an argument as no of days to start it's execution
   * @param args number of days to generate analytics report for those many days
   */
  def main(args: Array[String]) {
    val days = 20
    logger.info("Entering LogAnalyzer main method")
    val json_string = Source.fromFile(logFile) //.getLines()
    implicit val filter_data_map = getAllLogDataList(json_string.getLines(),days)
    
    val distinct_user_count = getDistinctUserCount(filter_data_map)
    logger.info("Distinct user count stored in file for " + days + " days to file " + distinct_user_count)

    val purchased_item_count = getPurchasedItemCount(filter_data_map)
    logger.info("Total item purchased stored in file for " + days + " days to file " + purchased_item_count)

   val added_to_cart_item_count = getAddToCartItemsCount(filter_data_map)
    logger.info("Total items added to cart but not purchased are stored in file for " + days + " days to file " + added_to_cart_item_count)

    val session_id_count = getSessionCount(filter_data_map)
    logger.info("Total sessions without purchase stored in file for " + days + " days to file " + session_id_count)
    logger.info("Exiting LogAnalyzer main method")
  }

  /**
   * This method fetches all data from log file as a List
   * @param  json_string a json format data
   * @returns allDataList of LogObject type
   *
   */
  def getAllLogDataList(json_string: Iterator[String], days: Integer) = {
    logger.info("Entering LogAnalyzer getAllLogDataList method")
    val all_log_list = new ListBuffer[LogObject]
    var filter_data_map= Map[String,ListBuffer[LogObject]] ()
    for (log_data <- json_string) {
      val myObj = convertJsonToObject(log_data)
      val date = myObj.date.substring(0, 10)
      myObj.date=date
      all_log_list += myObj
    }
    logger.info("Exiting LogAnalyzer getAllLogDataList method")
    val from_date = commonutils.daysAgo(days)
    filter_data_map = all_log_list.filter(_.date >= from_date.substring(0, 10)).groupBy(_.date) //.mapValues(_.size).toList.sortBy(_._1)
    
    filter_data_map
  }

  /**
   * This method writes the total distinct user count who logged in in a specific date
   * @param  all_log_list this has a list of LogObjects containing all data
   * @param days number of days for which the detail is needed
   * @returns boolean True if the data is written into file else false
   *
   */
  def getDistinctUserCount(filter_data_map: Map[String,ListBuffer[LogObject]]) = {
    logger.info("Entering LogAnalyzer getDistinctUserCount method" )
    var is_written = false
    val user_id_map = SortedMap[String, Integer]()
    var total_user = 0
    for (key <- filter_data_map) {
      val user_id = key._2.map(_.userid)
      total_user = user_id.distinct.size
      user_id_map.put(key._1, total_user)
    }
    commonutils.writeAnalyticsResult(user_id_map, "distinct_id.txt")
    is_written = true
    logger.info("Exiting LogAnalyzer getDistinctUserCount method")
    is_written
  }

  /**
   * This method writes the total purchased item for number of days
   * @param  all_log_list this has a list of LogObjects containing all data
   * @param days number of days for which the detail is needed
   * @returns boolean True if the data is written into file else false
   *
   */
  def getPurchasedItemCount(filter_data_map: Map[String, ListBuffer[LogObject]]) = {
    logger.info("Entering LogAnalyzer getPurchasedItemCount method")
    var is_written = false
    val http_method_map = SortedMap[String, Integer]()
    for (key <- filter_data_map) {
      val total_purchased_items = key._2.map(_.http_method).count(_ == "post")
      http_method_map.put(key._1, total_purchased_items)
    }
    commonutils.writeAnalyticsResult(http_method_map, "purchased_item.txt")
    is_written = true
    logger.info("Exiting LogAnalyzer getPurchasedItemCount method")
    is_written
  }

  /**
   * This method writes the total items added to cart but not purchsed for number of days
   * @param  all_log_list this has a list of LogObjects containing all data
   * @param days number of days for which the detail is needed
   * @returns boolean True if the data is written into file else false
   *
   */
  def getAddToCartItemsCount(filter_data_map: Map[String, ListBuffer[LogObject]]) = {
    logger.info("Entering LogAnalyzer getAddToCartItemsCount method")
    var is_written = false
    val http_method_map = SortedMap[String, Integer]()
    for (key <- filter_data_map) {
      val post_method_filter = key._2.map(_.http_method).filter(_ != "post")
      val post_method_count = key._2.map(_.http_method).count(_ == "post")
      val put_method_count = key._2.map(_.http_method).count(_ == "put")
      val total_added_items = post_method_filter.count(_ == "put") - post_method_count
      http_method_map.put(key._1, total_added_items)
    }
    commonutils.writeAnalyticsResult(http_method_map, "added_to_cart_item.txt")
    is_written = true
    logger.info("Exiting LogAnalyzer getAddToCartItemsCount method")
    is_written
  }

  /**
   * This method writes the total items added to cart but not purchsed for number of days
   * @param  all_log_list this has a list of LogObjects containing all data
   * @param days number of days for which the detail is needed
   * @returns boolean True if the data is written into file else false
   *
   */
  def getSessionCount(filter_data_map: Map[String,ListBuffer[LogObject]]) = {
    logger.info("Entering LogAnalyzer getSessionCount method")
    var is_written = false
    val session_id_map = SortedMap[String, Integer]()
    for (key <- filter_data_map) {
      val all_session_id = key._2.map(_.sessionid)
      val session_id_grouped = all_session_id.groupBy(identity).mapValues(_.size)
      val post_method_filter = key._2.map(_.http_method).filter(_ == "post")
      val session_id_count = session_id_grouped.size - post_method_filter.length
      session_id_map.put(key._1, session_id_count)
    }
    commonutils.writeAnalyticsResult(session_id_map, "session_id.txt")
    is_written = true
    logger.info("Exiting LogAnalyzer getSessionCount method")
    is_written
  }

  /**
   * This method convers a json to custom object LogObject
   * @param  log_data as a json string
   * @returns LogObject which has all required fields in it
   *
   */
  def convertJsonToObject(log_data: String) = {
    logger.info("Entering LogAnalyzer convertJsonToObject method")
    val gson = new Gson()
    val reader = new JsonReader(new StringReader(log_data.toString()));
    reader.setLenient(false);
    val jsonStringAsObject = new JsonParser().parse(reader).getAsJsonObject
    val myObj: LogObject = gson.fromJson(jsonStringAsObject, classOf[LogObject])
    reader.close()
    logger.info("Exiting LogAnalyzer convertJsonToObject method")
    myObj
  }

}