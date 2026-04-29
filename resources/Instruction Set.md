 # MOS 6502 Comprehensive Instruction Set Reference

 This document categorizes the 6502 instructions into functional groups and details the addressing modes used to access memory.

 ## 1. Addressing Modes

 The 6502 uses various ways to determine the effective address of an operand.

 | Mode | Syntax | Description |
 | :--- | :--- | :--- |
 | **Immediate** | `#$00` | The operand is a constant value included in the instruction. |
 | **Zero Page** | `$00` | Accesses the first 256 bytes of RAM (`$0000`–`$00FF`). Faster than absolute. |
 | **Zero Page,X** | `$00,X` | Zero Page address added to the X register (wraps within Page 0). |
 | **Zero Page,Y** | `$00,Y` | Zero Page address added to the Y register (wraps within Page 0). |
 | **Absolute** | `$0000` | Accesses a full 16-bit memory address. |
 | **Absolute,X** | `$0000,X` | 16-bit base address added to the X register. |
 | **Absolute,Y** | `$0000,Y` | 16-bit base address added to the Y register. |
 | **Indirect** | `($0000)` | Used by `JMP`. Reads the target address from the given location. |
 | **Relative** | `label` | Used by branches. An 8-bit signed offset (-128 to +127) from current PC. |
 | **Implicit** | (none) | The operand is implied by the instruction (e.g., `TAX`, `DEX`). |
 | **Accumulator** | `A` | The instruction operates directly on the Accumulator. |

 ---

 ## 2. Instruction Groups

 ### Load and Store Instructions

 Used to move data between registers and memory.

 | Mnemonic | Description | Example |
 | :--- | :--- | :--- |
 | **LDA** | Load Accumulator | `LDA #$01` |
 | **LDX** | Load X Register | `LDX $0200` |
 | **LDY** | Load Y Register | `LDY $00,X` |
 | **STA** | Store Accumulator | `STA $0200,X` |
 | **STX** | Store X Register | `STX $80` |
 | **STY** | Store Y Register | `STY $1234` |

 ### Arithmetic Instructions

 Perform addition, subtraction, and comparisons.

 | Mnemonic | Description | Example |
 | :--- | :--- | :--- |
 | **ADC** | Add with Carry | `ADC #$10` |
 | **SBC** | Subtract with Carry | `SBC $0200` |
 | **CMP** | Compare Accumulator | `CMP #$05` |
 | **CPX** | Compare X Register | `CPX $80` |
 | **CPY** | Compare Y Register | `CPY #$00` |

 ### Register Transfer Instructions

 Move data directly between internal registers.

 | Mnemonic | Description | Example |
 | :--- | :--- | :--- |
 | **TAX** | Transfer A to X | `TAX` |
 | **TXA** | Transfer X to A | `TXA` |
 | **TAY** | Transfer A to Y | `TAY` |
 | **TYA** | Transfer Y to A | `TYA` |
 | **TSX** | Transfer SP to X | `TSX` |
 | **TXS** | Transfer X to SP | `TXS` |

 ### Logical and Bitwise Instructions

 Perform boolean logic or bit manipulation.

 | Mnemonic | Description | Example |
 | :--- | :--- | :--- |
 | **AND** | Logical AND with A | `AND #$0F` |
 | **ORA** | Logical OR with A | `ORA $00` |
 | **EOR** | Exclusive OR with A | `EOR $0200` |
 | **BIT** | Bit Test | `BIT $0200` |
 | **ASL** | Arithmetic Shift Left | `ASL A` |
 | **LSR** | Logical Shift Right | `LSR $80` |
 | **ROL** | Rotate Left | `ROL A` |
 | **ROR** | Rotate Right | `ROR $0200,X` |

 ### Increments and Decrements

 Modify registers or memory values by one.

 | Mnemonic | Description | Example |
 | :--- | :--- | :--- |
 | **INC** | Increment Memory | `INC $0200` |
 | **INX** | Increment X | `INX` |
 | **INY** | Increment Y | `INY` |
 | **DEC** | Decrement Memory | `DEC $80` |
 | **DEX** | Decrement X | `DEX` |
 | **DEY** | Decrement Y | `DEY` |

 ### Branching and Control Flow

 Conditional jumps based on processor flags.

 | Mnemonic | Description | Condition |
 | :--- | :--- | :--- |
 | **BCC** | Branch Carry Clear | C = 0 |
 | **BCS** | Branch Carry Set | C = 1 |
 | **BEQ** | Branch Equal | Z = 1 |
 | **BNE** | Branch Not Equal | Z = 0 |
 | **BMI** | Branch Minus | N = 1 |
 | **BPL** | Branch Plus | N = 0 |
 | **BVC** | Branch Overflow Clear | V = 0 |
 | **BVS** | Branch Overflow Set | V = 1 |

 ### System and Stack Instructions

 Manage the stack, subroutines, and system state.

 | Mnemonic | Description | Example |
 | :--- | :--- | :--- |
 | **JMP** | Jump to Address | `JMP $8000` |
 | **JSR** | Jump to Subroutine | `JSR $FF00` |
 | **RTS** | Return from Subroutine | `RTS` |
 | **PHA** | Push A to Stack | `PHA` |
 | **PLA** | Pull A from Stack | `PLA` |
 | **PHP** | Push Status to Stack | `PHP` |
 | **PLP** | Pull Status from Stack | `PLP` |
 | **CLC/SEC** | Clear/Set Carry Flag | `CLC` |
 | **CLD/SED** | Clear/Set Decimal Mode | `CLD` |
 | **CLI/SEI** | Clear/Set Interrupt Disable | `SEI` |
 | **CLV** | Clear Overflow Flag | `CLV` |
 | **NOP** | No Operation | `NOP` |
 | **BRK** | Force Break (Interrupt) | `BRK` |
 | **RTI** | Return from Interrupt | `RTI` |
