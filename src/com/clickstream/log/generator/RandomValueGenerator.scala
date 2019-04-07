package com.clickstream.log.generator

import scala.util.Random
import java.util.Calendar
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer

object randomvaluegenerator {

  /*
   * This method is to generate random userid
   * @return String as userid
   */
  def getUserId = {
    var i = 0;
    var user_id = "null";
    var user_id_list = ListBuffer[String]()
    val random = new Random
    var x = random.nextInt(50)
    x = x * 10
    var y = x + 10
    while (i < 10) {
      user_id = random.nextInt(y).toString()
      user_id_list += user_id
      i += 1
    }
    user_id_list
  }

  /*
   * This method is to take random user id from an array
   * @return String as user action
   */
  def getRandomUserAction(user_action_array: Array[String]) = {
    val random = new Random()
    val user_action = random.shuffle(user_action_array.toList).head
    user_action
  }

}