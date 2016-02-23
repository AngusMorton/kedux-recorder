package com.angusmorton.kedux.recorder

import com.angusmorton.kedux.Action
import com.angusmorton.kedux.State

/**
 * Lifts a reducer to create a recording reducer.
 */
internal inline fun <S : State> createRecordingReducer(initialState: S, initAction: Action, crossinline liftedReducer: (S, Action) -> S): (RecordingState<S>, Action) -> RecordingState<S> = {
    recordingState, recordingAction ->

    if (recordingAction !is RecordingAction) throw IllegalArgumentException("recordingAction must be a com.angusmorton.kedux.recorder.RecordingAction")

    var committedState = recordingState.committedState
    var currentStateIndex = recordingState.currentStateIndex
    var stagedActions = recordingState.stagedActions
    var skippedActionsIndexes = recordingState.skippedActionIndexes

    when (recordingAction.type) {
        RecordingAction.DEVTOOL_ACTION_PERFORM -> {
            if (recordingAction.wrappedAction == null) {
                throw NullPointerException("recordingAction.wrappedAction == null")
            }

            if (currentStateIndex == stagedActions.size - 1) {
                currentStateIndex += 1
            }

            stagedActions = stagedActions.plus(recordingAction.wrappedAction)
        }

        RecordingAction.DEVTOOL_ACTION_RESET -> {
            committedState = initialState
            stagedActions = listOf(initAction)
            skippedActionsIndexes = emptySet()
            currentStateIndex = 0
        }

        RecordingAction.DEVTOOL_ACTION_COMMIT -> {
            committedState = recordingState.computedStates[currentStateIndex]
            stagedActions = listOf(initAction)
            skippedActionsIndexes = emptySet()
            currentStateIndex = 0
        }

        RecordingAction.DEVTOOL_ACTION_ROLLBACK -> {
            stagedActions = listOf(initAction)
            skippedActionsIndexes = emptySet()
            currentStateIndex = 0
        }

        RecordingAction.DEVTOOL_ACTION_JUMP_TO_STATE -> {
            currentStateIndex = recordingAction.index ?: currentStateIndex
        }

        RecordingAction.DEVTOOL_ACTION_TOGGLE -> {
            if (recordingAction.index == null) throw IllegalArgumentException("recordingAction.index == null ($recordingAction)");

            if (skippedActionsIndexes.contains(recordingAction.index)) {
                skippedActionsIndexes = skippedActionsIndexes.minus(recordingAction.index)
            } else {
                skippedActionsIndexes = skippedActionsIndexes.plus(recordingAction.index)
            }
        }

        else -> throw IllegalArgumentException("Unhandled action: $recordingAction")
    }

    // Recompute the state from scratch
    // TODO: Optimize and only recompute what is necessary
    val computedStates = mutableListOf<S>()
    var currentState = committedState
    stagedActions.forEachIndexed { i, action ->
        if (!skippedActionsIndexes.contains(i)) {
            currentState = liftedReducer(currentState, action)
        }
        computedStates.add(currentState)
    }

    RecordingState(committedState, computedStates, stagedActions, currentStateIndex, skippedActionsIndexes)
}
