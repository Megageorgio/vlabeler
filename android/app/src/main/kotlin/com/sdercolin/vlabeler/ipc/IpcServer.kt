package com.sdercolin.vlabeler.ipc

import com.sdercolin.vlabeler.env.Log
import com.sdercolin.vlabeler.ipc.request.IpcRequest
import com.sdercolin.vlabeler.ipc.response.IpcResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * IPC server for communication with UTAU plugins on desktop.
 * Android: not available (UTAU doesn't run on Android), so this is a no-op implementation.
 */
class IpcServer(val coroutineScope: CoroutineScope) {

    fun bind() {
        Log.debug("IpcServer is not available on Android")
    }

    fun startReceive(requestFlow: MutableSharedFlow<IpcRequest>) = Unit

    fun send(response: IpcResponse) = Unit

    fun close() = Unit
}
