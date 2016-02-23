package com.angusmorton.kedux.recorder

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordingReducerSpecs : Spek() {

    init {
        val recordingReducer = createRecordingReducer(TestState(0), TestAction(INIT_ACTION), counterReducer)

        given("a RecordingReducer with an initial state and no history of actions") {
            val initialState = RecordingState(TestState(0), emptyList(), emptyList(), 0, emptySet())

            on("receiving a single perform action") {
                val action = TestAction(PLUS_ACTION, 1)
                val recordingAction = RecordingAction.perform(action)
                val resultState = recordingReducer(initialState, recordingAction)

                it("should include the action in it's list of staged actions") {
                    assertTrue("resulting state did not contain the action") { resultState.stagedActions.contains(action) }
                }

                it("should set the current state pointer to 0") {
                    assertEquals(0, resultState.currentStateIndex)
                }

                it("should compute the correct unlifted state") {
                    assertEquals(TestState(1), resultState.computedStates[0])
                }
            }

            on("receiving three perform actions") {
                val action = TestAction(PLUS_ACTION, 1)
                val recordingAction = RecordingAction.perform(action)
                var resultState = recordingReducer(initialState, recordingAction)
                resultState = recordingReducer(resultState, recordingAction)
                resultState = recordingReducer(resultState, recordingAction)

                it("should have three staged actions") {
                    assertEquals(3, resultState.stagedActions.size, "result state did not have 3 staged actions")
                }

                it("should set the current state pointer to 2") {
                    assertEquals(2, resultState.currentStateIndex)
                }

                it("should compute the correct unlifted state for the last action") {
                    assertEquals(TestState(3), resultState.computedStates[2])
                }
            }
        }

        given("a RecordingReducer with an initial state with a history of actions") {
            val initialState = RecordingState(
                    TestState(2),
                    listOf(TestState(2), TestState(3), TestState(4)),
                    listOf(TestAction(INIT_ACTION), TestAction(PLUS_ACTION, 1), TestAction(PLUS_ACTION, 1)),
                    2,
                    emptySet())

            on("receiving a rollback action") {
                val recordingAction = RecordingAction.rollback()
                val resultState = recordingReducer(initialState, recordingAction)

                it ("should clear all staged actions, except for the init action") {
                    assertEquals(1, resultState.stagedActions.size)
                    assertEquals(TestAction(INIT_ACTION), resultState.stagedActions[0])
                }

                it ("should clear all computed states, except for the last committed state") {
                    assertEquals(1, resultState.computedStates.size)
                    assertEquals(resultState.committedState, resultState.computedStates[0])
                }

                it ("should set the selected index to 0") {
                    assertEquals(0, resultState.currentStateIndex)
                }
            }

            on("receiving a reset action") {
                val recordingAction = RecordingAction.reset()
                val resultState = recordingReducer(initialState, recordingAction)

                it ("should clear all staged actions, except for the init action") {
                    assertEquals(1, resultState.stagedActions.size)
                    assertEquals(TestAction(INIT_ACTION), resultState.stagedActions[0])
                }

                it ("should reset the committed state to the initial state") {
                    assertEquals(TestState(0), resultState.committedState)
                }

                it ("should set the selected index to 0") { assertEquals(0, resultState.currentStateIndex) }
            }

            on("receiving a commit action") {
                val recordingAction = RecordingAction.commit()
                val resultState = recordingReducer(initialState, recordingAction)

                it ("should clear all staged actions, except for the init action") {
                    assertEquals(1, resultState.stagedActions.size)
                    assertEquals(TestAction(INIT_ACTION), resultState.stagedActions[0])
                }

                it ("should clear all computed states, except for the new committed state") {
                    assertEquals(1, resultState.computedStates.size)
                    assertEquals(resultState.committedState, resultState.computedStates[0])
                }

                it ("should update the committed state") {
                    assertEquals(TestState(4), resultState.committedState)
                }
            }

            on("receiving a jump action") {
                val recordingAction = RecordingAction.jump(1)
                val resultState = recordingReducer(initialState, recordingAction)

                it ("should set the current state index correctly") {
                    assertEquals(1, resultState.currentStateIndex)
                }
            }

            on("receiving a toggle action for an action that is currently active") {
                val recordingAction = RecordingAction.toggle(1)
                val resultState = recordingReducer(initialState, recordingAction)

                it("should not apply the action at that index") {
                    assertEquals(TestState(3), resultState.computedStates[2])
                }

                it("should contain the same number of states") {
                    assertEquals(3, resultState.computedStates.size)
                }
            }
        }
    }
}
