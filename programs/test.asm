; ==============================================================================
; Lyco-8 Tile-Based PPU and NMI Hardware Test (Full Complete File)
; ==============================================================================

* = $8000               ; Assuming the ROM is mapped starting at $8000

; --- Memory Map Constants ---
CHR_RAM         = $2000
NAMETABLE       = $3000
PPU_CTRL        = $3080
PPU_STATUS      = $3081

; --- Zero Page Variables ---
TILE_X          = $00
FRAME_COUNTER   = $01

; ==============================================================================
; RESET HANDLER
; ==============================================================================
RESET:
    SEI                 ; Disable IRQs
    CLD                 ; Disable Decimal Mode
    LDX #$FF
    TXS                 ; Initialize Hardware Stack Pointer

    ; Initialize Zero Page Variables
    LDA #$00
    STA TILE_X
    STA FRAME_COUNTER

    ; --- 1. Clear Nametable (Set all tiles to Tile ID 0 / Background) ---
    LDX #$00
    LDA #$00
CLEAR_NT_LOOP:
    STA NAMETABLE, X
    INX
    CPX #128
    BNE CLEAR_NT_LOOP

    ; --- 2. Load Tile 1 Data (Solid Yellow Block) ---
    LDX #$00
    LDA #$77            ; Packed 4BPP: Color 7 (Yellow)
LOAD_TILE_LOOP:
    STA CHR_RAM + 32, X
    INX
    CPX #32
    BNE LOAD_TILE_LOOP

    ; --- 3. Draw initial tile on screen ---
    LDA #$01
    LDX TILE_X
    STA NAMETABLE + $30, X

    ; --- 4. Enable NMI on the PPU ---
    LDA #$80
    STA PPU_CTRL

; ==============================================================================
; MAIN LOOP
; ==============================================================================
MAIN_LOOP:
    JMP MAIN_LOOP

; ==============================================================================
; NMI HANDLER (Triggered automatically at 60Hz)
; ==============================================================================
* = $8100
NMI:
    PHA
    TXA
    PHA

    LDA PPU_STATUS

    INC FRAME_COUNTER
    LDA FRAME_COUNTER
    CMP #60
    BNE END_NMI

    LDA #$00
    STA FRAME_COUNTER

    LDX TILE_X
    LDA #$00
    STA NAMETABLE + $30, X

    INX
    CPX #16
    BNE DRAW_NEW
    LDX #$00

DRAW_NEW:
    STX TILE_X
    LDA #$01
    STA NAMETABLE + $30, X

END_NMI:
    PLA
    TAX
    PLA
    RTI

; ==============================================================================
; HARDWARE VECTORS (Forced exactly at the top of the 64KB memory map)
; ==============================================================================
* = $FFFA
    .BYTE $00, $81      ; NMI Vector ($8100)
    .BYTE $00, $80      ; Reset Vector ($8000)
    .BYTE $00, $80      ; IRQ/BRK Vector ($8000)