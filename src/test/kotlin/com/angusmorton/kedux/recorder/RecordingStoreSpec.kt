package com.angusmorton.kedux.recorder

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class RecordingStoreSpec : Spek() {
    init {
        val initAction = TestAction(INIT_ACTION)
        val initialState = TestState(0)

        given("a com.angusmorton.kedux.recorder.RecordingStore") {
            on("receiving an app action") {
                val recordingStore = RecordingStore.create(initAction, initialState, counterReducer)
                val action = TestAction(PLUS_ACTION, 1)
                println("before ${recordingStore.currentState()}")
                recordingStore.dispatch(action)
                println("after ${recordingStore.currentState()}")

                it("should have changed state") {
                    assertEquals(TestState(1), recordingStore.currentState())
                }
            }

            on("receiving an app action then a rollback") {
                val recordingStore = RecordingStore.create(initAction, initialState, counterReducer)
                val appAction = TestAction(PLUS_ACTION, 1)
                val devAction = RecordingAction.rollback()
                recordingStore.dispatch(appAction)
                recordingStore.dispatch(devAction)

                it("should be the initial state") {
                    assertEquals(TestState(0), recordingStore.currentState())
                }
            }

            on("subscribing then sending an app action") {
                val recordingStore = RecordingStore.create(initAction, initialState, counterReducer)
                val appAction = TestAction(PLUS_ACTION, 1)
                var state: TestState? = null;
                recordingStore.subscribe { state = it }
                recordingStore.dispatch(appAction)

                it("should call the subscriber with the correct state") {
                    assertEquals(TestState(1), state)
                }
            }
        }
    }
}