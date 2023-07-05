package com.bmmedia.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import pureconfig.error.FailureReason
import pureconfig.error.CannotConvert

// Given configReader: ConfigReader[EmberConfig]
final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig {
  given ConfigReader[Host] = ConfigReader[String].emap { hostString =>
    Host.fromString(hostString).toRight(CannotConvert(hostString, "Host", "Invalid host"))
  }
  given ConfigReader[Port] = ConfigReader[Int].emap { portInt =>
    Port
      .fromInt(portInt)
      .toRight(CannotConvert(portInt.toString, Port.getClass.toString, "Invalid port"))
  }
}
