; ==============================================================================
; Lyco-8 Native OS Video Driver (32x24 Resolution)
; Direct memory-mapped text rendering on the TileGraphicsPpu
; ==============================================================================

* = $8000                       ; ROM Entry Point

; --- Memory Mapped I/O ---
KEYBOARD_IN     = $4000         ; Read ASCII input here (0x00 if no key pressed)[cite: 12]
PPU_NAMETABLE   = $3000         ; Video RAM (32x24 grid, 768 bytes total)[cite: 12]
PPU_CTRL        = $3300         ; PPU Control Register (Ora in zona sicura!)

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

    ; Enable PPU rendering and Hardware NMI (VBlank)
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
; CLEAR_SCREEN: Fills the 768 bytes of the Nametable (32x24) with Spaces
; ------------------------------------------------------------------------------
CLEAR_SCREEN:
    LDA #ASCII_SPACE
    LDY #$00
CLEAR_LOOP:
    STA PPU_NAMETABLE, Y            ; Clear from $3000 to $30FF (256 bytes)
    STA PPU_NAMETABLE + $0100, Y    ; Clear from $3100 to $31FF (256 bytes)
    STA PPU_NAMETABLE + $0200, Y    ; Clear from $3200 to $32FF (256 bytes)
    INY
    BNE CLEAR_LOOP                  ; Continue until Y wraps to 0
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
; PRINT_CHAR: Prints a single character and advances the memory cursor.
; Handles Line Feeds by advancing the pointer to the next multiple of 32.
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
    ; Check if cursor has reached or exceeded $3300 (End of 32x24 screen)
    LDA CURSOR_POINTER + 1
    CMP #$33
    BNE EXIT_PRINT              ; If high byte is not $33, we are still on screen

    ; Screen overflow detected! We must scroll the entire screen up.
    JSR SCROLL_UP

    ; After scrolling, place the cursor at the start of the LAST row ($32E0)
    LDA #$E0
    STA CURSOR_POINTER
    LDA #$32
    STA CURSOR_POINTER + 1

EXIT_PRINT:
    RTS

HANDLE_LF:
    ; Advance cursor to the start of the next line (multiple of 32 = $20)
    LDA CURSOR_POINTER
    AND #$E0                    ; Clear the lower 5 bits (0 to 31)
    CLC
    ADC #$20                    ; Add 32 ($20)
    STA CURSOR_POINTER

    BCC CHECK_WRAP              ; If addition didn't overflow, check wrapping
    INC CURSOR_POINTER + 1      ; Carry over to high byte
    JMP CHECK_WRAP

; ------------------------------------------------------------------------------
; SCROLL_UP: Shifts the entire screen up by one row (32 bytes)
; and clears the bottom row with space characters.
; Screen is at $3000 - $32FF.
; ------------------------------------------------------------------------------
SCROLL_UP:
    LDY #$00

@COPY_BLOCK_1:
    LDA $3020, Y                ; Read from row 1 onwards
    STA $3000, Y                ; Write to row 0 onwards
    INY
    BNE @COPY_BLOCK_1           ; Copy first 256 bytes

@COPY_BLOCK_2:
    LDA $3120, Y
    STA $3100, Y
    INY
    BNE @COPY_BLOCK_2           ; Copy next 256 bytes

@COPY_BLOCK_3:
    LDA $3220, Y
    STA $3200, Y
    INY
    CPY #$E0                    ; Copy last 224 bytes (736 total)
    BNE @COPY_BLOCK_3

    ; Clear the last line (from $32E0 to $32FF) with spaces
    LDY #$00
    LDA #ASCII_SPACE
@CLEAR_LAST_LINE:
    STA $32E0, Y
    INY
    CPY #$20                    ; Clear exactly 32 characters
    BNE @CLEAR_LAST_LINE

    RTS

; ==============================================================================
; DATA SECTION
; ==============================================================================
MSG_BOOT:
    .BYTE "**** LYCO OS ****", ASCII_LF
    .BYTE "READY", ASCII_LF, 0

; ==============================================================================
; HARDWARE INTERRUPT HANDLERS
; ==============================================================================
NMI_HANDLER:
    ; Triggered 60 times a second by the PPU (VBlank)
    ; Safely return execution to the interrupted code
    RTI

IRQ_HANDLER:
    ; Triggered by external hardware (timers, sound chips)
    RTI

; ==============================================================================
; HARDWARE VECTORS
; ==============================================================================
* = $FFFA
    .WORD NMI_HANDLER           ; $FFFA/B: NMI Vector
    .WORD RESET                 ; $FFFC/D: Reset Vector
    .WORD IRQ_HANDLER           ; $FFFE/F: IRQ/BRK Vector