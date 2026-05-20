.setcpu "6502"

.segment "CODE"
.export FUNC_waitForVBlank
.export FUNC_drawSprite
.export FUNC_hideSprite
.export FUNC_clearSprites
.export FUNC_setTile

; ==========================================
; void waitForVBlank()
; ==========================================
; Polling the PPU_STATUS register ($3301).
; Bit 7 is set to 1 when VBlank starts, and reading it clears the flag.
FUNC_waitForVBlank:
@loop:
    LDA $3301           ; Read PPU Status
    BPL @loop           ; Branch if Plus (Bit 7 is 0). Loops until Bit 7 becomes 1.
    RTS

; ==========================================
; void drawSprite(byte index, byte tileId, byte x, byte y)
; ZP Params: $10 = index, $11 = tileId, $12 = x, $13 = y
; ==========================================
; Writes to OAM via $3302 (Address) and $3303 (Data).
; OAM format per sprite: [Y, Tile, Attributes, X]
FUNC_drawSprite:
    ; Calculate OAM base address (index * 4)
    LDA $10
    ASL A               ; Multiply by 2
    ASL A               ; Multiply by 4
    STA $3302           ; Set PPU_OAM_ADDR

    ; Write Sprite Data (Auto-increments PPU_OAM_ADDR internally)
    LDA $13             ; 1. Y position
    STA $3303
    LDA $11             ; 2. Tile ID
    STA $3303
    LDA #$00            ; 3. Attributes (Palette 0, No Flip)
    STA $3303
    LDA $12             ; 4. X position
    STA $3303
    RTS

; ==========================================
; void hideSprite(byte index)
; ZP Params: $10 = index
; ==========================================
; Hides a sprite by setting its Y coordinate to $FF (255, off-screen).
FUNC_hideSprite:
    LDA $10
    ASL A
    ASL A               ; index * 4
    STA $3302           ; Set PPU_OAM_ADDR

    LDA #$FF
    STA $3303           ; Write Y = 255
    RTS

; ==========================================
; void clearSprites()
; ==========================================
; Iterates through all 64 sprites and sets their Y coordinate to $FF.
FUNC_clearSprites:
    LDA #$00
    STA $3302           ; Reset OAM address pointer to 0

    LDX #64             ; We have 64 sprites to clear
@clear_loop:
    LDA #$FF
    STA $3303           ; Write Y = 255

    LDA #$00
    STA $3303           ; Tile = 0
    STA $3303           ; Attr = 0
    STA $3303           ; X = 0

    DEX
    BNE @clear_loop
    RTS

; ==========================================
; void setTile(byte x, byte y, byte tileId)
; ZP Params: $10 = x, $11 = y, $12 = tileId
; ==========================================
; Calculates NameTable pointer: $3000 + (Y * 32) + X
; Then writes the tileId directly to that mapped RAM address.
FUNC_setTile:
    ; We use $14 (Low) and $15 (High) as a temporary pointer
    LDA #$00
    STA $14             ; Initialize Low byte

    ; Calculate High byte: Base $3000 + (Y / 8)
    LDA $11
    LSR A
    LSR A
    LSR A
    CLC
    ADC #$30
    STA $15

    ; Calculate Low byte: (Y * 32) + X
    LDA $11
    ASL A
    ASL A
    ASL A
    ASL A
    ASL A
    CLC
    ADC $10
    STA $14
    BCC @skip_carry
    INC $15             ; If low byte overflows, increment high byte
@skip_carry:

    ; Write the tile ID to the calculated NameTable address
    LDY #$00
    LDA $12
    STA ($14), Y
    RTS