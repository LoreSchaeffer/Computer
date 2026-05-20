.setcpu "6502"

.segment "CODE"
.export FUNC_strLen
.export FUNC_strCopy
.export FUNC_strConcat
.export FUNC_strCompare

; ==========================================
; byte strLen(string text)
; ZP Params: $10,$11 = string pointer
; Returns:   $10 = length (0-255)
; ==========================================
FUNC_strLen:
    LDY #$00
@loop:
    LDA ($10), Y        ; Read character
    BEQ @done           ; If null-terminator (0x00) found, exit loop
    INY                 ; Increment length
    BNE @loop           ; Continue until 255 (max length)
@done:
    STY $10             ; Store the result length in $10 for return
    RTS

; ==========================================
; void strCopy(string src, string dest)
; ZP Params: $10,$11 = src pointer, $12,$13 = dest pointer
; ==========================================
FUNC_strCopy:
    LDY #$00
@loop:
    LDA ($10), Y        ; Read from source
    STA ($12), Y        ; Write to destination
    BEQ @done           ; If we just wrote the null-terminator, we are done
    INY
    BNE @loop           ; Loop up to 255 chars
@done:
    RTS

; ==========================================
; void strConcat(string a, string b, string dest)
; ZP Params: $10,$11 = a | $12,$13 = b | $14,$15 = dest
; ==========================================
FUNC_strConcat:
    LDY #$00
@copy_a:
    LDA ($10), Y        ; Read from string 'a'
    BEQ @setup_b        ; If end of 'a', skip writing 0 and prepare for 'b'
    STA ($14), Y        ; Write to 'dest'
    INY
    BNE @copy_a

@setup_b:
    ; Pointer Math: Advance the 'dest' pointer ($14,$15) by Y bytes
    ; This allows us to reset Y to 0 for string 'b'
    TYA                 ; Transfer Y to Accumulator
    CLC
    ADC $14             ; Add to low byte of dest pointer
    STA $14
    BCC @skip_inc       ; If carry clear, high byte is fine
    INC $15             ; Otherwise, increment high byte of dest pointer
@skip_inc:

    LDY #$00            ; Reset Y for the second string
@copy_b:
    LDA ($12), Y        ; Read from string 'b'
    STA ($14), Y        ; Write to 'dest' (which is now offset correctly)
    BEQ @done           ; If null-terminator, we are done
    INY
    BNE @copy_b
@done:
    RTS

; ==========================================
; boolean strCompare(string a, string b)
; ZP Params: $10,$11 = a | $12,$13 = b
; Returns:   $10 = 1 (true) or 0 (false)
; ==========================================
FUNC_strCompare:
    LDY #$00
@loop:
    LDA ($10), Y        ; Read char from 'a'
    CMP ($12), Y        ; Compare with char from 'b'
    BNE @mismatch       ; If different, strings don't match

    ; Characters match. Are they the null-terminator?
    LDA ($10), Y
    BEQ @match          ; If it's 0x00, we reached the end successfully!

    INY
    BNE @loop           ; Keep checking

@mismatch:
    LDA #$00            ; Return 0 (false)
    STA $10
    RTS

@match:
    LDA #$01            ; Return 1 (true)
    STA $10
    RTS