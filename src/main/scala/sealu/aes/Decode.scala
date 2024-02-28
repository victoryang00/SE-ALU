package sealu.aes

import chisel3._

class Decode extends Module {
  val io = IO(new Bundle {
    val input1 = Input(UInt(128.W))
    val input2 = Input(UInt(128.W))
    val cond = Input(UInt(128.W))
    val valid = Input(Bool())
    val output_input1 = Output(UInt(128.W))
    val output_input2 = Output(UInt(128.W))
    val output_cond = Output(UInt(128.W))
  })
  val aes_cypher = Module(new AES({
    io.in.data := io.input1
    io.in.valid := io.valid
    io
  }))

  // instantiate the AES
  io.output_input1 := io.input1
  io.output_input2 := io.input2
  io.output_cond := io.cond
  // pad with 128 bits
  when(io.valid) {
    printf("Decode: input1:%x, input2:%x, cond:%x\n", io.input1, io.input2, io.cond)
  }
}
