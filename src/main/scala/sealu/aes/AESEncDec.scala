package aes

import chisel3._
import chisel3.util.Pipe
// decrypt the data

class AESEncDec(isEnc: Boolean) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(128.W))
    val valid = Input(Bool())
    val key = Input(UInt(128.W))
    val output = Output(UInt(128.W))
    val ready = Output(Bool())
  })
  private val key_gen = Module(new KeySchedule)
  private val aes_cypher = Module(new AESCore)
  key_gen.io.key_in := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_cypher.io.is_enc := isEnc.B
  aes_cypher.io.input := io.input
  aes_cypher.io.valid := io.valid
  aes_cypher.io.key := io.key.asTypeOf(Vec(16, UInt(8.W)))
  aes_cypher.io.key_schedule := key_gen.io.key_schedule
  aes_cypher.io.key_valid := io.valid

  io.output := aes_cypher.io.output
  io.ready := aes_cypher.io.ready
}

class MockEncDec(isEnc: Boolean) extends AESEncDec(isEnc) {
  val outs = Pipe(io.valid, io.input, 10)
  io.output := outs.bits
  io.ready := outs.valid
}