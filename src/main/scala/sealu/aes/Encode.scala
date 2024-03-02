package sealu.aes

import chisel3._
// decrypt the data

class Encode extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(128.W))
    val valid = Input(Bool())
    val key = Input(UInt(128.W))
    val output = Output(UInt(128.W))
    val ready = Output(Bool())
  })
  io.output := 0.U
  // instantiate the AES
  val aes_invcypher = Module(new AESCore)
  aes_invcypher.io.input := io.input
  aes_invcypher.io.valid := io.valid
  aes_invcypher.io.key := io.key.asTypeOf(Vec(16, UInt(8.W)))
  val key_gen = Module(new KeySchedule)
  key_gen.io.key_in := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_invcypher.io. key_schedule := key_gen.io.key_schedule
  aes_invcypher.io.key_valid := true.B
  aes_invcypher.io.is_enc := false.B
  io.ready := aes_invcypher.io.ready
  io.output := aes_invcypher.io.output
}