package com.ruimo.samples.scannerclient

import java.net.InetAddress
import java.net.Inet4Address
import java.nio.charset.Charset
import java.util.Enumeration
import scala.collection.JavaConversions._
import java.net.NetworkInterface
import com.ruimo.scoins.Conversions._
import com.ruimo.mqtt.mqttclient.{CallbackMqttClient, MqttMessage}
import org.fusesource.mqtt.client.{QoS, Topic, CallbackConnection, Listener}
import org.fusesource.hawtbuf.{UTF8Buffer, Buffer}

import ConnectionParam._
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.joda.time.format.ISODateTimeFormat
import spray.json._
import DefaultJsonProtocol._
import java.nio.charset.StandardCharsets._
import scala.util.{Try, Success, Failure}
import javafx.scene.input.KeyEvent
import javafx.scene.input.KeyCode

abstract class JavaAdapter {
  import JavaAdapter._

  private var conn: CallbackConnection = _
  private val keyBuf = new StringBuilder

  def initialize() {
    logger.info("Start to connect url = '" + Url + "', clientId = '" + ClientId + "'")

    onNotifyReport("接続中...");
    conn = CallbackMqttClient.createConnectionWithUserPassword(
      Url, ClientId, "use-token-auth", AuthToken
    ).listener(
      new Listener {
        def onConnected() {
          onNotifyReport("接続しました");
        }
        def onDisconnected() {
          onNotifyReport("接続が失われました");
          logger.info("Disconnected.")
        }
        def onPublish(topic: UTF8Buffer, payload: Buffer, ack: Runnable) {
          logger.info("Received command: '" + topic + "', " + payload.hex())
          val jsonString = new String(payload.toByteArray, "UTF-8")
          val commandString = topic.toString.split("/")(2)
          logger.info("Response received. Command: " + commandString + ", " + jsonString)
          val json: JsObject = JsonParser(jsonString).asJsObject

          commandString match {
            case "Show" =>
              val code: String = json.fields("code").convertTo[String]
              val count: Int = json.fields("count").convertTo[Int]
              onQueryCustomerVisitCompleted(code, count)

//          router ! OnReceiveMqttMessage(topic.toString, new String(payload.toByteArray, "UTF-8"))

            case "ShowUnregisteredUser" =>
              val code: String = json.fields("code").convertTo[String]
              onQueryCustomerUnregistered(code)
          }
          ack.run()
        }
        def onFailure(t: Throwable) {
          onNotifyReport("通信エラーが発生しました");
          logger.error("Error in mqtt", t)
        }
      }
    )
    conn.connect(
      CallbackMqttClient.voidCallback(
        success = {
          logger.info("Connected.")
          logger.info("Subscribing topic='" + TopicsToSubscribe + "'")
          conn.subscribe(
            TopicsToSubscribe,
            CallbackMqttClient.callback[Array[Byte]](
              success = (data: Array[Byte]) => {
                logger.info("Subscribe success.")
              },
              failure = (t: Throwable) => {
                logger.error("Subscribe failed.", t)
                Thread.sleep(3000)
//                self ! 'Connected
              }
            )
          )
          logger.info("Subscribing...")


//          self ! 'Connected
        },
        failure = (t: Throwable) => {
          onNotifyReport("通信エラーが発生しました");
          logger.error("Connection failure.", t)
          Thread.sleep(3000)
//            self ! 'Launch
        }
      )
    )
  }

  def reportKeyEvent(e: KeyEvent) {
    if (e.getCode == KeyCode.ENTER) {
      onInputChanged("")
      val customerId = keyBuf.toString
      if (customerId.startsWith("0")) {
        doSystemCommand(customerId)
      }
      else {
        onQueryCustomerVisitStart(customerId)
        val payLoad = Map[String, JsValue] (
          "d" -> JsObject(Map("code" -> JsString(customerId))),
          "ts" -> JsString(IsoDateTimeFormatter.print(System.currentTimeMillis))
        ).toJson.compactPrint
        logger.info("Publishing payload '" + payLoad + "' to topic '" + TopicToPublish + "'")

        onNotifyReport("送信中...");
        conn.publish(
          TopicToPublish, payLoad.getBytes(UTF_8), QoS.EXACTLY_ONCE, false,
          CallbackMqttClient.voidCallback(
            success = {
              onNotifyReport("");
              logger.info("Publish success.")
            },
            failure = (t: Throwable) => {
              onNotifyReport("送信エラー");
              logger.error("Publish failure.", t)
              Thread.sleep(3000)
            }
          )
        )
        keyBuf.clear()
      }
    }
    else {
      keyBuf.append(e.getText)
      onInputChanged(keyBuf.toString)
    }
  }

  private def doSystemCommand(command: String) {
    command match {
      case "00000001" =>
        onNotifyReport("システム終了中")
        doShutdown()

      case "00000002" =>
        onNotifyReport(IpAddress)
        keyBuf.clear()
    }
  }

  private def doShutdown() {
    logger.info("Perform shutdown.")
    Runtime.getRuntime.exec("sudo /sbin/shutdown -h now")
  }

  def onQueryCustomerVisitStart(id: String)

  def onQueryCustomerVisitCompleted(id: String, count: Int)

  def onQueryCustomerUnregistered(id: String)

  def onNotifyReport(input: String)

  def onInputChanged(input: String)
}

object JavaAdapter {
  val IsoDateTimeFormatter = ISODateTimeFormat.dateTime()
  val logger = LoggerFactory.getLogger(getClass)
  val TopicToPublish = "iot-2/evt/" + DeviceType + "/fmt/json"
  val AuthToken = System.getProperty("MqttAuthToken") ensuring (
    _ != null, "Specify Iotf auth token by system property '-DMqttAuthToken=xxx'."
  )
  val TopicsToSubscribe: Array[Topic] = Array(
    new Topic("iot-2/cmd/Show/fmt/json", QoS.EXACTLY_ONCE),
    new Topic("iot-2/cmd/ShowUnregisteredUser/fmt/json", QoS.EXACTLY_ONCE)
  )

  def nics: Iterator[NetworkInterface] = NetworkInterface.getNetworkInterfaces
  def ethNic: NetworkInterface = nics.find(_.getName.startsWith("eth")) match {
    case Some(intf) => intf
    case None => throw new Error("Cannot find NIC that starts with 'eth' to retrieve MAC address.")
  }

  lazy val MacAddress: String = toHexString(ethNic.getHardwareAddress)
  lazy val IpAddress: String = {
    val ips: Iterator[InetAddress] = ethNic.getInetAddresses
    ips.find(_.getClass == classOf[Inet4Address]) match {
      case Some(ip) => ip.getHostAddress
      case None =>  throw new Error("Interface " + ethNic.getName + " has no ipv4 address.")
    }
  }

  lazy val ClientId: String = "d:" + OrgId + ":" + DeviceType + ":" + MacAddress
}
