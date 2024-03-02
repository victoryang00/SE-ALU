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

  val aes_cypher = Module(new AESCore)
  val aes_cypher1 = Module(new AESCore)
  val aes_cypher2 = Module(new AESCore)
  aes_cypher.io.input := io.input1
  aes_cypher.io.valid := io.valid
  aes_cypher.io.key := io.key.asTypeOf(Vec(16, UInt(8.W)))
  val key_gen = Module(new KeySchedule)
  key_gen.io.key_in := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_cypher.io.key_schedule := key_gen.io.key_schedule
  aes_cypher.io.key_valid := true.B

  aes_cypher.io.is_enc := true.B
  aes_cypher1.io.input := io.input2
  aes_cypher1.io.valid := io.valid
  aes_cypher1.io.key := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_cypher1.io.is_enc := true.B
  aes_cypher1.io.key_schedule := key_gen.io.key_schedule
  aes_cypher1.io.key_valid := true.B

  aes_cypher2.io.input := io.cond
  aes_cypher2.io.valid := io.valid
  aes_cypher2.io.key := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_cypher2.io.is_enc := true.B
  aes_cypher2.io.key_schedule := key_gen.io.key_schedule
  aes_cypher2.io.key_valid := true.B

  io.ready := aes_cypher.io.ready && aes_cypher1.io.ready && aes_cypher2.io.ready
  printf("aes_cypher.io.ready: %b %x\n", aes_cypher.io.ready, aes_cypher.io.output)
  printf("aes_cypher1.io.ready: %b %x\n", aes_cypher1.io.ready, aes_cypher1.io.output)
  printf("aes_cypher2.io.ready: %b %x\n", aes_cypher2.io.ready, aes_cypher2.io.output)
  // instantiate the AES
  io.output_input1 := aes_cypher.io.output
  io.output_input2 := aes_cypher1.io.output
  io.output_cond := aes_cypher2.io.output
}
