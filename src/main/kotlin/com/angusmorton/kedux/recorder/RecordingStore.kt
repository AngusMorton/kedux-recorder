package com.angusmorton.kedux.recorder

import com.angusmorton.kedux.Action
import com.angusmorton.kedux.Store
import com.angusmorton.kedux.State
import com.angusmorton.kedux.Subscription

/**
 * The com.angusmorton.kedux.recorder.RecordingStore is a store that keeps the history of your application.
 *
 * You will interact with this by dispatching [com.angusmorton.kedux.recorder.RecordingActions] directly to the store. e.g.
 *
 */
class RecordingStore<S : State> : Store<S> {
    /*
     * The RecordingStore works by lifting the original store the delegating directly to the lifted store.
     */

    private val liftedStore: Store<RecordingState<S>>

    override fun currentState(): S {
        return liftedStore.currentState().computedStates[liftedStore.currentState().currentStateIndex]
    }

    override fun dispatch(action: Action): Action {
        // If the Action is not a com.angusmorton.kedux.recorder.RecordingAction then we wrap it and assume it's being performed.
        // Otherwise we pass it on as normal.
        var actionToDispatch = action
        if (action !is RecordingAction) actionToDispatch = RecordingAction.perform(action)
        return liftedStore.dispatch(actionToDispatch)
    }

    override fun subscribe(subscriber: (S) -> Unit): Subscription {
        return liftedStore.subscribe { subscriber(currentState()) }
    }

    internal constructor(initAction: Action, initialState: S, reducer: (S, Action) -> S) {
        val liftedReducer: (RecordingState<S>, Action) -> RecordingState<S> = createRecordingReducer(initialState, initAction, reducer)
        val liftedState = RecordingState(committedState = initialState, stagedActions = listOf(initAction), computedStates = listOf(initialState))
        this.liftedStore = Store.create(liftedState, liftedReducer)
    }

    companion object {
        /**
         * Creates and returns a Store implementation that will record all actions that pass through it and provide options to replay/load/save/time travel.
         */
        fun <S : State> create(initAction: Action, initialState: S, reducer: (S, Action) -> S): Store<S> {
            return RecordingStore(initAction, initialState, reducer)
        }
    }
}