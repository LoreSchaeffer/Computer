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

; --- Variabili Zero Page (Libere da EhBASIC) ---
CURSOR_POINTER  = $E0
BLINK_COUNTER   = $E2           ; Timer per il lampeggio (0-30)
CURSOR_STATE    = $E3           ; 0 = spento, 1 = acceso
CHAR_UNDER_CUR  = $E4           ; Salva il carattere sotto il cursore

CHAR_TO_PRINT   = $E5           ; Backup sicuro per l'Accumulatore
SAVE_X          = $E6           ; Backup sicuro per il registro X
SAVE_Y          = $E7           ; Backup sicuro per il registro Y

; --- Costanti ASCII ---
ASCII_BS        = $08           ; Backspace
ASCII_CR        = $0D
ASCII_LF        = $0A
ASCII_PROMPT    = $3E
ASCII_SPACE     = $20
ASCII_CURSOR    = $5F           ; Underscore '_'

.segment "BIOS"

RES_vec:
    CLD
    LDX #$FF
    TXS

    ; --- INIZIALIZZAZIONE HARDWARE LYCO-8 ---
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

    ; FONDAMENTALE: Copia solo i 4 vettori, NON tutto il codice!
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

    AND #$DF            ; Forza maiuscole
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

; --- OUTPUT A SCHERMO ---
ACIAout:
    ; Salvataggio antiproiettile in Zero Page
    STA CHAR_TO_PRINT
    STX SAVE_X
    STY SAVE_Y

    ; Prima di stampare o muoversi, spegne il cursore per non lasciare scie
    JSR ERASE_CURSOR

    LDA CHAR_TO_PRINT
    CMP #ASCII_CR       ; Ignora CR
    BEQ @SKIP
    CMP #ASCII_BS       ; Intercetta Backspace
    BEQ @BACKSPACE

    JSR PRINT_CHAR      ; Stampa normale
    JMP @SKIP

@BACKSPACE:
    JSR HANDLE_BACKSPACE

@SKIP:
    ; Ripristino antiproiettile
    LDX SAVE_X
    LDY SAVE_Y
    LDA CHAR_TO_PRINT
    RTS

; --- INPUT TASTIERA ---
ACIAin:
    LDA KEYBOARD_IN
    BEQ LAB_nobyw
    SEC
    RTS

LAB_nobyw:
    CLC
no_load:
no_save:
    RTS

; ==============================================================================
; DRIVER VIDEO LYCO-8
; ==============================================================================

ERASE_CURSOR:
    LDA CURSOR_STATE
    BEQ @DONE
    LDA #$00
    STA CURSOR_STATE
    STA BLINK_COUNTER       ; Resetta il timer
    LDY #$00
    LDA CHAR_UNDER_CUR
    STA (CURSOR_POINTER), Y ; Ripristina il carattere originale
@DONE:
    RTS

HANDLE_BACKSPACE:
    ; Evita di cancellare oltre l'inizio dello schermo ($3000)
    LDA CURSOR_POINTER + 1
    CMP #$30
    BNE @DO_BACKSPACE
    LDA CURSOR_POINTER
    BEQ @DONE
@DO_BACKSPACE:
    ; Decrementa il cursore a 16-bit
    LDA CURSOR_POINTER
    BNE @NO_WRAP
    DEC CURSOR_POINTER + 1
@NO_WRAP:
    DEC CURSOR_POINTER

    ; Cancella il carattere scrivendo uno spazio
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
; VETTORI DI SISTEMA EHBASIC E INTERRUPT
; ==============================================================================
LAB_vec:
    .word   ACIAin
    .word   ACIAout
    .word   no_load
    .word   no_save
END_VECS:               ; FONDAMENTALE: Il limite della copia in RAM!

IRQ_CODE:
    PHA
    LDA IrqBase
    LSR
    ORA IrqBase
    STA IrqBase
    PLA
    RTI

NMI_CODE:
    PHA
    TXA
    PHA
    TYA
    PHA

    ; --- LOGICA CURSORE LAMPEGGIANTE ---
    INC BLINK_COUNTER
    LDA BLINK_COUNTER
    CMP #30              ; Lampeggia ogni 30 frame (mezzo secondo)
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
    STA CHAR_UNDER_CUR
    LDA #ASCII_CURSOR
    STA (CURSOR_POINTER), Y
    JMP @SKIP_BLINK

@TURN_OFF:
    LDY #$00
    LDA CHAR_UNDER_CUR
    STA (CURSOR_POINTER), Y

@SKIP_BLINK:
    PLA
    TAY
    PLA
    TAX
    PLA

    ; Logica originale NMI di EhBASIC
    PHA
    LDA NmiBase
    LSR
    ORA NmiBase
    STA NmiBase
    PLA
    RTI

END_CODE:

LAB_mess:
    .byte   $0D,$0A,"LYCO-8 BASIC [C]OLD/[W]ARM ?",$00

.segment "VECTORS"
    ; FONDAMENTALE: Puntiamo i vettori direttamente in ROM, salvando la RAM!
    .word   NMI_CODE
    .word   RES_vec
    .word   IRQ_CODE