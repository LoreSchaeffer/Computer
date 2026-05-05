; ==============================================================================
; Lyco-8 Pong: Player vs CPU (Fast Ball, Keyboard Support, Scoreboard)
; ==============================================================================

* = $8000               ; Execution begins at $8000

; --- Memory Map I/O ---
CHR_RAM         = $2000
NAMETABLE       = $3000
PPU_CTRL        = $3080
PPU_STATUS      = $3081
KEYBOARD_REG    = $4000
OAM_DMA         = $4014
OAM_RAM         = $0200 ; RAM page 2 reserved for OAM Buffer

; --- Zero Page Variables ---
BALL_X          = $01
BALL_Y          = $02
BALL_DX         = $03   ; Velocity X (2 or $FE for -2)
BALL_DY         = $04   ; Velocity Y (2 or $FE for -2)

PLAYER_Y        = $05   ; Player Paddle Y (X is fixed at 10)
CPU_Y           = $06   ; CPU Paddle Y (X is fixed at 110)
FRAME_COUNT     = $07   ; Used to throttle CPU AI

P1_SCORE        = $08   ; Player 1 Score (0-5)
CPU_SCORE       = $09   ; CPU Score (0-5)

; --- Constants ---
KEY_W           = $57
KEY_S           = $53

; ==============================================================================
; RESET HANDLER: Hardware and Game Initialization
; ==============================================================================
RESET:
    SEI                 ; Disable IRQs
    CLD                 ; Disable Decimal Mode
    LDX #$FF
    TXS                 ; Set Stack Pointer

    ; 1. Clear OAM RAM Buffer ($0200 - $02FF)
    LDX #$00
    LDA #$FF            ; Put sprites off-screen
CLEAR_OAM:
    STA OAM_RAM, X
    INX
    BNE CLEAR_OAM

    ; 2. Load Graphics (Tile 1: Paddle, Tile 2: Ball)
    LDX #$00
    LDA #$11            ; Color 1 (White)
LOAD_TILE_1:
    STA CHR_RAM + 32, X
    INX
    CPX #32
    BNE LOAD_TILE_1

    LDX #$00
    LDA #$22            ; Color 2 (Red)
LOAD_TILE_2:
    STA CHR_RAM + 64, X
    INX
    CPX #32
    BNE LOAD_TILE_2

    ; 3. Load Number Fonts into CHR-RAM (Tile 10 to 15)
    LDX #$00
LOAD_FONTS:
    LDA FONT_DATA, X
    STA CHR_RAM + 320, X  ; Tile 10 starts at offset 320 (10 * 32)
    INX
    CPX #192              ; 6 tiles * 32 bytes = 192 bytes
    BNE LOAD_FONTS

    ; 4. Initialize Game State
    LDA #$00
    STA P1_SCORE
    STA CPU_SCORE
    STA FRAME_COUNT

    LDA #24
    STA PLAYER_Y
    STA CPU_Y

    JSR RESET_BALL
    JSR UPDATE_SCOREBOARD

    ; 5. Enable NMI
    LDA #$80
    STA PPU_CTRL

; ==============================================================================
; MAIN LOOP
; ==============================================================================
MAIN_LOOP:
    JMP MAIN_LOOP       ; Wait for Hardware NMI (60Hz)

; ==============================================================================
; NMI HANDLER: The Game Engine Core
; ==============================================================================
* = $8100
NMI:
    PHA
    TXA
    PHA
    TYA
    PHA

    LDA PPU_STATUS      ; Acknowledge VBlank

    ; --- 1. OAM DMA TRANSFER ---
    LDA #$02
    STA OAM_DMA         ; Suspend CPU and transfer $0200-$02FF to PPU

    ; --- 2. GAME LOGIC ---
    INC FRAME_COUNT

    JSR READ_KEYBOARD
    JSR MOVE_CPU
    JSR MOVE_BALL
    JSR CHECK_COLLISIONS

    ; --- 3. RENDER STATE ---
    JSR UPDATE_SPRITES

    PLA
    TAY
    PLA
    TAX
    PLA
    RTI

; ==============================================================================
; SUBROUTINE: Keyboard Input Polling (W and S)
; ==============================================================================
READ_KEYBOARD:
    LDA KEYBOARD_REG
    BEQ END_KEYBOARD    ; No key pressed

    CMP #KEY_W
    BNE CHK_S
    LDA PLAYER_Y
    BEQ END_KEYBOARD    ; Hit top boundary
    DEC PLAYER_Y
    DEC PLAYER_Y        ; Move 2 pixels up
    DEC PLAYER_Y
    JMP END_KEYBOARD

CHK_S:
    CMP #KEY_S
    BNE END_KEYBOARD
    LDA PLAYER_Y
    CMP #48             ; Bottom boundary
    BCS END_KEYBOARD
    INC PLAYER_Y
    INC PLAYER_Y        ; Move 2 pixels down
    INC PLAYER_Y

END_KEYBOARD:
    RTS

; ==============================================================================
; SUBROUTINE: Basic CPU AI
; ==============================================================================
MOVE_CPU:
    LDA FRAME_COUNT
    AND #$01
    BEQ END_CPU_MOVE    ; Move every other frame to remain beatable

    LDA CPU_Y
    CLC
    ADC #$04            ; Target center of the paddle
    CMP BALL_Y
    BEQ END_CPU_MOVE
    BCC CPU_MOVE_DOWN

CPU_MOVE_UP:
    LDA CPU_Y
    BEQ END_CPU_MOVE
    DEC CPU_Y
    DEC CPU_Y
    JMP END_CPU_MOVE

CPU_MOVE_DOWN:
    LDA CPU_Y
    CMP #48
    BCS END_CPU_MOVE
    INC CPU_Y
    INC CPU_Y

END_CPU_MOVE:
    RTS

; ==============================================================================
; SUBROUTINE: Physics & Ball Movement
; ==============================================================================
MOVE_BALL:
    CLC
    LDA BALL_X
    ADC BALL_DX
    STA BALL_X

    CLC
    LDA BALL_Y
    ADC BALL_DY
    STA BALL_Y
    RTS

; ==============================================================================
; SUBROUTINE: Collision Detection
; ==============================================================================
CHECK_COLLISIONS:
    ; --- Top / Bottom Walls ---
    LDA BALL_Y
    BMI REVERSE_Y_TOP   ; Underflow (went negative)
    CMP #56             ; Screen (64) - Ball (8)
    BCS REVERSE_Y_BOT   ; Overflow (hit bottom)
    JMP CHK_LEFT_PADDLE

REVERSE_Y_TOP:
    LDA #$02            ; Bounce Down
    STA BALL_DY
    LDA #$00
    STA BALL_Y          ; Push out of wall
    JMP CHK_LEFT_PADDLE

REVERSE_Y_BOT:
    LDA #$FE            ; Bounce Up (-2 in Two's Complement)
    STA BALL_DY
    LDA #55
    STA BALL_Y          ; Push out of wall

CHK_LEFT_PADDLE:
    ; Check if Ball reached Player (X <= 18)
    LDA BALL_X
    CMP #18
    BCS CHK_RIGHT_PADDLE
    CMP #08             ; Did it pass the paddle completely?
    BCC CPU_SCORED

    ; Y intersection check
    LDA BALL_Y
    CLC
    ADC #$08
    CMP PLAYER_Y
    BCC CHK_RIGHT_PADDLE ; Ball above paddle

    LDA PLAYER_Y
    CLC
    ADC #16
    CMP BALL_Y
    BCC CHK_RIGHT_PADDLE ; Ball below paddle

    ; Hit Player!
    LDA #$02
    STA BALL_DX
    LDA #18
    STA BALL_X          ; Push out of paddle
    JMP END_COLLISIONS

CHK_RIGHT_PADDLE:
    ; Check if Ball reached CPU (X >= 102)
    LDA BALL_X
    CMP #102
    BCC END_COLLISIONS
    CMP #114            ; Did it pass the CPU completely?
    BCS P1_SCORED

    ; Y intersection check
    LDA BALL_Y
    CLC
    ADC #$08
    CMP CPU_Y
    BCC END_COLLISIONS

    LDA CPU_Y
    CLC
    ADC #16
    CMP BALL_Y
    BCC END_COLLISIONS

    ; Hit CPU!
    LDA #$FE
    STA BALL_DX
    LDA #100
    STA BALL_X          ; Push out of paddle
    JMP END_COLLISIONS

P1_SCORED:
    INC P1_SCORE
    JMP EVALUATE_WIN

CPU_SCORED:
    INC CPU_SCORE

EVALUATE_WIN:
    LDA P1_SCORE
    CMP #$05
    BEQ RESET_GAME
    LDA CPU_SCORE
    CMP #$05
    BEQ RESET_GAME
    JMP RESET_ROUND

RESET_GAME:
    LDA #$00
    STA P1_SCORE
    STA CPU_SCORE

RESET_ROUND:
    JSR UPDATE_SCOREBOARD
    JSR RESET_BALL

END_COLLISIONS:
    RTS

; ==============================================================================
; SUBROUTINE: Scoreboard UI Rendering
; ==============================================================================
UPDATE_SCOREBOARD:
    ; Write Player Score (Tile 10 + P1_SCORE)
    LDA P1_SCORE
    CLC
    ADC #10
    STA NAMETABLE + 5   ; X=5, Y=0

    ; Write CPU Score
    LDA CPU_SCORE
    CLC
    ADC #10
    STA NAMETABLE + 10  ; X=10, Y=0
    RTS

; ==============================================================================
; SUBROUTINE: Reset Ball
; ==============================================================================
RESET_BALL:
    LDA #60             ; Center X
    STA BALL_X
    LDA #28             ; Center Y
    STA BALL_Y

    ; Set initial speed to +2, +2
    LDA #$02
    STA BALL_DX
    STA BALL_DY
    RTS

; ==============================================================================
; SUBROUTINE: Render State to OAM Buffer
; ==============================================================================
UPDATE_SPRITES:
    ; Sprite 0: Player Top Half
    LDA PLAYER_Y
    STA OAM_RAM         ; Y
    LDA #$01
    STA OAM_RAM + 1     ; Tile ID
    LDA #$00
    STA OAM_RAM + 2     ; Attributes
    LDA #10
    STA OAM_RAM + 3     ; X

    ; Sprite 1: Player Bottom Half
    LDA PLAYER_Y
    CLC
    ADC #$08
    STA OAM_RAM + 4
    LDA #$01
    STA OAM_RAM + 5
    LDA #$00
    STA OAM_RAM + 6
    LDA #10
    STA OAM_RAM + 7

    ; Sprite 2: CPU Top Half
    LDA CPU_Y
    STA OAM_RAM + 8
    LDA #$01
    STA OAM_RAM + 9
    LDA #$00
    STA OAM_RAM + 10
    LDA #110
    STA OAM_RAM + 11

    ; Sprite 3: CPU Bottom Half
    LDA CPU_Y
    CLC
    ADC #$08
    STA OAM_RAM + 12
    LDA #$01
    STA OAM_RAM + 13
    LDA #$00
    STA OAM_RAM + 14
    LDA #110
    STA OAM_RAM + 15

    ; Sprite 4: Ball
    LDA BALL_Y
    STA OAM_RAM + 16
    LDA #$02            ; Red Ball Tile
    STA OAM_RAM + 17
    LDA #$00
    STA OAM_RAM + 18
    LDA BALL_X
    STA OAM_RAM + 19

    RTS

; ==============================================================================
; DATA: 4BPP Packed Fonts (Numbers 0 to 5) - 32 bytes each
; ==============================================================================
FONT_DATA:
    ; 0
    .BYTE $11, $11, $00, $00, $10, $01, $00, $00, $10, $01, $00, $00, $10, $01, $00, $00
    .BYTE $11, $11, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00
    ; 1
    .BYTE $01, $10, $00, $00, $11, $10, $00, $00, $01, $10, $00, $00, $01, $10, $00, $00
    .BYTE $11, $11, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00
    ; 2
    .BYTE $11, $11, $00, $00, $00, $01, $00, $00, $11, $11, $00, $00, $10, $00, $00, $00
    .BYTE $11, $11, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00
    ; 3
    .BYTE $11, $11, $00, $00, $00, $01, $00, $00, $01, $11, $00, $00, $00, $01, $00, $00
    .BYTE $11, $11, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00
    ; 4
    .BYTE $10, $01, $00, $00, $10, $01, $00, $00, $11, $11, $00, $00, $00, $01, $00, $00
    .BYTE $00, $01, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00
    ; 5
    .BYTE $11, $11, $00, $00, $10, $00, $00, $00, $11, $11, $00, $00, $00, $01, $00, $00
    .BYTE $11, $11, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00, $00

; ==============================================================================
; HARDWARE VECTORS
; ==============================================================================
* = $FFFA
    .BYTE $00, $81      ; NMI Vector ($8100)
    .BYTE $00, $80      ; Reset Vector ($8000)
    .BYTE $00, $80      ; IRQ/BRK Vector ($8000)