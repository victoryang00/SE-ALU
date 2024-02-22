package sealu.aes

import chisel3._

class Encode extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(64.W))
    val valid = Input(Bool())

    val output = Output(UInt(64.W))
  })
  // instantiate the AES
  io.output := io.input
}