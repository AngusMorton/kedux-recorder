package com.angusmorton.kedux.recorder

import com.angusmorton.kedux.Action
import com.angusmorton.kedux.State
import com.angusmorton.kedux.createReducer

internal val PLUS_ACTION = "com.angusmorton.kedux.recorder.getPLUS_ACTION"
internal val MINUS_ACTION = "com.angusmorton.kedux.recorder.getMINUS_ACTION"
internal val RESET_ACTION = "com.angusmorton.kedux.recorder.getRESET_ACTION"
internal val INIT_ACTION = "com.angusmorton.kedux.recorder.getINIT_ACTION"

internal data class TestState(val value: Int) : State

internal data class TestAction(val type: String, val by: Int = 0) : Action

internal val counterReducer = createReducer<TestState> { state, action ->
    if (action is TestAction) {
        when (action.type) {
            PLUS_ACTION -> state.copy(value = state.value + action.by)
            MINUS_ACTION -> state.copy(value = state.value - action.by)
            RESET_ACTION -> state.copy(value = 0)
            else -> state
        }
    } else {
        state
    }
}