package sealu.aes

import chisel3._

class Encode extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(128.W))
    val valid = Input(Bool())

    val output = Output(UInt(128.W))
  })
  // instantiate the AES
  io.output := io.input
  // pad with 128 bits
  when(io.valid) {
    printf("Encode: input:%x\n", io.input)
  }
}