package sealu.aes

import chisel3._

class Decode extends Module {
  val io = IO(new Bundle {
    val input1 = Input(UInt(64.W))
    val input2 = Input(UInt(64.W))
    val cond = Input(UInt(64.W))
    val valid = Input(Bool())
    val output_input1 = Output(UInt(64.W))
    val output_input2 = Output(UInt(64.W))
    val output_cond = Output(UInt(64.W))
  })

  // instantiate the AES
  io.output_input1 := io.input1
  io.output_input2 := io.input2
  io.output_cond := io.cond
}
