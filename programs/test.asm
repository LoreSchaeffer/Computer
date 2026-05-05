; ==============================================================================
; Lyco-8 Hardware I/O Test (Keyboard & Shift-Register Joypad)
; ==============================================================================

* = $8000               ; Execution begins at $8000 (Start of 32KB ROM)

; --- Memory Map I/O ---
CHR_RAM         = $2000
NAMETABLE       = $3000
PPU_CTRL        = $3080
PPU_STATUS      = $3081
KEYBOARD_REG    = $4000
JOYPAD_REG      = $4016

; --- Zero Page Variables ---
TILE_X          = $00   ; 0 to 15
TILE_Y          = $01   ; 0 to 7
PAD_STATE       = $02   ; Bitmask holding the 8-bit Joypad state
NAMETABLE_INDEX = $03   ; Calculated memory offset for the screen grid

; --- Constants ---
KEY_W           = $57
KEY_A           = $41
KEY_S           = $53
KEY_D           = $44

PAD_UP          = $08
PAD_DOWN        = $04
PAD_LEFT        = $02
PAD_RIGHT       = $01

; ==============================================================================
; RESET HANDLER: System Initialization
; ==============================================================================
RESET:
    SEI                 ; Disable software interrupts
    CLD                 ; Clear decimal math mode
    LDX #$FF
    TXS                 ; Initialize the hardware stack pointer to $01FF

    ; Set initial position to center of the screen
    LDA #$07
    STA TILE_X
    LDA #$03
    STA TILE_Y

    ; --- Clear the Nametable (Background) ---
    LDX #$00
    LDA #$00
CLEAR_NT_LOOP:
    STA NAMETABLE, X
    INX
    CPX #128            ; 16 columns * 8 rows
    BNE CLEAR_NT_LOOP

    ; --- Load Solid Yellow Tile into CHR-RAM (Tile 1) ---
    LDX #$00
    LDA #$77            ; Packed 4BPP: Color 7 (Yellow)
LOAD_TILE_LOOP:
    STA CHR_RAM + 32, X
    INX
    CPX #32             ; 32 bytes per tile
    BNE LOAD_TILE_LOOP

    ; Enable the PPU VBlank NMI generation
    LDA #$80
    STA PPU_CTRL

; ==============================================================================
; MAIN EXECUTION LOOP (Interrupt-driven architecture)
; ==============================================================================
MAIN_LOOP:
    JMP MAIN_LOOP       ; The CPU yields here, waiting for the hardware NMI

; ==============================================================================
; NMI HANDLER: Runs at ~60Hz to handle Game Logic and Rendering
; ==============================================================================
* = $8100
NMI:
    PHA                 ; Push A to stack
    TXA
    PHA                 ; Push X to stack
    TYA
    PHA                 ; Push Y to stack

    LDA PPU_STATUS      ; Acknowledge VBlank to the PPU

    ; 1. Erase the tile at its CURRENT position
    JSR CALC_OFFSET
    LDX NAMETABLE_INDEX
    LDA #$00            ; Tile 0 (Empty Background)
    STA NAMETABLE, X

    ; 2. Poll the Keyboard (Address $4000)
    JSR READ_KEYBOARD

    ; 3. Poll the Joypad Shift Register (Address $4016)
    JSR READ_JOYPAD

    ; 4. Clamp the X and Y coordinates to prevent going off-screen
    JSR CLAMP_POSITION

    ; 5. Draw the tile at its NEW position
    JSR CALC_OFFSET
    LDX NAMETABLE_INDEX
    LDA #$01            ; Tile 1 (Yellow Block)
    STA NAMETABLE, X

    ; End of NMI
    PLA                 ; Pull Y
    TAY
    PLA                 ; Pull X
    TAX
    PLA                 ; Pull A
    RTI

; ==============================================================================
; SUBROUTINE: Read Keyboard Input
; ==============================================================================
READ_KEYBOARD:
    LDA KEYBOARD_REG    ; Reading $4000 fetches ASCII and clears the Java buffer
    BEQ END_KEYBOARD    ; If 0, no key was pressed

    CMP #KEY_W
    BNE CHECK_S
    DEC TILE_Y
    JMP END_KEYBOARD

CHECK_S:
    CMP #KEY_S
    BNE CHECK_A
    INC TILE_Y
    JMP END_KEYBOARD

CHECK_A:
    CMP #KEY_A
    BNE CHECK_D
    DEC TILE_X
    JMP END_KEYBOARD

CHECK_D:
    CMP #KEY_D
    BNE END_KEYBOARD
    INC TILE_X

END_KEYBOARD:
    RTS

; ==============================================================================
; SUBROUTINE: Read Joypad Shift Register
; ==============================================================================
READ_JOYPAD:
    ; Strobe the joypad to latch current physical state (Write 1 then 0)
    LDA #$01
    STA JOYPAD_REG
    LDA #$00
    STA JOYPAD_REG

    ; Read 8 bits sequentially from the shift register
    LDX #$08
JOYPAD_READ_LOOP:
    LDA JOYPAD_REG      ; Read bit 7 from Java hardware
    LSR A               ; Shift bit 0 into the Carry Flag
    ROL PAD_STATE       ; Shift Carry Flag into PAD_STATE from the right
    DEX
    BNE JOYPAD_READ_LOOP

    ; Process extracted bitmask
    LDA PAD_STATE
    AND #PAD_UP
    BEQ PAD_CHECK_DOWN
    DEC TILE_Y

PAD_CHECK_DOWN:
    LDA PAD_STATE
    AND #PAD_DOWN
    BEQ PAD_CHECK_LEFT
    INC TILE_Y

PAD_CHECK_LEFT:
    LDA PAD_STATE
    AND #PAD_LEFT
    BEQ PAD_CHECK_RIGHT
    DEC TILE_X

PAD_CHECK_RIGHT:
    LDA PAD_STATE
    AND #PAD_RIGHT
    BEQ END_JOYPAD
    INC TILE_X

END_JOYPAD:
    RTS

; ==============================================================================
; SUBROUTINE: Clamp Spatial Coordinates (Screen Bounds)
; ==============================================================================
CLAMP_POSITION:
    ; Check X Underflow (went below 0, wrapping to $FF)
    LDA TILE_X
    BPL CHECK_X_MAX     ; Branch if Positive (Bit 7 is 0)
    LDA #$00
    STA TILE_X

CHECK_X_MAX:
    ; Check X Overflow (>= 16)
    LDA TILE_X
    CMP #16
    BCC CHECK_Y_MIN     ; Branch if Carry Clear (TILE_X < 16)
    LDA #15
    STA TILE_X

CHECK_Y_MIN:
    ; Check Y Underflow
    LDA TILE_Y
    BPL CHECK_Y_MAX
    LDA #$00
    STA TILE_Y

CHECK_Y_MAX:
    ; Check Y Overflow (>= 8)
    LDA TILE_Y
    CMP #8
    BCC END_CLAMP
    LDA #7
    STA TILE_Y

END_CLAMP:
    RTS

; ==============================================================================
; SUBROUTINE: Calculate Nametable Memory Offset (Y * 16 + X)
; ==============================================================================
CALC_OFFSET:
    LDA TILE_Y
    ASL A               ; Y * 2
    ASL A               ; Y * 4
    ASL A               ; Y * 8
    ASL A               ; Y * 16
    STA NAMETABLE_INDEX

    CLC                 ; Clear carry before addition
    LDA NAMETABLE_INDEX
    ADC TILE_X          ; Add X offset
    STA NAMETABLE_INDEX
    RTS

; ==============================================================================
; HARDWARE VECTORS (Zero-padded to 32KB by the Assembler)
; ==============================================================================
* = $FFFA
    .BYTE $00, $81      ; NMI Vector ($8100)
    .BYTE $00, $80      ; Reset Vector ($8000)
    .BYTE $00, $80      ; IRQ/BRK Vector ($8000)