package com.gaided.engine.api

import java.net.HttpURLConnection
import java.net.URL

public sealed class HttpApi protected constructor(
    baseUrl: String,
    protected val openConnection: ((URL) -> HttpURLConnection) = { url -> url.openConnection() as HttpURLConnection }
)