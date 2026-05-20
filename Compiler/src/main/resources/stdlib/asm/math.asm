.setcpu "6502"

; ==========================================
; INTERNAL DRIVER VARIABLES (BSS)
; ==========================================
.segment "BSS"
RAND_SEED_LO: .res 1
RAND_SEED_HI: .res 1

.segment "CODE"
.export FUNC_abs
.export FUNC_max
.export FUNC_min
.export FUNC_multiply
.export FUNC_srand
.export FUNC_rand

; ==========================================
; abs(int value)
; ZP Params: $10 = value Low, $11 = value High
; Returns:   $10 = result Low, $11 = result High
; ==========================================
FUNC_abs:
    LDA $11             ; Load high byte to check sign
    BPL @done           ; If positive (bit 7 is 0), we are done

    ; Perform Two's Complement (Invert bits and add 1)
    LDA $10
    EOR #$FF            ; Invert low byte
    CLC
    ADC #$01            ; Add 1
    STA $10

    LDA $11
    EOR #$FF            ; Invert high byte
    ADC #$00            ; Add carry from low byte
    STA $11

@done:
    RTS

; ==========================================
; max(int a, int b)
; ZP Params: $10,$11 = A | $12,$13 = B
; ==========================================
FUNC_max:
    JSR _signed_compare ; Compare A and B
    BPL @done           ; If A >= B, A is already in $10,$11. Return.

    ; A < B, so we move B into the return registers ($10, $11)
    LDA $12
    STA $10
    LDA $13
    STA $11
@done:
    RTS

; ==========================================
; min(int a, int b)
; ZP Params: $10,$11 = A | $12,$13 = B
; ==========================================
FUNC_min:
    JSR _signed_compare ; Compare A and B
    BMI @done           ; If A < B, A is already in $10,$11. Return.

    ; A >= B, so we move B into the return registers ($10, $11)
    LDA $12
    STA $10
    LDA $13
    STA $11
@done:
    RTS

; ------------------------------------------
; INTERNAL ROUTINE: Signed 16-bit Comparison
; Compares $10,$11 with $12,$13.
; Sets the N flag to 0 if A >= B, and 1 if A < B.
; ------------------------------------------
_signed_compare:
    SEC
    LDA $10
    SBC $12             ; Subtract Low bytes
    LDA $11
    SBC $13             ; Subtract High bytes
    BVC @no_overflow    ; Branch if no overflow (V=0)
    EOR #$80            ; Overflow occurred! Flip the Negative (N) flag
@no_overflow:
    RTS

; ==========================================
; multiply(int a, int b)
; ZP Params: $10,$11 = A | $12,$13 = B
; Returns:   $10,$11 = 16-bit Result
; ==========================================
; Uses the Shift-and-Add algorithm (similar to how humans do long multiplication).
FUNC_multiply:
    ; We use $14 and $15 as the accumulator for the product
    LDA #$00
    STA $14
    STA $15
    LDX #16             ; 16 bits to process

@mul_loop:
    LSR $13             ; Shift B right (High byte)
    ROR $12             ; Shift B right (Low byte), carry goes into C flag
    BCC @skip_add       ; If C is 0, we don't add A to the product

    ; C is 1, add A to the product
    CLC
    LDA $14
    ADC $10
    STA $14
    LDA $15
    ADC $11
    STA $15

@skip_add:
    ASL $10             ; Shift A left (Low byte)
    ROL $11             ; Shift A left (High byte)
    DEX                 ; Decrement bit counter
    BNE @mul_loop       ; Loop until X is 0

    ; Move product from $14,$15 to the return registers $10,$11
    LDA $14
    STA $10
    LDA $15
    STA $11
    RTS

; ==========================================
; srand(int seed)
; ZP Params: $10,$11 = seed
; ==========================================
FUNC_srand:
    LDA $10
    ; A seed of 0 breaks a Galois LFSR, ensure it's at least 1
    BNE @store
    LDA $11
    BNE @store
    LDA #$01
    STA $10             ; Force low byte to 1 if user passed 0
@store:
    LDA $10
    STA RAND_SEED_LO
    LDA $11
    STA RAND_SEED_HI
    RTS

; ==========================================
; rand()
; Returns: $10,$11 = Pseudo-random number
; ==========================================
; Implements a 16-bit Galois LFSR with polynomial $B400.
; This generates a sequence of 65,535 random numbers before repeating.
FUNC_rand:
    LDA RAND_SEED_HI
    LSR A               ; Shift right
    STA RAND_SEED_HI

    LDA RAND_SEED_LO
    ROR A               ; Rotate right, catching the bit from HI
    STA RAND_SEED_LO

    BCC @no_eor         ; Se il bit uscito è 0, salta l'XOR (salta a @no_eor)

    ; The bit was 1, apply the polynomial mask ($B400)
    LDA RAND_SEED_HI
    EOR #$B4
    STA RAND_SEED_HI

@no_eor:
    ; Move the new seed to the return registers
    LDA RAND_SEED_LO
    STA $10
    LDA RAND_SEED_HI
    STA $11
    RTS