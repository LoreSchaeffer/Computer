# Lycoris-8 & Toolchain: Project Plan

## Phase 1: Core Architecture & Processor (Completed ✅)
The fundamental infrastructure is established. The simulation accurately represents logic gates, signal propagation, and the instruction set.

* [x] **Hardware Simulation Engine:** Logic gate simulation, signal propagation (`SimulationContext`), wiring, and component architecture.
* [x] **MOS 6502 CPU Implementation:**
    * [x] Internal Registers (A, X, Y, PC, SP, Status).
    * [x] Internal Bus Multiplexer and ALU operations.
    * [x] Complete Addressing Modes (including historical hardware bugs like the indirect jump page wrap).
    * [x] Base Instruction Set (151 legal opcodes).
* [x] **Lycoris Assembler:**
    * [x] Lexical Analysis (Tokenization).
    * [x] Pass 1: Symbol Table resolution and directives (`* =`).
    * [x] Pass 2: Machine Code Generation (Little-Endian formatting, relative offsets).
* [x] **Emulator Runner (CLI Engine):**
    * [x] Dynamic binary loading.
    * [x] Watchdog mechanism (`maxSteps` timeout).
    * [x] Advanced logging integration (SLF4J, Logback) with runtime debug toggling.
    * [x] Universal HALT detection (`BRK` or infinite self-loops).

---

## Phase 2: Peripherals (In Progress 🚀)
Transforming the raw microprocessor into an interactive computer utilizing Memory-Mapped I/O.

* [ ] **Memory Map Restructuring:** Formalize memory segments (Zero Page, Stack, RAM, Video RAM, I/O Registers, ROM).
* [ ] **Graphics Processing Unit (Simulated GPU):**
    * [ ] Intercept VRAM memory writes.
    * [ ] Implement a Java `JFrame` or `Canvas` with a linear Framebuffer (e.g., 128x128 pixels).
    * [ ] (Optional) Implement an indexed 16/256 color palette.
* [ ] **Input Controller:**
    * [ ] Map Java KeyListeners to specific memory addresses (e.g., `$4000`).
* [ ] **Audio Processing Unit (Simulated APU):**
    * [ ] Map audio registers (Frequency, Volume, Waveform type).
    * [ ] Implement sound generation utilizing `javax.sound.sampled` API.
* [ ] **Main Loop Synchronization:**
    * [ ] Synchronize CPU clock cycles with screen refresh rates (60 FPS target).

---

## Phase 3: High-Level Custom Compiler (Planned ⏳)
Development of a custom high-level programming language (e.g., "LycoScript" or "Mini-C") targeting the Lycoris-8 architecture.

* [ ] **Language Design:** Define syntax, strict data types (`byte`, `word`), arithmetic operators, and control structures (`if`, `while`, `for`).
* [ ] **Compiler Front-end:**
    * [ ] Lexical Analysis (Advanced Lexer).
    * [ ] Syntactic Analysis (Parser) and Abstract Syntax Tree (AST) construction.
* [ ] **6502 Target Architecture Adaptation:**
    * [ ] Implement a Software Stack in the Zero Page for local variables and function parameters.
    * [ ] Implement 16-bit "Virtual Registers" within the Zero Page.
* [ ] **Code Generation (Back-end):**
    * [ ] Translate AST nodes into native Lycoris Assembler code.
* [ ] **Standard Library (Built-ins):**
    * [ ] Provide pre-mapped native functions (e.g., `print()`, `drawPixel()`, `playSound()`).

---

## Phase 4: Enterprise Tooling & Ecosystem (Future Scope 🌟)
Enhancing the development experience and system robustness.

* [ ] **Graphical IDE & Debugger:** Build a Java-based UI to monitor RAM, registers, and pin states in real-time.
* [ ] **Lyco-OS / Bootloader:** Develop a minimal ROM-based operating system to display a splash screen before executing the user payload.
* [ ] **Demonstration Software:** Develop a full-featured application (e.g., Pong, Snake) entirely in LycoScript to validate the entire toolchain.