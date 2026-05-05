; ==============================================================================
; Lyco-8 Native OS Video Driver
; Direct memory-mapped text rendering on the TileGraphicsPpu
; ==============================================================================

* = $8000                       ; ROM Entry Point

; --- Memory Mapped I/O ---
KEYBOARD_IN     = $4000         ; Read ASCII input here (0x00 if no key pressed)
PPU_NAMETABLE   = $3000         ; Video RAM (16x8 grid, 128 bytes total)
PPU_CTRL        = $3080         ; PPU Control Register

; --- Zero Page Variables ---
CURSOR_POINTER  = $00           ; 16-bit pointer to the current screen position

; --- ASCII Constants ---
ASCII_CR        = $0D           ; Carriage Return (\r)
ASCII_LF        = $0A           ; Line Feed (\n)
ASCII_PROMPT    = $3E           ; Greater-than sign (>)
ASCII_SPACE     = $20           ; Space character

; ==============================================================================
; BOOT SEQUENCE
; ==============================================================================
RESET:
    SEI                         ; Disable hardware interrupts
    CLD                         ; Clear decimal mode
    LDX #$FF
    TXS                         ; Initialize hardware stack pointer

    ; 1. Clear the screen (Fill Nametable with Spaces)
    JSR CLEAR_SCREEN

    ; 2. Initialize the cursor pointer to the start of the Nametable ($3000)
    LDA #$00
    STA CURSOR_POINTER          ; Low byte  ($00)
    LDA #$30
    STA CURSOR_POINTER + 1      ; High byte ($30)

    ; 3. Print the boot message
    LDX #$00
PRINT_BOOT_MSG:
    LDA MSG_BOOT, X
    BEQ BOOT_DONE               ; Null-terminator check
    JSR PRINT_CHAR
    INX
    JMP PRINT_BOOT_MSG

BOOT_DONE:
    JSR PRINT_NEW_PROMPT        ; Print "> "

    ; Enable PPU rendering (Ensure screen refreshes)
    LDA #$80
    STA PPU_CTRL

; ==============================================================================
; MAIN OS LOOP (Keyboard Polling)
; ==============================================================================
MAIN_LOOP:
    LDA KEYBOARD_IN             ; Read hardware keyboard register
    BEQ MAIN_LOOP               ; Block until a key is pressed

    CMP #ASCII_CR               ; Is it the Enter key?
    BEQ HANDLE_ENTER

    CMP #$1B                    ; Is it the Escape key?
    BEQ HALT_SYSTEM

    JSR PRINT_CHAR              ; Print any other character to VRAM
    JMP MAIN_LOOP

; ==============================================================================
; INPUT HANDLERS
; ==============================================================================
HANDLE_ENTER:
    LDA #ASCII_LF               ; Treat Enter as a Line Feed
    JSR PRINT_CHAR
    JSR PRINT_NEW_PROMPT
    JMP MAIN_LOOP

HALT_SYSTEM:
    BRK                         ; Halt the CPU

; ==============================================================================
; VIDEO DRIVER SUBROUTINES
; ==============================================================================

; ------------------------------------------------------------------------------
; CLEAR_SCREEN: Fills the 128 bytes of the Nametable with Space characters
; ------------------------------------------------------------------------------
CLEAR_SCREEN:
    LDA #ASCII_SPACE
    LDY #$00
CLEAR_LOOP:
    STA PPU_NAMETABLE, Y            ; Pulisce da $3000 a $30FF (256 byte)
    STA PPU_NAMETABLE + $0100, Y    ; Pulisce da $3100 a $31FF (256 byte)
    STA PPU_NAMETABLE + $0200, Y    ; Pulisce da $3200 a $32FF (256 byte)
    INY
    BNE CLEAR_LOOP                  ; Continua finché Y non torna a 0 (overflow a 256)
    RTS

; ------------------------------------------------------------------------------
; PRINT_NEW_PROMPT: Prints "> " at the current cursor position
; ------------------------------------------------------------------------------
PRINT_NEW_PROMPT:
    LDA #ASCII_PROMPT
    JSR PRINT_CHAR
    LDA #ASCII_SPACE
    JSR PRINT_CHAR
    RTS

; ------------------------------------------------------------------------------
; PRINT_CHAR: Prints a single character and advances the memory cursor
; Handles Line Feeds by advancing the pointer to the next multiple of 16
; ------------------------------------------------------------------------------
PRINT_CHAR:
    CMP #ASCII_LF               ; Is it a Line Feed?
    BEQ HANDLE_LF

    ; Write the character to Video RAM at the current Cursor Pointer
    LDY #$00
    STA (CURSOR_POINTER), Y

    ; Advance the Cursor Pointer by 1
    INC CURSOR_POINTER
    BNE CHECK_WRAP              ; If low byte didn't overflow, skip high byte inc
    INC CURSOR_POINTER + 1

CHECK_WRAP:
    ; Se il cursore raggiunge $3300 (fine dello schermo: $3000 + $0300)
    LDA CURSOR_POINTER + 1
    CMP #$33
    BNE EXIT_PRINT

    ; Screen overflow rilevato: riporta il cursore in alto a sinistra ($3000)
    LDA #$00
    STA CURSOR_POINTER
    LDA #$30
    STA CURSOR_POINTER + 1

EXIT_PRINT:
    RTS

HANDLE_LF:
    ; Avanza il cursore alla prossima riga (multiplo di 32 = $20)
    LDA CURSOR_POINTER
    AND #$E0                    ; Azzera i 5 bit più bassi (da 0 a 31)
    CLC
    ADC #$20                    ; Aggiunge 32 ($20)
    STA CURSOR_POINTER

    BCC CHECK_WRAP              ; Se non c'è riporto, controlla il fondo dello schermo
    INC CURSOR_POINTER + 1      ; Riporto sul byte alto
    JMP CHECK_WRAP

; ==============================================================================
; DATA SECTION
; ==============================================================================
MSG_BOOT:
    .BYTE "**** LYCO OS ****", ASCII_LF
    .BYTE "READY", ASCII_LF, 0

; ==============================================================================
; HARDWARE VECTORS
; ==============================================================================
* = $FFFA
    .BYTE $00, $80              ; NMI Vector
    .BYTE $00, $80              ; Reset Vector
    .BYTE $00, $80              ; IRQ/BRK Vector