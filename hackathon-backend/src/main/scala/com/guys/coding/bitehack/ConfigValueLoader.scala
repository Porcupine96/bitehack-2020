package com.guys.coding.bitehack

import java.io.InputStream
import java.util.Scanner

import com.typesafe.config.Config

object ConfigValueLoader {

  def provide(config: Config) =
    ConfigValues(
      config,
      ApplicationConfig(
        config.getString("application.host"),
        config.getInt("application.port"),
        config.getString("application.log-level")
      )
    )

  final case class ConfigValues(config: Config, applicationConfig: ApplicationConfig)

  final case class ApplicationConfig(bindHost: String, bindPort: Int, logLevel: String)

  //https://stackoverflow.com/a/5445161
  def streamToString(is: InputStream): String = {
    val s = new Scanner(is).useDelimiter("\\A")
    if (s.hasNext) s.next
    else ""
  }
}
