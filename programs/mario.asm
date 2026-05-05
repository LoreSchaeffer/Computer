; ==============================================================================
; Lyco-8 APU Integration Test: Super Mario Bros. Theme
; NMI-Driven 3-Channel Audio Sequencer (Polyrhythmic FSM)
; ==============================================================================

* = $8000               ; Execution begins at $8000 (Start of 32KB ROM)

; --- Memory Map I/O Constants ---
PPU_CTRL        = $3080
PPU_STATUS      = $3081

; --- APU Registers (Lyco-8 Custom Mapping) ---
APU_P1_FREQ_L   = $5000
APU_P1_FREQ_H   = $5001
APU_P1_VOL      = $5002

APU_P2_FREQ_L   = $5004
APU_P2_FREQ_H   = $5005
APU_P2_VOL      = $5006

APU_TRI_FREQ_L  = $5008
APU_TRI_FREQ_H  = $5009
APU_TRI_VOL     = $500A

; --- Zero Page Variables ---
; Channel 1 (Pulse 1 - Melody)
P1_TIMER        = $00   ; Frames remaining for the current note
P1_INDEX        = $01   ; Position in the sequence data array

; Channel 2 (Pulse 2 - Harmony)
P2_TIMER        = $02
P2_INDEX        = $03

; Channel 3 (Triangle - Bass)
TRI_TIMER       = $04
TRI_INDEX       = $05

; --- Musical Note Constants (Indices for the Frequency LUT) ---
REST    = $00
N_G2    = $01
N_C3    = $02
N_E3    = $03
N_F3    = $04
N_FS3   = $05
N_G3    = $06
N_B3    = $07
N_C4    = $08
N_D4    = $09
N_E4    = $0A
N_F4    = $0B
N_FS4   = $0C
N_G4    = $0D
N_GS4   = $0E
N_A4    = $0F
N_AS4   = $10
N_B4    = $11
N_C5    = $12
N_D5    = $13
N_E5    = $14
N_F5    = $15
N_FS5   = $16
N_G5    = $17
N_A5    = $18

CMD_END = $FF           ; End of song marker

; ==============================================================================
; RESET HANDLER: System Initialization
; ==============================================================================
RESET:
    SEI                 ; Disable software interrupts
    CLD                 ; Disable decimal math mode
    LDX #$FF
    TXS                 ; Initialize stack pointer to $01FF

    ; Initialize sequencer state variables to 0
    LDA #$00
    STA P1_TIMER
    STA P1_INDEX
    STA P2_TIMER
    STA P2_INDEX
    STA TRI_TIMER
    STA TRI_INDEX

    ; Silence all channels on boot
    STA APU_P1_VOL
    STA APU_P2_VOL
    STA APU_TRI_VOL

    ; Enable PPU VBlank NMI generation (Starts the 60Hz hardware clock)
    LDA #$80
    STA PPU_CTRL

; ==============================================================================
; MAIN EXECUTION LOOP
; ==============================================================================
MAIN_LOOP:
    JMP MAIN_LOOP       ; Yield execution to the NMI handler

; ==============================================================================
; NMI HANDLER: The 60Hz Music Sequencer Engine
; ==============================================================================
* = $8100
NMI:
    PHA                 ; Push A
    TXA
    PHA                 ; Push X
    TYA
    PHA                 ; Push Y

    LDA PPU_STATUS      ; Acknowledge VBlank to the hardware

    ; Process each audio channel independently via Finite State Machines
    JSR PROCESS_PULSE_1
    JSR PROCESS_PULSE_2
    JSR PROCESS_TRIANGLE

    PLA                 ; Pull Y
    TAY
    PLA                 ; Pull X
    TAX
    PLA                 ; Pull A
    RTI                 ; Return from Interrupt

; ==============================================================================
; SUBROUTINE: Pulse 1 Engine (Melody)
; ==============================================================================
PROCESS_PULSE_1:
    LDA P1_TIMER
    BEQ P1_FETCH_NEXT   ; If timer is 0, fetch the next note
    DEC P1_TIMER        ; Otherwise, decrement timer and exit
    RTS

P1_FETCH_NEXT:
    LDX P1_INDEX
    LDA TRACK_PULSE_1, X ; Read Pitch

    CMP #CMD_END
    BNE P1_PLAY_NOTE

    ; Loop the song if CMD_END is reached
    LDA #$00
    STA P1_INDEX
    JMP P1_FETCH_NEXT

P1_PLAY_NOTE:
    CMP #REST
    BNE P1_SOUND_ON

    ; Note is a REST: Mute volume
    LDA #$00
    STA APU_P1_VOL
    JMP P1_READ_DURATION

P1_SOUND_ON:
    ; Lookup frequency from table using Pitch index
    TAY
    LDA FREQ_LUT_L, Y
    STA APU_P1_FREQ_L
    LDA FREQ_LUT_H, Y
    STA APU_P1_FREQ_H

    ; Set max volume for Pulse 1
    LDA #$0F
    STA APU_P1_VOL

P1_READ_DURATION:
    ; Read the duration byte and store it in the timer
    INX
    LDA TRACK_PULSE_1, X
    STA P1_TIMER

    ; Advance sequence pointer for the next frame
    INX
    STX P1_INDEX
    RTS

; ==============================================================================
; SUBROUTINE: Pulse 2 Engine (Harmony)
; ==============================================================================
PROCESS_PULSE_2:
    LDA P2_TIMER
    BEQ P2_FETCH_NEXT
    DEC P2_TIMER
    RTS

P2_FETCH_NEXT:
    LDX P2_INDEX
    LDA TRACK_PULSE_2, X

    CMP #CMD_END
    BNE P2_PLAY_NOTE

    LDA #$00
    STA P2_INDEX
    JMP P2_FETCH_NEXT

P2_PLAY_NOTE:
    CMP #REST
    BNE P2_SOUND_ON

    LDA #$00
    STA APU_P2_VOL
    JMP P2_READ_DURATION

P2_SOUND_ON:
    TAY
    LDA FREQ_LUT_L, Y
    STA APU_P2_FREQ_L
    LDA FREQ_LUT_H, Y
    STA APU_P2_FREQ_H

    ; Slightly lower volume for harmony
    LDA #$08
    STA APU_P2_VOL

P2_READ_DURATION:
    INX
    LDA TRACK_PULSE_2, X
    STA P2_TIMER

    INX
    STX P2_INDEX
    RTS

; ==============================================================================
; SUBROUTINE: Triangle Engine (Bass)
; ==============================================================================
PROCESS_TRIANGLE:
    LDA TRI_TIMER
    BEQ TRI_FETCH_NEXT
    DEC TRI_TIMER
    RTS

TRI_FETCH_NEXT:
    LDX TRI_INDEX
    LDA TRACK_TRIANGLE, X

    CMP #CMD_END
    BNE TRI_PLAY_NOTE

    LDA #$00
    STA TRI_INDEX
    JMP TRI_FETCH_NEXT

TRI_PLAY_NOTE:
    CMP #REST
    BNE TRI_SOUND_ON

    LDA #$00
    STA APU_TRI_VOL
    JMP TRI_READ_DURATION

TRI_SOUND_ON:
    TAY
    LDA FREQ_LUT_L, Y
    STA APU_TRI_FREQ_L
    LDA FREQ_LUT_H, Y
    STA APU_TRI_FREQ_H

    ; Triangle plays at constant volume in typical hardware
    LDA #$0F
    STA APU_TRI_VOL

TRI_READ_DURATION:
    INX
    LDA TRACK_TRIANGLE, X
    STA TRI_TIMER

    INX
    STX TRI_INDEX
    RTS

; ==============================================================================
; DATA: Frequency Lookup Table (NTSC Approx Periods for 1.789 MHz)
; ==============================================================================
FREQ_LUT_L:
    .BYTE $00   ; 00: REST
    .BYTE $74   ; 01: G2
    .BYTE $56   ; 02: C3
    .BYTE $A5   ; 03: E3
    .BYTE $7F   ; 04: F3
    .BYTE $5B   ; 05: FS3
    .BYTE $39   ; 06: G3
    .BYTE $C3   ; 07: B3
    .BYTE $AB   ; 08: C4
    .BYTE $7B   ; 09: D4
    .BYTE $52   ; 0A: E4
    .BYTE $3F   ; 0B: F4
    .BYTE $2D   ; 0C: FS4
    .BYTE $1C   ; 0D: G4
    .BYTE $0C   ; 0E: GS4
    .BYTE $FD   ; 0F: A4
    .BYTE $EE   ; 10: AS4
    .BYTE $E1   ; 11: B4
    .BYTE $D4   ; 12: C5
    .BYTE $BD   ; 13: D5
    .BYTE $A8   ; 14: E5
    .BYTE $9F   ; 15: F5
    .BYTE $96   ; 16: FS5
    .BYTE $8D   ; 17: G5
    .BYTE $7E   ; 18: A5

FREQ_LUT_H:
    .BYTE $00   ; 00: REST
    .BYTE $04   ; 01: G2
    .BYTE $03   ; 02: C3
    .BYTE $02   ; 03: E3
    .BYTE $02   ; 04: F3
    .BYTE $02   ; 05: FS3
    .BYTE $02   ; 06: G3
    .BYTE $01   ; 07: B3
    .BYTE $01   ; 08: C4
    .BYTE $01   ; 09: D4
    .BYTE $01   ; 0A: E4
    .BYTE $01   ; 0B: F4
    .BYTE $01   ; 0C: FS4
    .BYTE $01   ; 0D: G4
    .BYTE $01   ; 0E: GS4
    .BYTE $00   ; 0F: A4
    .BYTE $00   ; 10: AS4
    .BYTE $00   ; 11: B4
    .BYTE $00   ; 12: C5
    .BYTE $00   ; 13: D5
    .BYTE $00   ; 14: E5
    .BYTE $00   ; 15: F5
    .BYTE $00   ; 16: FS5
    .BYTE $00   ; 17: G5
    .BYTE $00   ; 18: A5

; ==============================================================================
; DATA: Musical Tracks (Format: PITCH, DURATION_IN_FRAMES)
; 1 frame = ~16.6ms. Base tempo unit is 2 frames.
; ==============================================================================

; --- CHANNEL 1: MELODY ---
TRACK_PULSE_1:
    ; Intro (120 frames)
    .BYTE N_E5, 6, REST, 2
    .BYTE N_E5, 6, REST, 10
    .BYTE N_E5, 6, REST, 2
    .BYTE N_C5, 6, REST, 2
    .BYTE N_E5, 6, REST, 10
    .BYTE N_G5, 6, REST, 26
    .BYTE N_G4, 6, REST, 26

    ; Part A (214 frames)
    .BYTE N_C5, 12, REST, 6
    .BYTE N_G4, 12, REST, 6
    .BYTE N_E4, 12, REST, 10
    .BYTE N_A4, 6,  REST, 2
    .BYTE N_B4, 6,  REST, 2
    .BYTE N_AS4,6,  REST, 2
    .BYTE N_A4, 6,  REST, 6
    .BYTE N_G4, 8,  REST, 2
    .BYTE N_E5, 8,  REST, 2
    .BYTE N_G5, 8,  REST, 2
    .BYTE N_A5, 12, REST, 2
    .BYTE N_F5, 6,  REST, 2
    .BYTE N_G5, 6,  REST, 6
    .BYTE N_E5, 12, REST, 2
    .BYTE N_C5, 6,  REST, 2
    .BYTE N_D5, 6,  REST, 2
    .BYTE N_B4, 12, REST, 14
    .BYTE CMD_END

; --- CHANNEL 2: HARMONY ---
TRACK_PULSE_2:
    ; Intro (120 frames)
    .BYTE N_FS4, 6, REST, 2
    .BYTE N_FS4, 6, REST, 10
    .BYTE N_FS4, 6, REST, 2
    .BYTE N_FS4, 6, REST, 2
    .BYTE N_FS4, 6, REST, 10
    .BYTE N_B4,  6, REST, 26
    .BYTE N_G4,  6, REST, 26

    ; Part A (214 frames)
    .BYTE N_E4, 12, REST, 6
    .BYTE N_C4, 12, REST, 6
    .BYTE N_G3, 12, REST, 10
    .BYTE N_F4, 6,  REST, 2
    .BYTE N_G4, 6,  REST, 2
    .BYTE N_FS4,6,  REST, 2
    .BYTE N_F4, 6,  REST, 6
    .BYTE N_E4, 8,  REST, 2
    .BYTE N_C5, 8,  REST, 2
    .BYTE N_E5, 8,  REST, 2
    .BYTE N_F5, 12, REST, 2
    .BYTE N_D5, 6,  REST, 2
    .BYTE N_E5, 6,  REST, 6
    .BYTE N_C5, 12, REST, 2
    .BYTE N_A4, 6,  REST, 2
    .BYTE N_B4, 6,  REST, 2
    .BYTE N_G4, 12, REST, 14
    .BYTE CMD_END

; --- CHANNEL 3: BASS ---
TRACK_TRIANGLE:
    ; Intro (120 frames)
    .BYTE N_D4, 6, REST, 2
    .BYTE N_D4, 6, REST, 10
    .BYTE N_D4, 6, REST, 2
    .BYTE N_D4, 6, REST, 2
    .BYTE N_D4, 6, REST, 10
    .BYTE N_G3, 6, REST, 26
    .BYTE N_G2, 6, REST, 26

    ; Part A (214 frames)
    .BYTE N_G3, 12, REST, 6
    .BYTE N_C4, 12, REST, 6
    .BYTE N_G3, 12, REST, 10
    .BYTE N_F3, 6,  REST, 2
    .BYTE N_G3, 6,  REST, 2
    .BYTE N_FS3,6,  REST, 2
    .BYTE N_F3, 6,  REST, 6
    .BYTE N_C4, 8,  REST, 2
    .BYTE N_G3, 8,  REST, 2
    .BYTE N_C4, 8,  REST, 2
    .BYTE N_F4, 12, REST, 2
    .BYTE N_C4, 6,  REST, 2
    .BYTE N_C4, 6,  REST, 6
    .BYTE N_G3, 12, REST, 2
    .BYTE N_F3, 6,  REST, 2
    .BYTE N_G3, 6,  REST, 2
    .BYTE N_C4, 12, REST, 14
    .BYTE CMD_END

; ==============================================================================
; HARDWARE VECTORS
; ==============================================================================
* = $FFFA
    .BYTE $00, $81      ; NMI Vector ($8100)
    .BYTE $00, $80      ; Reset Vector ($8000)
    .BYTE $00, $80      ; IRQ/BRK Vector ($8000)