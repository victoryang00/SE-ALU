import chisel3._
import chisel3.util._
import aes._
import chisel3.DontCare.:=
import instruction._
import chisel3.util.random._

case class SEALUParams() {
}

class SEALUIO extends Bundle {
  val in = new Bundle {
    val inst_data = Input(UInt(6.W))
    val input1_data = Input(UInt(128.W))
    val input2_data = Input(UInt(128.W))
    val inputcond_data = Input(UInt(128.W))
    // using param.key for now
    val check = Input(Bool())
    //    val change_key = Input(Bool())
    //    val new_key = Input(UInt(128.W))
    val valid = Input(Bool())
  }
  val output = new Bundle {
    val result = Output(UInt(128.W))
    val valid = Output(Bool())
  }
}

class SEALU(p: SEALUParams) extends Module {
  val io: SEALUIO = IO(new SEALUIO())
  val dec = 0.until(3).map(_ => Module(new MockEncDec(isEnc = false)))
  val enc = Module(new MockEncDec(isEnc = true))
  dec.foreach(_.io.input := 0.U)
  dec.foreach(_.io.key := 0.U)
  dec.foreach(_.io.valid := false.B)
  enc.io.input := 0.U
  enc.io.key := 0.U
  enc.io.valid := false.B
  io.output.valid := false.B
  io.output.result := 0.U

  when(io.in.valid) { // Only proceed if the current cycle's input is valid
    // Accessing cycle-specific data using the counter
    val inst = io.in.inst_data
    val input1 = io.in.input1_data
    val input2 = io.in.input2_data
    val inputCond = io.in.inputcond_data

    // Assume sealuop can perform operations based on inst, and inputs
    val sealuop = Module(ALU()) // Define Opcode module with appropriate IO
    sealuop.io.instruction := inst
    // AES dycryption takes 3 cycles, so we need to wait for the result?
    // big number operation
    dec.zip(Seq(input1, input2, inputCond)).foreach {
      case (d, i) =>
        d.io.input := i
        d.io.key := 0.U(128.W)
        d.io.valid := io.in.valid
    }

    sealuop.io.inArgs.valid := dec.map(_.io.ready).reduce(_ | _)
    sealuop.io.inArgs.bits.in1 := dec(0).io.output(127, 64)
    sealuop.io.inArgs.bits.in2 := dec(1).io.output(127, 64)
    sealuop.io.inArgs.bits.condition := dec(2).io.output(127, 64)

    //    printf("\n")
    //    printf("op1:%x\n", sealuop.io.inArgs.bits.in1)
    //    printf("op2:%x\n", sealuop.io.inArgs.bits.in2)
    //    printf("cond:%x\n", sealuop.io.inArgs.bits.condition)
    //    printf("inst:%b\n", sealuop.io.instruction)
    // Read from custom memory based on input addresses
    when(sealuop.io.out.valid) {
      //      printf("waiting for dycrypt\n")
      // AES encryption
      // Example operation and storing the result back to memory
      // This is where you would perform your encryption/decryption and store the result
      // Assuming we get a result that we want to store in memory
      when(io.in.check) {
        val bit64_randnum = PRNG(new MaxPeriodFibonacciLFSR(64, Some(scala.math.BigInt(64, scala.util.Random))))
        val padded_result = Cat(sealuop.io.out.bits, bit64_randnum)
        enc.io.input := padded_result
      }.otherwise {
        enc.io.input := sealuop.io.out.bits
      }
      enc.io.valid := true.B
      enc.io.key := 0.U
      io.output.result := enc.io.output
      io.output.valid := enc.io.ready
    }
  }


  //  when(reset.asBool) {
  //    printf("\n\nRESET SE ALU...\n\n")
  //  }
}