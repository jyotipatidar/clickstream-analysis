# clickstream-analysis
This project has 2 modules 2 Log File Generation and 2) Clickstream analysis
Log file Generator:
This project is in Scala which will generate a log file for the day for various user-sessions for an e-commerce website. 
Generator will generate data for N days (provided as argument to the generator-tool)
Generated log file will contains the event detail in JSON format for user click action and will consists following fields:
datetime                           DateTime when event triggered
userid                             Userid of the user
country                            A random country name, which will same for a session but same user logged-in again                                              he/she might get another country-name.
sessionid                          SessionId to track the user session
http method                        HTTP method (GET/POST/DELET) to distinguish user action.
                                   GET for view-item, 
                                   PUT for add item to cart, 
                                   DELETE for delete item from cart.
                                   POST to purchase the item.

url                                Item detail as /category/sub-category/item
                                   Login : /login
                                   Logout: /logout

Sample logs
e.g. 
{"datetime":"2015-12-13 00:03:37.745","userid":"492","country":"canada","sessionid":"dbfa3e2f6a274e3285fc99a7610edcad","http_method":"GET","url":"/login"}
{"datetime":"2015-12-13 00:03:38.145","userid":"492","country":"canada","sessionid":"dbfa3e2f6a274e3285fc99a7610edcad","http_method":"GET","url":"/electroncis/home-appliance/LG-TV"}
{"datetime":"2015-12-13 00:03:39.715","userid":"492","country":"canada","sessionid":"dbfa3e2f6a274e3285fc99a7610edcad","http_method":"PUT","url":"/electroncis/home-appliance/LG-TV"}
{"datetime":"2015-12-13 00:03:41.145","userid":"492","country":"canada","sessionid":"dbfa3e2f6a274e3285fc99a7610edcad","http_method":"GET","url":"":"/electroncis/office-usage/Panasonic-Projector"}
{"datetime":"2015-12-13 00:03:47.725","userid":"492","country":"canada","sessionid":"dbfa3e2f6a274e3285fc99a7610edcad","http_method":"POST","url":"/electroncis/home-appliance/LG-TV"}
{"datetime":"2015-12-13 00:03:50.347","userid":"492","country":"canada","sessionid":"dbfa3e2f6a274e3285fc99a7610edcad","http_method":"GET","url":"/logout"}


Analysis:
Following analytics is performed on the generated log file
1. By User 
              a. Total count of  distinct user ( at  day level )
2. By Item
              a. Total item purchased ( at  day level )
              b. Total item added to cart but not purchased ( at  day level )
3. By Session
              a. Total number of sessions where user logged-in and logged out without purchase (at day level).

Note: 
1. Above analysis is performed on last N days (provided as argument to the tool)
2. After analysis, generated text-file for each use-case with analysis result.

