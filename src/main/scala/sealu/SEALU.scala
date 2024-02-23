package sealu

import chisel3._
import chisel3.util._
import aes._
import instruction._
import chisel3.util.random._

case class SEALUParams() {
  val init_cipher: Seq[BigInt] = Seq(0x00000000, 0x00000001, 0x00000002, 0x00000003, 0x00000004, 0x00000005, 0x00000006, 0x00000007, 0x00000008, 0x00000009, 0x0000000a, 0x0000000b, 0x0000000c, 0x0000000d, 0x0000000e, 0x0000000f)
  val init_plain: Seq[BigInt] = Seq(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f)
  val mem_size: Int = 16
  val count: Int = 100
}

class SEALUIO() extends Bundle {
  val in = new Bundle {
    val inst_data = Input(UInt(6.W))
    val input1_data = Input(UInt(64.W))
    val input2_data = Input(UInt(64.W))
    val inputcond_data = Input(UInt(64.W))
    val valid = Input(Bool())
  }
  val output = new Bundle {
    val result = Output(UInt(128.W))
    val valid = Output(Bool())
    val counter = Output(UInt(8.W))
  }
}

class SEALU(p: SEALUParams) extends Module {
  val io: SEALUIO = IO(new SEALUIO())
  val counter = new Counter(p.count)
  val ciphers = VecInit(p.init_cipher.map(_.U(128.W)))
  val plaintexts = VecInit(p.init_plain.map(_.U(64.W)))
  val dycrypt = Module(new Decode())
  val encrypt = Module(new Encode())

  dycrypt.io.input1 := 0.U
  dycrypt.io.input2 := 0.U
  dycrypt.io.cond := 0.U
  dycrypt.io.valid := false.B
  encrypt.io.input := 0.U
  encrypt.io.valid := false.B
  io.output.result := 0.U

  when(io.in.valid(counter.value)) { // Only proceed if the current cycle's input is valid
    // Accessing cycle-specific data using the counter
    val inst = io.in.inst_data
    val input1 = io.in.input1_data
    val input2 = io.in.input2_data
    val inputCond = io.in.inputcond_data

    // Assume sealuop can perform operations based on inst, and inputs
    val sealuop = Module(new Opcode()) // Define Opcode module with appropriate IO
    val cond_found = Wire(Bool())
    val op1_found = ciphers.contains(input1)
    val op2_found = ciphers.contains(input2)
    sealuop.io.inst := inst
    if (sealuop.io.inst == Instruction.CMOV) {
      cond_found := ciphers.contains(inputCond)
    } else {
      cond_found := true.B
    }
    val op1_idx = ciphers.indexWhere(ele => (ele === input1))
    val op2_idx = ciphers.indexWhere(ele => (ele === input2))
    val cond_idx = ciphers.indexWhere(ele => (ele === inputCond))
    // AES dycryption takes 3 cycles, so we need to wait for the result?
    // big number operation
    dycrypt.io.input1 := input1
    dycrypt.io.input2 := input2
    dycrypt.io.cond := inputCond
    dycrypt.io.valid := io.in.valid && cond_found && op1_found && op2_found

    sealuop.io.valid := io.in.valid && cond_found && op1_found && op2_found
    sealuop.io.input_1 := plaintexts(op1_idx)
    sealuop.io.input_2 := plaintexts(op2_idx)
    sealuop.io.cond := plaintexts(cond_idx)

    printf("\n")
    printf("op1:%x\n", sealuop.io.input_1)
    printf("op2:%x\n", sealuop.io.input_2)
    printf("cond:%x\n", sealuop.io.cond)
    printf("inst:%b\n", sealuop.io.inst)
    // Read from custom memory based on input addresses


    // AES encryption
    // Example operation and storing the result back to memory
    // This is where you would perform your encryption/decryption and store the result
    // Assuming we get a result that we want to store in memory
    val bit64_randnum = PRNG(new MaxPeriodFibonacciLFSR(64, Some(scala.math.BigInt(64, scala.util.Random))))
    val padded_result = Cat(sealuop.io.output, bit64_randnum)

    encrypt.io.input := padded_result
    encrypt.io.valid := true.B
    io.output.result := encrypt.io.output
    counter.inc() // Increment the counter each cycle to move to the next set of inputs
  }


  when(reset.asBool) {
    counter.reset()
  }.otherwise {
    when(counter.value === p.count.U - 1.U) {
      printf("\n-----back----\n")
    }
  }
  io.output.valid := true.B
  io.output.counter := counter.value
}