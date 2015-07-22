package com.ruimo.samples.scannerclient

object ConnectionParam {
  val OrgId = "zsfzwd"
  val DeviceType = "Scanner"
  val Port = 1883
  val Url = "tcp://" + OrgId + ".messaging.internetofthings.ibmcloud.com:" + Port
}
