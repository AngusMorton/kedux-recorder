package com.angusmorton.kedux.recorder

import com.angusmorton.kedux.Action
import com.angusmorton.kedux.State

internal data class RecordingState<S : State>(
        internal val committedState: S, // The last known committed state.
        internal val computedStates: List<S> = emptyList(), // List of computed states after committed.
        internal val stagedActions: List<Action> = emptyList(), // List of all currently staged actions.
        internal val currentStateIndex: Int = 0, // Current state index in the computedStates List.
        internal val skippedActionIndexes: Set<Int> = emptySet() // Set of actio\n indexes that should be skipped.
) : State