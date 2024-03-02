package sealu.aes

import chisel3._
import chisel3.util._

// encrypt the data
class Decode extends Module {
  val io = IO(new Bundle {
    val input1 = Input(UInt(128.W))
    val input2 = Input(UInt(128.W))
    val cond = Input(UInt(128.W))
    val key = Input(UInt(128.W))
    val valid = Input(Bool())
    val output_input1 = Output(UInt(128.W))
    val output_input2 = Output(UInt(128.W))
    val output_cond = Output(UInt(128.W))
    val ready = Output(Bool())
  })
  io.ready := false.B
  val aes_invcypher = Module(new AESCore)
  val aes_invcypher1 = Module(new AESCore)
  val aes_invcypher2 = Module(new AESCore)
  aes_invcypher.io.input := io.input1
  aes_invcypher.io.valid := io.valid
  aes_invcypher.io.key := io.key.asTypeOf(Vec(16, UInt(8.W)))
  val key_gen = Module(new KeySchedule)
  key_gen.io.key_in := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_invcypher.io.key_schedule := key_gen.io.key_schedule
  aes_invcypher.io.key_valid := true.B

  aes_invcypher.io.is_enc := true.B
  aes_invcypher1.io.input := io.input2
  aes_invcypher1.io.valid := io.valid
  aes_invcypher1.io.key := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_invcypher1.io.is_enc := true.B
  aes_invcypher1.io.key_schedule := key_gen.io.key_schedule
  aes_invcypher1.io.key_valid := true.B

  aes_invcypher2.io.input := io.cond
  aes_invcypher2.io.valid := io.valid
  aes_invcypher2.io.key := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_invcypher2.io.is_enc := true.B
  aes_invcypher2.io.key_schedule := key_gen.io.key_schedule
  aes_invcypher2.io.key_valid := true.B

  io.ready := aes_invcypher.io.ready && aes_invcypher1.io.ready && aes_invcypher2.io.ready
  printf("aes_invcypher.io.ready: %b %x\n", aes_invcypher.io.ready, aes_invcypher.io.output)
  printf("aes_invcypher1.io.ready: %b %x\n", aes_invcypher1.io.ready, aes_invcypher1.io.output)
  printf("aes_invcypher2.io.ready: %b %x\n", aes_invcypher2.io.ready, aes_invcypher2.io.output)
  // instantiate the AES
  io.output_input1 := aes_invcypher.io.output
  io.output_input2 := aes_invcypher1.io.output
  io.output_cond := aes_invcypher2.io.output
}
