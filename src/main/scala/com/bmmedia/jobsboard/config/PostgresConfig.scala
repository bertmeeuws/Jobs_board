package com.bmmedia.jobsboard.config

import pureconfig.generic.derivation.default.*
import pureconfig.ConfigReader

final case class PostgresConfig(
    driver: String,
    url: String,
    username: String,
    password: String,
    threads: Int
) derives ConfigReader
