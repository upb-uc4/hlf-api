package de.upb.cs.uc4.hyperledger.utilities.helper

import java.text.SimpleDateFormat
import java.util.Calendar

import com.google.gson.Gson

object StringHelper {
  def getOperationIdFromOperation(operationInfo: String): String = {
    operationInfo
      .replace("\\\"", "\"")
      .replace("\\\\", "\\")
      .split(""""operationId":"""").tail.head // index 1
      .split("""","transactionInfo""").head
  }

  def getTransactionInfoFromOperation(operationInfo: String): String = {
    operationInfo
      .replace("\\\"", "\"")
      .replace("\\\\", "\\")
      .split(""""transactionInfo":\{""").tail.head // index 1
      .split("""},"initiator""").head
  }

  def getInfoFromTransactionInfo(transactionInfo: String): (String, String, Seq[String]) = {
    val contractName: String = transactionInfo
      .split("contractName\":\"").tail.head
      .split("\"").head
    val transactionName: String = transactionInfo
      .split("transactionName\":\"").tail.head
      .split("\"").head
    val transactionParamsStringPlus1: String = transactionInfo
      .split("parameters\":\"").tail.head
    val transactionParamsString = transactionParamsStringPlus1.substring(0, transactionParamsStringPlus1.lastIndexOf("\""))

    val transactionParamsArray: Array[String] = parameterArrayFromJson(transactionParamsString)
    (contractName, transactionName, transactionParamsArray.toSeq)
  }

  def parameterArrayToJson(params: Seq[String]): String = parameterArrayToJson(params.toArray)
  def parameterArrayToJson(params: Array[String]): String = {
    new Gson().toJson(params)
  }
  def parameterArrayFromJson(json: String): Array[String] = {
    new Gson().fromJson(json, classOf[Array[String]])
  }
  def objectArrayFromJson(json: String): Array[Object] = {
    new Gson().fromJson(json, classOf[Array[Object]])
  }

  def getCurrentDate: String = {
    val current = Calendar.getInstance()
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(current.getTime)
  }
}
