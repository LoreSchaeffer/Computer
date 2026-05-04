; ==============================================================================
; TEST 3: Stack and Internal Bus Routing
; Verifies PHA/PLA instructions and TAX transfer to ensure the internal
; multiplexer routes data without corruption.
; ==============================================================================

* = $8000

    LDA #$42        ; Load magical number into A
    PHA             ; Push A ($42) to stack
    LDA #$00        ; Clear A to ensure we are actually pulling data
    PLA             ; Pull from stack into A (A should be $42 again)
    TAX             ; Transfer A to X (X should be $42)
    CPX #$42        ; Compare X with $42
    BNE FAIL        ; If not equal, branch to FAIL
SUCCESS:
    JMP SUCCESS     ; Infinite loop to signal success
FAIL:
    BRK             ; Halt execution