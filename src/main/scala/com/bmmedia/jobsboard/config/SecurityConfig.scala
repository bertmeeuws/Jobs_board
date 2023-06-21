package com.bmmedia.jobsboard.config

import pureconfig.generic.derivation.default.*
import pureconfig.ConfigReader

final case class SecurityConfig(secretKey: String, tokenExpiration: Int) derives ConfigReader
