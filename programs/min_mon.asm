.setcpu "6502"
.feature labels_without_colons
.feature loose_char_term

; minimal monitor for EhBASIC and 6502 simulator V1.05 tailored for Lyco-8

.segment "CODE"
.include "basic.asm"

; --- Memory Mapped I/O ---
KEYBOARD_IN     = $4000
PPU_NAMETABLE   = $3000
PPU_CTRL        = $3300
VFS_COMMAND     = $4100         ; Virtual File System interface

; --- Zero Page Variables (Guaranteed free by EhBASIC) ---
CURSOR_POINTER  = $E0
BLINK_COUNTER   = $E2           ; Timer for blinking cursor
CURSOR_STATE    = $E3           ; 0 = off, 1 = on
CHAR_UNDER_CUR  = $E4           ; Memory for the character covered by the cursor
CHAR_TO_PRINT   = $E5           ; Safe backup for Accumulator
SAVE_X          = $E6           ; Safe backup for X register
SAVE_Y          = $E7           ; Safe backup for Y register

; --- ASCII Constants ---
ASCII_BS        = $08           ; Backspace (Sent by EhBASIC)
ASCII_CR        = $0D           ; Carriage Return
ASCII_LF        = $0A           ; Line Feed
ASCII_PROMPT    = $3E           ; ">"
ASCII_SPACE     = $20           ; " "
ASCII_CURSOR    = $5F           ; "_" (Cursor symbol)

.segment "BIOS"

RES_vec:
    CLD
    LDX #$FF
    TXS

    ; --- LYCO-8 HARDWARE INITIALIZATION ---
    JSR CLEAR_SCREEN
    LDA #$00
    STA CURSOR_POINTER
    STA BLINK_COUNTER
    STA CURSOR_STATE
    LDA #$30
    STA CURSOR_POINTER + 1
    LDA #ASCII_SPACE
    STA CHAR_UNDER_CUR

    LDA #$80
    STA PPU_CTRL
    ; ----------------------------------------

    ; VITAL: Copy only the 4 pointers, NOT the entire code block!
    LDY #END_VECS-LAB_vec
LAB_stlp:
    LDA LAB_vec-1,Y
    STA VEC_IN-1,Y
    DEY
    BNE LAB_stlp

LAB_signon:
    LDA LAB_mess,Y
    BEQ LAB_nokey
    JSR V_OUTP
    INY
    BNE LAB_signon

LAB_nokey:
    JSR V_INPT
    BCC LAB_nokey

    AND #$DF            ; Force uppercase
    CMP #'W'
    BEQ LAB_dowarm

    CMP #'C'
    BNE RES_vec

    JMP LAB_COLD

LAB_dowarm:
    JMP LAB_WARM

; ==============================================================================
; LYCO-8 HARDWARE ABSTRACTION LAYER (HAL)
; ==============================================================================

; --- SCREEN OUTPUT ---
ACIAout:
    ; Bulletproof Zero Page backup
    STA CHAR_TO_PRINT
    STX SAVE_X
    STY SAVE_Y

    ; Temporarily turn off cursor before printing
    JSR ERASE_CURSOR

    LDA CHAR_TO_PRINT
    CMP #ASCII_CR       ; Ignore CR
    BEQ @SKIP
    CMP #ASCII_BS       ; Intercept Backspace
    BEQ @BACKSPACE

    JSR PRINT_CHAR      ; Otherwise, print normally
    JMP @SKIP

@BACKSPACE:
    JSR HANDLE_BACKSPACE

@SKIP:
    ; Bulletproof restore from Zero Page
    LDX SAVE_X
    LDY SAVE_Y
    LDA CHAR_TO_PRINT
    RTS

; --- KEYBOARD INPUT ---
ACIAin:
    LDA KEYBOARD_IN
    BEQ LAB_nobyw
    SEC
    RTS

LAB_nobyw:
    CLC
    RTS

; --- VFS HOST CALLS ---
DO_LOAD:
    LDA #$01            ; 0x01 = Comando LOAD per Java
    STA VFS_COMMAND     ; La CPU va in pausa qui finché non chiudi il File Selector!

    ; Handshake: Legge la risposta di Java
    LDA VFS_COMMAND
    CMP #$FF            ; Se Java ha risposto con FF, ha iniettato un binario e serve un riavvio!
    BEQ LAB_dowarm      ; Salta al Warm Start (riavvia l'OS senza cancellare la RAM)

    CLC                 ; Altrimenti, se era testo, torna normalmente al BASIC
    RTS

DO_SAVE:
    LDA #$02
    STA VFS_COMMAND
    CLC
    RTS

; ==============================================================================
; LYCO-8 VIDEO DRIVER
; ==============================================================================

ERASE_CURSOR:
    LDA CURSOR_STATE
    BEQ @DONE
    LDA #$00
    STA CURSOR_STATE
    STA BLINK_COUNTER       ; Reset timer
    LDY #$00
    LDA CHAR_UNDER_CUR
    STA (CURSOR_POINTER), Y ; Restore original character
@DONE:
    RTS

HANDLE_BACKSPACE:
    ; Prevent erasing past the start of VRAM ($3000)
    LDA CURSOR_POINTER + 1
    CMP #$30
    BNE @DO_BACKSPACE
    LDA CURSOR_POINTER
    BEQ @DONE

@DO_BACKSPACE:
    ; Retreat pointer by 1 step (16-bit)
    LDA CURSOR_POINTER
    BNE @NO_WRAP
    DEC CURSOR_POINTER + 1
@NO_WRAP:
    DEC CURSOR_POINTER

    ; Overwrite old character with whitespace
    LDA #ASCII_SPACE
    LDY #$00
    STA (CURSOR_POINTER), Y
@DONE:
    RTS

CLEAR_SCREEN:
    LDA #ASCII_SPACE
    LDY #$00
@CLEAR_LOOP:
    STA PPU_NAMETABLE, Y
    STA PPU_NAMETABLE + $0100, Y
    STA PPU_NAMETABLE + $0200, Y
    INY
    BNE @CLEAR_LOOP
    RTS

PRINT_CHAR:
    CMP #ASCII_LF
    BEQ HANDLE_LF

    LDY #$00
    STA (CURSOR_POINTER), Y
    INC CURSOR_POINTER
    BNE CHECK_WRAP
    INC CURSOR_POINTER + 1

CHECK_WRAP:
    LDA CURSOR_POINTER + 1
    CMP #$33
    BNE EXIT_PRINT
    JSR SCROLL_UP
    LDA #$E0
    STA CURSOR_POINTER
    LDA #$32
    STA CURSOR_POINTER + 1
EXIT_PRINT:
    RTS

HANDLE_LF:
    LDA CURSOR_POINTER
    AND #$E0
    CLC
    ADC #$20
    STA CURSOR_POINTER
    BCC CHECK_WRAP
    INC CURSOR_POINTER + 1
    JMP CHECK_WRAP

SCROLL_UP:
    LDY #$00
@COPY_BLOCK_1:
    LDA $3020, Y
    STA $3000, Y
    INY
    BNE @COPY_BLOCK_1
@COPY_BLOCK_2:
    LDA $3120, Y
    STA $3100, Y
    INY
    BNE @COPY_BLOCK_2
@COPY_BLOCK_3:
    LDA $3220, Y
    STA $3200, Y
    INY
    CPY #$E0
    BNE @COPY_BLOCK_3

    LDY #$00
    LDA #ASCII_SPACE
@CLEAR_LAST_LINE:
    STA $32E0, Y
    INY
    CPY #$20
    BNE @CLEAR_LAST_LINE
    RTS

; ==============================================================================
; EHBASIC SYSTEM VECTORS & HARDWARE INTERRUPTS
; ==============================================================================
LAB_vec:
    .word   ACIAin
    .word   ACIAout
    .word   DO_LOAD         ; Linked to Virtual File System Host hook
    .word   DO_SAVE         ; Linked to Virtual File System Host hook
END_VECS:                   ; VITAL: Boundary limit for RAM copy!

IRQ_CODE:
    PHA
    LDA IrqBase
    LSR
    ORA IrqBase
    STA IrqBase
    PLA
    RTI

NMI_CODE:
    ; Minimal stack save for Interrupt duration only
    PHA
    TXA
    PHA
    TYA
    PHA

    ; --- BLINKING CURSOR LOGIC ---
    INC BLINK_COUNTER
    LDA BLINK_COUNTER
    CMP #30              ; Timer: blink every half second (at 60 FPS)
    BNE @SKIP_BLINK

    LDA #$00
    STA BLINK_COUNTER

    LDA CURSOR_STATE
    EOR #$01
    STA CURSOR_STATE
    BEQ @TURN_OFF

@TURN_ON:
    LDY #$00
    LDA (CURSOR_POINTER), Y
    STA CHAR_UNDER_CUR       ; Save character under cursor
    LDA #ASCII_CURSOR
    STA (CURSOR_POINTER), Y  ; Draw "_"
    JMP @SKIP_BLINK

@TURN_OFF:
    LDY #$00
    LDA CHAR_UNDER_CUR
    STA (CURSOR_POINTER), Y  ; Remove "_"

@SKIP_BLINK:
    PLA
    TAY
    PLA
    TAX
    PLA

    ; Original EhBASIC NMI logic
    PHA
    LDA NmiBase
    LSR
    ORA NmiBase
    STA NmiBase
    PLA
    RTI

END_CODE:

LAB_mess:
    .byte   $0D,$0A,"LYCO-8 EHBASIC [C]OLD/[W]ARM ?",$00

.segment "VECTORS"
    ; Pointing hardware vectors safely to ROM
    .word   NMI_CODE
    .word   RES_vec
    .word   IRQ_CODE
