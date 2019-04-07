package com.clickstream.log.generator

import scala.io.Source
import java.time.LocalDateTime
import scala.util.Random
import java.util.Date

import com.google.gson.Gson
import java.io.FileWriter
import java.io.PrintWriter
import java.io.FileOutputStream
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import com.clickstream.log.common.commonutils
import org.apache.log4j.Logger

/**
 * This object is an entry point for generating analytics log
 *
 */

object loggenerator {
  private val logger: Logger = Logger.getLogger(loggenerator.getClass)
  /**
   * main method which accepts an argument as number of days to start it's execution
   * @param args number of days to generate logs for those many days
   */
  def main(args: Array[String]) {
    logger.info("Entering loggenerator main method")
    var days = 20;
    while (days > 0) {
      val country_array = Array[String]("India", "US", "Australia", "Canada", "Spain", "Newzeland", "Austria", "Nitherland")
      val action_method_url_map = commonutils.getInputDataFromFile
      val user_action_array = action_method_url_map.keySet.toArray
      val distinct_user_id_list = randomvaluegenerator.getUserId.distinct
      val user_session_id_map = commonutils.getSessionIdMap(distinct_user_id_list)

      for (user_id <- distinct_user_id_list) {
        val session_id = user_session_id_map(user_id)
        val user_action = randomvaluegenerator.getRandomUserAction(user_action_array)
        val country = randomvaluegenerator.getRandomUserAction(country_array)
        val httpMethodsAndUrlsList = commonutils.getAllHttpMethodAndUrlList(user_action, action_method_url_map)
        commonutils.writeLog(user_id, user_action, country, session_id, httpMethodsAndUrlsList, days)
      }
      days -= 1
    }
    logger.info("logs generated")
    logger.info("Exiting loggenerator main method")
  }
}