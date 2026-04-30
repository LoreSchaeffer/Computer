; ==============================================================================
; Lycoris-8 Hardware Test: APU Polyphonic Synthesizer
; Theme: Super Mario Bros. (Overworld)
; ==============================================================================

* = $8000

; --- APU Memory Map Constants (Pulse 1 Channel) ---
PULSE1_FREQ_LO = $5000
PULSE1_FREQ_HI = $5001
PULSE1_VOL     = $5002

; --- Note Frequencies (16-bit Hex) ---
; E5: 659.25 Hz -> $0293
NOTE_E5_HI = $02
NOTE_E5_LO = $93

; C5: 523.25 Hz -> $020B
NOTE_C5_HI = $02
NOTE_C5_LO = $0B

; G5: 783.99 Hz -> $030F
NOTE_G5_HI = $03
NOTE_G5_LO = $0F

; G4: 392.00 Hz -> $0188
NOTE_G4_HI = $01
NOTE_G4_LO = $88

; --- Volume Constants ---
VOL_MAX = $0A
VOL_OFF = $00

; ==============================================================================
; BOOT SEQUENCE
; ==============================================================================
INIT:
    ; Note 1: E5
    LDX #NOTE_E5_HI
    LDY #NOTE_E5_LO
    JSR PLAY_NOTE
    JSR DELAY_SHORT
    JSR MUTE_NOTE

    ; Note 2: E5
    LDX #NOTE_E5_HI
    LDY #NOTE_E5_LO
    JSR PLAY_NOTE
    JSR DELAY_SHORT
    JSR MUTE_NOTE
    JSR DELAY_SHORT       ; Rest

    ; Note 3: E5
    LDX #NOTE_E5_HI
    LDY #NOTE_E5_LO
    JSR PLAY_NOTE
    JSR DELAY_SHORT
    JSR MUTE_NOTE

    ; Note 4: C5
    LDX #NOTE_C5_HI
    LDY #NOTE_C5_LO
    JSR PLAY_NOTE
    JSR DELAY_SHORT
    JSR MUTE_NOTE

    ; Note 5: E5
    LDX #NOTE_E5_HI
    LDY #NOTE_E5_LO
    JSR PLAY_NOTE
    JSR DELAY_SHORT
    JSR MUTE_NOTE
    JSR DELAY_SHORT       ; Rest

    ; Note 6: G5
    LDX #NOTE_G5_HI
    LDY #NOTE_G5_LO
    JSR PLAY_NOTE
    JSR DELAY_LONG
    JSR MUTE_NOTE
    JSR DELAY_LONG        ; Rest

    ; Note 7: G4
    LDX #NOTE_G4_HI
    LDY #NOTE_G4_LO
    JSR PLAY_NOTE
    JSR DELAY_LONG
    JSR MUTE_NOTE

HALT_SYSTEM:
    JMP HALT_SYSTEM       ; Infinite loop to end execution

; ==============================================================================
; SUBROUTINES
; ==============================================================================

; ------------------------------------------------------------------------------
; PLAY_NOTE: Activates Pulse 1 with frequency passed in X (High) and Y (Low)
; ------------------------------------------------------------------------------
PLAY_NOTE:
    STY PULSE1_FREQ_LO    ; Store Low Byte
    STX PULSE1_FREQ_HI    ; Store High Byte
    LDA #VOL_MAX
    STA PULSE1_VOL        ; Set Volume to max to start the oscillator
    RTS

; ------------------------------------------------------------------------------
; MUTE_NOTE: Silences Pulse 1 to create separation between notes
; ------------------------------------------------------------------------------
MUTE_NOTE:
    LDA #VOL_OFF
    STA PULSE1_VOL
    JSR DELAY_TINY        ; Tiny delay to prevent audio popping
    RTS

; ------------------------------------------------------------------------------
; DELAYS: Nested loop burn-cycle algorithms to hold notes.
; Context saving: We push A, X, Y to the stack to preserve caller state.
; ------------------------------------------------------------------------------
DELAY_LONG:
    PHA                   ; Save Accumulator
    TXA
    PHA                   ; Save X
    TYA
    PHA                   ; Save Y
    LDX #$B0              ; Outer loop counter (Long)
    JMP DELAY_EXEC

DELAY_SHORT:
    PHA
    TXA
    PHA
    TYA
    PHA
    LDX #$40              ; Outer loop counter (Short)
    JMP DELAY_EXEC

DELAY_TINY:
    PHA
    TXA
    PHA
    TYA
    PHA
    LDX #$10              ; Outer loop counter (Tiny)

DELAY_EXEC:
    LDY #$FF              ; Inner loop counter
DELAY_INNER:
    DEY
    BNE DELAY_INNER       ; Branch until Y = 0
    DEX
    BNE DELAY_EXEC        ; Branch until X = 0

    PLA                   ; Restore Y
    TAY
    PLA                   ; Restore X
    TAX
    PLA                   ; Restore Accumulator
    RTS