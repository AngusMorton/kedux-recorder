package com.angusmorton.kedux.recorder

import com.angusmorton.kedux.Action

data class RecordingAction(val type: String,
                           val wrappedAction: Action? = null,
                           val index: Int? = null,
                           val isEnabled: Boolean? = null) : Action {
    companion object {
        internal val DEVTOOL_ACTION_PERFORM = "DEVTOOL_ACTION_PERFORM"
        internal val DEVTOOL_ACTION_JUMP_TO_STATE = "DEVTOOL_ACTION_JUMP_TO_STATE"
        internal val DEVTOOL_ACTION_RESET = "DEVTOOL_ACTION_RESET"
        internal val DEVTOOL_ACTION_COMMIT = "DEVTOOL_ACTION_COMMIT"
        internal val DEVTOOL_ACTION_ROLLBACK = "DEVTOOL_ACTION_ROLLBACK"
        internal val DEVTOOL_ACTION_TOGGLE = "DEVTOOL_ACTION_ENABLE"

        fun perform(action: Action): Action {
            return RecordingAction(DEVTOOL_ACTION_PERFORM, wrappedAction = action)
        }

        fun jump(index: Int): Action {
            return RecordingAction(DEVTOOL_ACTION_JUMP_TO_STATE, index = index)
        }

        fun reset(): Action {
            return RecordingAction(DEVTOOL_ACTION_RESET)
        }

        fun commit(): Action {
            return RecordingAction(DEVTOOL_ACTION_COMMIT)
        }

        fun rollback(): Action {
            return RecordingAction(DEVTOOL_ACTION_ROLLBACK)
        }

        fun toggle(index: Int): Action {
            return RecordingAction(DEVTOOL_ACTION_TOGGLE, index = index)
        }
    }
}