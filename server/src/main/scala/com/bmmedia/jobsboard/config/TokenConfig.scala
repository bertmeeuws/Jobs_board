package com.bmmedia.jobsboard.config

import pureconfig.ConfigReader
import scala.concurrent.duration.FiniteDuration
import pureconfig.generic.derivation.default.*

final case class TokenConfig(secretKey: String, jwtExpiryDuration: FiniteDuration)
    derives ConfigReader
