.setcpu "6502"

.segment "CODE"
.export FUNC_playPulse1
.export FUNC_stopPulse1
.export FUNC_playPulse2
.export FUNC_stopPulse2
.export FUNC_playTriangle
.export FUNC_stopTriangle
.export FUNC_playNoise
.export FUNC_stopNoise
.export FUNC_stopAudio

; ==========================================
; playPulse1(int freq, byte volume)
; ZP Params: $10,$11 = freq | $12 = volume
; APU Regs:  $5000,$5001 = freq | $5002 = volume
; ==========================================
FUNC_playPulse1:
    LDA $10             ; Load Frequency Low Byte
    STA $5000           ; Write to APU Pulse 1 Freq Low
    LDA $11             ; Load Frequency High Byte
    STA $5001           ; Write to APU Pulse 1 Freq High
    LDA $12             ; Load Volume
    STA $5002           ; Write to APU Pulse 1 Volume
    RTS

; ==========================================
; void stopPulse1()
; ==========================================
FUNC_stopPulse1:
    LDA #$00
    STA $5002           ; Write 0 to Pulse 1 Volume
    RTS

; ==========================================
; playPulse2(int freq, byte volume)
; ZP Params: $10,$11 = freq | $12 = volume
; APU Regs:  $5004,$5005 = freq | $5006 = volume
; ==========================================
FUNC_playPulse2:
    LDA $10             ; Load Frequency Low Byte
    STA $5004           ; Write to APU Pulse 2 Freq Low
    LDA $11             ; Load Frequency High Byte
    STA $5005           ; Write to APU Pulse 2 Freq High
    LDA $12             ; Load Volume
    STA $5006           ; Write to APU Pulse 2 Volume
    RTS

; ==========================================
; void stopPulse2()
; ==========================================
FUNC_stopPulse2:
    LDA #$00
    STA $5006           ; Write 0 to Pulse 2 Volume
    RTS

; ==========================================
; playTriangle(int freq, byte volume)
; ZP Params: $10,$11 = freq | $12 = volume
; APU Regs:  $5008,$5009 = freq | $500A = volume
; ==========================================
FUNC_playTriangle:
    LDA $10             ; Load Frequency Low Byte
    STA $5008           ; Write to APU Triangle Freq Low
    LDA $11             ; Load Frequency High Byte
    STA $5009           ; Write to APU Triangle Freq High
    LDA $12             ; Load Volume
    STA $500A           ; Write to APU Triangle Volume
    RTS

; ==========================================
; void stopTriangle()
; ==========================================
FUNC_stopTriangle:
    LDA #$00
    STA $500A           ; Write 0 to Triangle Volume
    RTS

; ==========================================
; playNoise(byte period, byte volume)
; ZP Params: $10 = period | $11 = volume
; APU Regs:  $500C = period | $500D = volume
; ==========================================
FUNC_playNoise:
    LDA $10             ; Load Noise Period (8-bit)
    STA $500C           ; Write to APU Noise Period
    LDA $11             ; Load Volume
    STA $500D           ; Write to APU Noise Volume
    RTS

; ==========================================
; void stopNoise()
; ==========================================
FUNC_stopNoise:
    LDA #$00
    STA $500D           ; Write 0 to Noise Volume
    RTS

; ==========================================
; stopAudio()
; Mutes all channels by setting their volumes to 0
; ==========================================
FUNC_stopAudio:
    LDA #$00            ; Zero volume
    STA $5002           ; Mute Pulse 1
    STA $5006           ; Mute Pulse 2
    STA $500A           ; Mute Triangle
    STA $500D           ; Mute Noise
    RTS