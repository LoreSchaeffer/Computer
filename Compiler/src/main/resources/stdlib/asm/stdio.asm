.setcpu "6502"

; ==========================================
; INTERNAL DRIVER VARIABLES (GLOBAL RAM)
; ==========================================
.segment "BSS"
CURSOR_X:     .res 1
CURSOR_Y:     .res 1
LEADING_ZERO: .res 1
PUTCHAR_SAVE_Y: .res 1

; Temporary pointers in Zero Page used for 16-bit math
VRAM_PTR = $14 ; Uses $14 and $15

.segment "CODE"
.export FUNC_clearScreen
.export FUNC_setCursor
.export FUNC_printNewLine
.export FUNC_print
.export FUNC_println
.export FUNC_printInt
.export FUNC_printByte
.export FUNC_printChar
.export FUNC_printBool

; ==========================================
; clearScreen()
; ==========================================
FUNC_clearScreen:
    ; Reset cursor to 0,0
    LDA #$00
    STA CURSOR_X
    STA CURSOR_Y

    ; Clear the 768 bytes of the NameTable (32x24) with Space (ASCII $20)
    LDA #$20
    LDY #$00
@clear_loop:
    STA $3000, Y
    STA $3100, Y
    STA $3200, Y
    INY
    BNE @clear_loop
    RTS

; ==========================================
; setCursor(byte x, byte y)
; ZP Params: $10 = x, $11 = y
; ==========================================
FUNC_setCursor:
    LDA $10
    STA CURSOR_X
    LDA $11
    STA CURSOR_Y
    RTS

; ==========================================
; printNewLine()
; ==========================================
FUNC_printNewLine:
    LDA #$00
    STA CURSOR_X        ; Carriage return
    INC CURSOR_Y        ; Line feed

    ; Optional wrapping: if Y >= 24, reset to top
    LDA CURSOR_Y
    CMP #24
    BCC @no_wrap
    LDA #$00
    STA CURSOR_Y
@no_wrap:
    RTS

; ==========================================
; print(string text)
; ZP Params: $10 = string pointer Low, $11 = High
; ==========================================
FUNC_print:
    LDY #$00
@print_loop:
    LDA ($10), Y        ; Read character from user string
    BEQ @print_done     ; If null-terminator (0), exit loop
    JSR _putchar        ; Print character and advance cursor
    INY
    JMP @print_loop
@print_done:
    RTS

; ==========================================
; println(string text)
; ==========================================
FUNC_println:
    JSR FUNC_print
    JMP FUNC_printNewLine

; ==========================================
; printChar(char value)
; ZP Params: $10 = char value
; ==========================================
FUNC_printChar:
    LDA $10
    JSR _putchar
    RTS

; ==========================================
; printBool(boolean value)
; ZP Params: $10 = boolean value (0 = false, 1 = true)
; ==========================================
FUNC_printBool:
    LDA $10
    BEQ @is_false
@is_true:
    LDA #<str_true
    STA $10
    LDA #>str_true
    STA $11
    JMP FUNC_print
@is_false:
    LDA #<str_false
    STA $10
    LDA #>str_false
    STA $11
    JMP FUNC_print

; ==========================================
; printByte(byte value)
; ZP Params: $10 = byte value
; ==========================================
FUNC_printByte:
    ; A byte is just a 16-bit int with the high byte set to 0.
    ; We reuse the printInt routine to save ROM space!
    LDA #$00
    STA $11
    JMP FUNC_printInt

; ==========================================
; printInt(int value)
; ZP Params: $10 = value Low, $11 = value High
; ==========================================
; Converts a 16-bit binary number to decimal ASCII and prints it.
; Uses a highly optimized "Powers of 10 Subtraction" algorithm.
FUNC_printInt:
    LDA #$01
    STA LEADING_ZERO    ; Set flag to 1 (true) to suppress leading zeros

    LDX #$00            ; X acts as the index for our powers of 10 table
@digit_loop:
    LDY #$00            ; Y counts how many times we subtract the power of 10

@sub_loop:
    ; Try subtracting pow10_lo[x] and pow10_hi[x] from $10/$11
    LDA $10
    SEC
    SBC pow10_lo, X
    PHA                 ; Save low byte result
    LDA $11
    SBC pow10_hi, X
    BCC @restore        ; If carry clear, underflow occurred (subtracted too much)

    ; Subtraction succeeded, save the new value back to $10/$11
    STA $11
    PLA
    STA $10
    INY                 ; Increment digit counter
    JMP @sub_loop

@restore:
    PLA                 ; Discard the saved low byte, it underflowed

    ; Y now contains the decimal digit (0-9)
    CPY #$00
    BNE @print_digit    ; If Y > 0, it's a non-zero digit, definitely print it

    ; If Y == 0, check if it's the last digit (we MUST print "0" for the number 0)
    CPX #$04
    BEQ @print_digit

    ; If Y == 0 and it's not the last digit, check if we are still suppressing zeros
    LDA LEADING_ZERO
    BNE @next_digit     ; Skip printing if it's a leading zero

@print_digit:
    LDA #$00
    STA LEADING_ZERO    ; Turn off leading zero suppression once a digit is printed

    TYA                 ; Move digit to Accumulator
    CLC
    ADC #'0'            ; Convert binary digit (0-9) to ASCII ('0'-'9')
    JSR _putchar        ; Print it to screen

@next_digit:
    INX
    CPX #$05            ; Have we processed all 5 powers of 10?
    BNE @digit_loop
    RTS

; ------------------------------------------
; INTERNAL ROUTINE: putchar
; Prints the character in Accumulator and advances cursor
; ------------------------------------------
_putchar:
    STY PUTCHAR_SAVE_Y

    PHA                 ; Save the character to the stack
    JSR _calc_vram_ptr  ; Calculate VRAM pointer based on CURSOR_X and CURSOR_Y
    PLA                 ; Restore the character

    LDY #$00
    STA (VRAM_PTR), Y   ; Write character to VRAM

    ; Advance cursor
    INC CURSOR_X
    LDA CURSOR_X
    CMP #32             ; If X reached screen width (32)
    BNE @done
    JSR FUNC_printNewLine
@done:
    LDY PUTCHAR_SAVE_Y
    RTS

; ------------------------------------------
; INTERNAL ROUTINE: calc_vram_ptr
; Calculates VRAM_PTR = $3000 + (CURSOR_Y * 32) + CURSOR_X
; ------------------------------------------
_calc_vram_ptr:
    LDA #$00
    STA VRAM_PTR        ; Low byte starts at 0
    LDA CURSOR_Y
    LSR A               ; Y / 2
    LSR A               ; Y / 4
    LSR A               ; Y / 8
    CLC
    ADC #$30            ; High Byte: Base $3000
    STA VRAM_PTR+1

    LDA CURSOR_Y
    ASL A               ; Y * 2
    ASL A               ; Y * 4
    ASL A               ; Y * 8
    ASL A               ; Y * 16
    ASL A               ; Y * 32
    CLC
    ADC CURSOR_X        ; Add X offset
    STA VRAM_PTR
    BCC @skip_carry
    INC VRAM_PTR+1
@skip_carry:
    RTS

; ==========================================
; READ-ONLY DATA (RODATA)
; ==========================================
.segment "RODATA"

str_true:  .asciiz "true"
str_false: .asciiz "false"

; Powers of 10 table used for 16-bit binary-to-decimal conversion
; Values: 10000, 1000, 100, 10, 1
pow10_lo: .byte <10000, <1000, <100, <10, <1
pow10_hi: .byte >10000, >1000, >100, >10, >1