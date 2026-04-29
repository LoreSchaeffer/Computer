; ========================================================
; TEST PROGRAM: Sum of N numbers
;
; This program calculates the sum of integers from 1 to N.
; N is stored in memory address $0000 (Zero Page).
; The final result is stored in $0001 (Zero Page).
;
; Example: If $0000 = $05 (5 decimal),
; result in $0001 will be $0F (15 decimal).
; ========================================================

* = $8000           ; Origin directive (Start assembling at $8000)

START:
    LDA #$05        ; Load Accumulator with the value N (e.g., 5)
    STA $00         ; Store N in Zero Page address $0000

    LDA #$00        ; Initialize Accumulator to 0 (This will hold the sum)
    LDX $00         ; Load X register with the value of N from memory

LOOP:
    CLC             ; Clear Carry Flag before addition (crucial for ADC)
    ADC $00         ; Add the current value of N (from memory $0000) to Accumulator

    DEC $00         ; Decrement the value of N in memory ($0000)
    DEX             ; Decrement X register (used as loop counter)

    BNE LOOP        ; Branch to LOOP if X is Not Equal to 0 (Z flag clear)

DONE:
    STA $01         ; Store the final sum in Zero Page address $0001

    ; The program enters an infinite loop to halt execution
HALT:
    JMP HALT        ; Jump to itself (infinite loop)