package com.bmmedia.jobsboard.config

import pureconfig.generic.derivation.default.*
import pureconfig.ConfigReader

final case class AppConfig(
    postgresConfig: PostgresConfig,
    emberConfig: EmberConfig
) derives ConfigReader
