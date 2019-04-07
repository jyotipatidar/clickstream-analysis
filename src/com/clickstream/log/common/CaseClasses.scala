package com.clickstream.log.common
/**
 * This contains all used case classes
 */
case class InputFile(action: String, http_method: String, url: String)
case class LogObject(userid: String, user_action: String, http_method: String, url: String, country: String, sessionid: String, var date: String)
case class HTTPMethodURLMapper(http_method: String, url: String)