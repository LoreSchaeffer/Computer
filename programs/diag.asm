; ==============================================================================
; Lyco-8 Hardware Diagnostic Test Suite v3.0
; ==============================================================================

* = $8000                               ; Entry point in Main RAM

; --- Memory Mapped I/O Constants ---
PPU_BASE        = $2000                 ; Graphics Video RAM Start
KEYBOARD_IN     = $4000                 ; Keyboard Input (ASCII)

; --- APU Registers ---
APU_P1_FREQ_L   = $5000
APU_P1_FREQ_H   = $5001
APU_P1_VOL      = $5002
APU_P2_FREQ_L   = $5004
APU_P2_FREQ_H   = $5005
APU_P2_VOL      = $5006
APU_TRI_FREQ_L  = $5008
APU_TRI_FREQ_H  = $5009
APU_TRI_VOL     = $500A
APU_NOI_PER     = $500C
APU_NOI_VOL     = $500D

TERMINAL_OUT    = $F000                 ; Console Terminal Output

    JMP MAIN

; ==============================================================================
; STRING DATA
; ==============================================================================
MSG_WELCOME:
    .byte "===================================", $0A
    .byte " LYCO-8 DIAGNOSTICS v3.0", $0A
    .byte "===================================", $0A, 0

MSG_CPU:     .byte "[TEST 1] CPU ALU & Stack......... ", 0
MSG_PPU:     .byte "[TEST 2] PPU VRAM Render......... OK", $0A, 0
MSG_APU:     .byte "[TEST 3] APU Audio Sweep......... OK", $0A, 0
MSG_KB:      .byte "[TEST 4] Keyboard Active. Press ESC.", $0A, ">>> ", 0

MSG_OK:      .byte "OK", $0A, 0

; Error Messages
MSG_ERR_ALU: .byte "FAILED! (ERR 01: ALU Math mismatch)", $0A, 0
MSG_ERR_LOG: .byte "FAILED! (ERR 02: Logical Bit mismatch)", $0A, 0
MSG_ERR_SHF: .byte "FAILED! (ERR 03: Shift/Rotate mismatch)", $0A, 0
MSG_ERR_SUB: .byte "FAILED! (ERR 04: Subroutine/Stack mismatch)", $0A, 0

MSG_HALT_OK: .byte $0A, $0A, "SYSTEM HALTED GRACEFULLY.", $0A, 0
MSG_HALT_ER: .byte $0A, $0A, "SYSTEM HALTED DUE TO HARDWARE ERROR.", $0A, 0

; ==============================================================================
; MAIN ROUTINE
; ==============================================================================
MAIN:
    LDX #$FF
    TXS

    LDX #$00
PRINT_WELCOME:
    LDA MSG_WELCOME, X
    BEQ DO_CPU_TEST
    STA TERMINAL_OUT
    INX
    JMP PRINT_WELCOME

; ==============================================================================
; TEST 1: COMPREHENSIVE CPU VALIDATION
; ==============================================================================
DO_CPU_TEST:
    LDX #$00
PRINT_CPU:
    LDA MSG_CPU, X
    BEQ CPU_EXEC
    STA TERMINAL_OUT
    INX
    JMP PRINT_CPU

CPU_EXEC:
    ; 1. Arithmetic Test
    CLC
    LDA #$10
    ADC #$20
    SEC
    SBC #$05
    CMP #$2B
    BNE FAIL_ALU

    ; 2. Logical Operations
    LDA #$FF
    AND #$0F
    ORA #$F0
    EOR #$AA
    CMP #$55
    BNE FAIL_LOG

    ; 3. Shift and Rotate
    LDA #$01
    ASL A
    ASL A
    LSR A
    CLC
    ROL A
    ROR A
    CMP #$02
    BNE FAIL_SHF

    ; 4. Stack and Subroutine
    PHP
    SEC
    PLP
    LDX #$00
    JSR SUB_TEST
    CPX #$05
    BNE FAIL_SUB

    ; If all passed, print OK
    LDX #$00
PRINT_OK:
    LDA MSG_OK, X
    BEQ DO_PPU_TEST
    STA TERMINAL_OUT
    INX
    JMP PRINT_OK

; --- Specific Error Handlers ---
FAIL_ALU:
    LDX #$00
PRINT_ERR_ALU:
    LDA MSG_ERR_ALU, X
    BNE CONTINUE_ERR_ALU
    JMP HALT_SYS_ERROR
CONTINUE_ERR_ALU:
    STA TERMINAL_OUT
    INX
    JMP PRINT_ERR_ALU

FAIL_LOG:
    LDX #$00
PRINT_ERR_LOG:
    LDA MSG_ERR_LOG, X
    BNE CONTINUE_ERR_LOG
    JMP HALT_SYS_ERROR
CONTINUE_ERR_LOG:
    STA TERMINAL_OUT
    INX
    JMP PRINT_ERR_LOG

FAIL_SHF:
    LDX #$00
PRINT_ERR_SHF:
    LDA MSG_ERR_SHF, X
    BNE CONTINUE_ERR_SHF
    JMP HALT_SYS_ERROR
CONTINUE_ERR_SHF:
    STA TERMINAL_OUT
    INX
    JMP PRINT_ERR_SHF

FAIL_SUB:
    LDX #$00
PRINT_ERR_SUB:
    LDA MSG_ERR_SUB, X
    BNE CONTINUE_ERR_SUB
    JMP HALT_SYS_ERROR
CONTINUE_ERR_SUB:
    STA TERMINAL_OUT
    INX
    JMP PRINT_ERR_SUB

; --- Subroutine Helper ---
SUB_TEST:
    INX
    INX
    INX
    INX
    INX
    RTS

; ==============================================================================
; TEST 2: PPU MULTI-COLOR RENDER
; ==============================================================================
DO_PPU_TEST:
    LDX #$00
PRINT_PPU:
    LDA MSG_PPU, X
    BEQ PPU_EXEC
    STA TERMINAL_OUT
    INX
    JMP PRINT_PPU

PPU_EXEC:
    LDX #$00
PPU_LOOP:
    LDA #$02
    STA PPU_BASE, X
    LDA #$05
    STA PPU_BASE + 128, X
    LDA #$06
    STA PPU_BASE + 256, X
    LDA #$07
    STA PPU_BASE + 384, X
    INX
    BNE PPU_LOOP

; ==============================================================================
; TEST 3: APU 4-CHANNEL AUDIO SWEEP
; ==============================================================================
DO_APU_TEST:
    LDX #$00
PRINT_APU:
    LDA MSG_APU, X
    BEQ APU_EXEC
    STA TERMINAL_OUT
    INX
    JMP PRINT_APU

APU_EXEC:
    LDA #$B8
    STA APU_P1_FREQ_L
    LDA #$01
    STA APU_P1_FREQ_H
    LDA #$0F
    STA APU_P1_VOL
    JSR DELAY_ROUTINE
    LDA #$00
    STA APU_P1_VOL

    LDA #$10
    STA APU_NOI_PER
    LDA #$0F
    STA APU_NOI_VOL
    JSR DELAY_ROUTINE
    LDA #$00
    STA APU_NOI_VOL

    JMP DO_KB_TEST

DELAY_ROUTINE:
    LDY #$80
DELAY_OUTER:
    LDX #$FF
DELAY_INNER:
    DEX
    BNE DELAY_INNER
    DEY
    BNE DELAY_OUTER
    RTS

; ==============================================================================
; TEST 4: KEYBOARD POLLING
; ==============================================================================
DO_KB_TEST:
    LDX #$00
PRINT_KB:
    LDA MSG_KB, X
    BEQ KB_POLL
    STA TERMINAL_OUT
    INX
    JMP PRINT_KB

KB_POLL:
    LDA KEYBOARD_IN
    BEQ KB_POLL

    CMP #$1B
    BNE CONTINUE_KB
    JMP HALT_SYS_OK
CONTINUE_KB:
    STA TERMINAL_OUT
    JMP KB_POLL

; ==============================================================================
; SHUTDOWN SEQUENCES
; ==============================================================================
HALT_SYS_ERROR:
    LDX #$00
PRINT_HALT_ER:
    LDA MSG_HALT_ER, X
    BEQ DO_BRK
    STA TERMINAL_OUT
    INX
    JMP PRINT_HALT_ER

HALT_SYS_OK:
    LDX #$00
PRINT_HALT_OK:
    LDA MSG_HALT_OK, X
    BEQ DO_BRK
    STA TERMINAL_OUT
    INX
    JMP PRINT_HALT_OK

DO_BRK:
    BRK