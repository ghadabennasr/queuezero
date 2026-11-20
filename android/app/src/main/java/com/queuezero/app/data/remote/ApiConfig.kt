package com.queuezero.app.data.remote

/**
 * Backend address.
 * 10.0.2.2 is how the Android emulator reaches the development PC.
 * On a physical phone, replace it with the PC's LAN IP (and allow that IP in network_security_config.xml).
 */
object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8081/"
}
