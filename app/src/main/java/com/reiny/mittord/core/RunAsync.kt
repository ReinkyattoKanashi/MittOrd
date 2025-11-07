package com.reiny.mittord.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface RunAsync {

    fun <T : Any> runAsync(scope: CoroutineScope, background: suspend () -> T, ui: (T) -> Unit)

    fun <T : Any> runFlow(
        scope: CoroutineScope,
        flow: Flow<T>,
        onEach: suspend (T) -> Unit
    )

    fun <T : Any> runFlowCollect(
        scope: CoroutineScope,
        flow: Flow<T>,
        collect: suspend (T) -> Unit
    )

    class Base @Inject constructor() : RunAsync {

        override fun <T : Any> runAsync(
            scope: CoroutineScope,
            background: suspend () -> T,
            ui: (T) -> Unit
        ) {
            scope.launch(Dispatchers.IO) {
                val result = background.invoke()
                withContext(Dispatchers.Main) {
                    ui.invoke(result)
                }
            }
        }

        override fun <T : Any> runFlow(
            scope: CoroutineScope,
            flow: Flow<T>,
            onEach: suspend (T) -> Unit
        ) {
            flow.onEach(onEach).flowOn(Dispatchers.IO).launchIn(scope)
        }

        override fun <T : Any> runFlowCollect(
            scope: CoroutineScope,
            flow: Flow<T>,
            collect: suspend (T) -> Unit
        ) {
            scope.launch {
                flow.collect(collect)
            }
        }
    }
}