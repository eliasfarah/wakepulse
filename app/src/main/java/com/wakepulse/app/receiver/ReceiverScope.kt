package com.wakepulse.app.receiver

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal object ReceiverScope {
    val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
