.setcpu "6502"

.segment "CODE"
.export FUNC_arrayCopy
.export FUNC_arrayFill
.export FUNC_arrayIndexOf

; ==========================================
; void arrayCopy(byte* src, byte* dest, byte length)
; ZP Params: $10,$11 = src | $12,$13 = dest | $14 = length
; ==========================================
FUNC_arrayCopy:
    LDA $14             ; Check if length is 0
    BEQ @done           ; If 0, nothing to do
    LDY #$00
@loop:
    LDA ($10), Y        ; Read from source array
    STA ($12), Y        ; Write to destination array
    INY
    CPY $14             ; Compare current index (Y) with target length
    BNE @loop           ; If not equal, keep looping
@done:
    RTS

; ==========================================
; void arrayFill(byte* arr, byte value, byte length)
; ZP Params: $10,$11 = array ptr | $12 = value | $13 = length
; ==========================================
FUNC_arrayFill:
    LDA $13             ; Check if length is 0
    BEQ @done           ; If 0, exit

    LDA $12             ; Load the fill value into Accumulator
    LDY #$00
@loop:
    STA ($10), Y        ; Store the value in the array
    INY
    CPY $13             ; Have we reached the length?
    BNE @loop           ; Loop until Y == length
@done:
    RTS

; ==========================================
; byte arrayIndexOf(byte* arr, byte value, byte length)
; ZP Params: $10,$11 = array ptr | $12 = value | $13 = length
; Returns:   $10 = index (0-254) or 255 ($FF) if not found
; ==========================================
FUNC_arrayIndexOf:
    LDA $13             ; Check if length is 0
    BEQ @not_found

    LDY #$00
@loop:
    LDA ($10), Y        ; Read element from array
    CMP $12             ; Compare with the search value
    BEQ @found          ; If they match, we found it!

    INY
    CPY $13             ; Have we checked all elements?
    BNE @loop           ; If not, check next

@not_found:
    LDA #$FF            ; Return -1 (255 as unsigned byte)
    STA $10
    RTS

@found:
    STY $10             ; Return the index (Y register)
    RTS