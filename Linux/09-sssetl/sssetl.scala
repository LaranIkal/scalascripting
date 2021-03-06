/*******************************************************************************
* title: Main script that runs a function from utils.scala, Utils object.
* author: Carlos Kassab
* date: 08/27/2020
* date: 09/04/2020, Carlos Kassab, Adding EplSite db report groups ETL
*
*******************************************************************************/

import scala.util.{Try, Success, Failure}
import scala.collection.mutable.Map


object SssEtl extends App {
  def Main(procSequences: Array[String]): Unit = {

    val log = Utils.GetLogFilePointer // Create log file and return PrintWriter
    val sssConfig = Utils.GetConfigInfoFromFile(System.getProperty("user.dir")+"/config.hash") // get config info

    // Delete old log files
    log.write(s"Delete old log files, keeping: ${sssConfig("numDaysToKeepLogFiles")} days.\n")
    Utils.DeleteLogFiles(sssConfig("numDaysToKeepLogFiles").toInt)

    log.write("Open db connection, if success, the process continues.\n")
    Utils.DbConnection(sssConfig("sourcedbtype"), sssConfig("sourcedbinfo"), "", "") match
    {
      case Success(sssConnection) => {
        procSequences.foreach(procSequence => {

          /*************************************************************************
          * Read EplSite db modules and store it in a csv file. SELECT title, active FROM eplsite_modules
          *************************************************************************/
          if( procSequence == "99" || procSequence == "10" ) {
            ReadEplSiteModules.ReadModules(log, sssConfig, sssConnection) match {
              case Success(_) => 
              case Failure(f) => log.write(s"There was a problem: $f while running procSequence:$procSequence.\n")
            }
          }

          /*************************************************************************
          * Read EplSite db pages categories and store it in a csv file. SELECT title, description FROM EPLSITE_PAGES_CATEGORIES
          *************************************************************************/
          if( procSequence == "99" || procSequence == "20" ) {
            ReadPagesCategories.PagesCategories(log, sssConfig, sssConnection) match {
              case Success(_) => 
              case Failure(f) => log.write(s"There was a problem: $f while running procSequence:$procSequence.\n")
            }            
          }
          
          /*************************************************************************
          * Read EplSite db report groups and store it in a csv file. SELECT ReportGroupID, ReportGroupDescription FROM eplsite_report_groups
          *************************************************************************/
          if( procSequence == "99" || procSequence == "30" ) {
            ReadReportGroups.ReportGroups(log, sssConfig, sssConnection) match {
              case Success(_) => 
              case Failure(f) => log.write(s"There was a problem: $f while running procSequence:$procSequence.\n")
            }            
          }

          /*************************************************************************
          * Read EplSite db reports and store it in a csv file. SELECT ReportID, ReportDescription FROM eplsite_reports_definition
          *************************************************************************/
          if( procSequence == "99" || procSequence == "40" ) {
            ReadEplSiteReports.Reports(log, sssConfig, sssConnection) match {
              case Success(_) => 
              case Failure(f) => log.write(s"There was a problem: $f while running procSequence:$procSequence.\n")
            }            
          }

        }) // procSequences.foreach(procSequence => {
        
        sssConnection.close()
      } 
      case Failure(f) => println(f) // Connection failure.
    } //  Utils.DbConnection("sqlite", dbInfo, "", "") match
    log.close // Close log file.
  } // def Main(procSequences: Array[String]): Unit = {
} // object SssEtl extends App {
