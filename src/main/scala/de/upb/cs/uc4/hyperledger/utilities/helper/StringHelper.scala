package de.upb.cs.uc4.hyperledger.utilities.helper

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

  def parameterArrayToJson(params: Array[String]): String = {
    new Gson().toJson(params)
  }

  def parameterArrayFromJson(json: String): Array[String] = {
    new Gson().fromJson(json, classOf[Array[String]])
  }
}
