
import chisel3._
import chisel3.util._
import aes._
import chisel3.DontCare.:=
import instruction._
import chisel3.util.random._

trait Instruction;

case class SEALUParams() {
  val init_cipher: Seq[BigInt] = Seq(0x00000000, 0x00000001, 0x00000002, 0x00000003, 0x00000004, 0x00000005, 0x00000006, 0x00000007, 0x00000008, 0x00000009, 0x0000000a, 0x0000000b, 0x0000000c, 0x0000000d, 0x0000000e, 0x0000000f, 0x00000010, 0x00000011, 0x00000012, 0x00000013, 0x00000014, 0x00000015, 0x00000016, 0x00000017, 0x00000018, 0x00000019, 0x0000001a, 0x0000001b, 0x0000001c, 0x0000001d, 0x0000001e, 0x0000001f)
  val init_plain: Seq[BigInt] = Seq(0x1000, 0x0001, 0x1002, 0x1003, 0x1004, 0x1005, 0x1006, 0x1007, 0x1008, 0x1009, 0x100a, 0x100b, 0x100c, 0x100d, 0x100e, 0x100f, 0x1010, 0x1011, 0x1012, 0x1013, 0x1014, 0x1015, 0x1016, 0x1017, 0x1018, 0x1019, 0x101a, 0x101b, 0x101c, 0x101d, 0x101e, 0x101f)
  val key: String = "000102030405060708090a0b0c0d0e0f"
  val count: Int = 100
}

class SEALUIO extends Bundle {
  val in = new Bundle {
    val inst_data = Input(UInt(6.W))
    val input1_data = Input(UInt(128.W))
    val input2_data = Input(UInt(128.W))
    val inputcond_data = Input(UInt(128.W))
    // using param.key for now
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
  val dycrypt = Module(new Decode())
  val encrypt = Module(new Encode())

  dycrypt.io.input1 := 0.U
  dycrypt.io.input2 := 0.U
  dycrypt.io.cond := 0.U
  dycrypt.io.key := 0.U
  dycrypt.io.valid := false.B
  encrypt.io.input := 0.U
  encrypt.io.key := 0.U
  encrypt.io.valid := false.B
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
    dycrypt.io.input1 := input1
    dycrypt.io.input2 := input2
    dycrypt.io.cond := inputCond
    dycrypt.io.key := AESUtils.convert(p.key).U
    dycrypt.io.valid := io.in.valid

    sealuop.io.inArgs.valid := dycrypt.io.ready
    sealuop.io.inArgs.bits.in1 := dycrypt.io.output_input1(127, 64)
    sealuop.io.inArgs.bits.in2 := dycrypt.io.output_input2(127, 64)
    sealuop.io.inArgs.bits.condition := dycrypt.io.output_cond(127, 64)

    printf("\n")
    printf("op1:%x\n", sealuop.io.inArgs.bits.in1)
    printf("op2:%x\n", sealuop.io.inArgs.bits.in2)
    printf("cond:%x\n", sealuop.io.inArgs.bits.condition)
    printf("inst:%b\n", sealuop.io.instruction)
    // Read from custom memory based on input addresses
    when(sealuop.io.out.valid) {
      //      printf("waiting for dycrypt\n")
      // AES encryption
      // Example operation and storing the result back to memory
      // This is where you would perform your encryption/decryption and store the result
      // Assuming we get a result that we want to store in memory
      val bit64_randnum = PRNG(new MaxPeriodFibonacciLFSR(64, Some(scala.math.BigInt(64, scala.util.Random))))
      val padded_result = Cat(sealuop.io.out.bits, bit64_randnum)

      encrypt.io.input := padded_result
      encrypt.io.valid := true.B
      encrypt.io.key := AESUtils.convert(p.key).U
      io.output.result := encrypt.io.output
      io.output.valid := encrypt.io.ready
    }
  }


  //  when(reset.asBool) {
  //    printf("\n\nRESET SE ALU...\n\n")
  //  }
}