package sealu.aes

import chisel3._
// encrypt the data
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

  val aes_cypher = Module(new AESCore)
  val aes_cypher1 = Module(new AESCore)
  val aes_cypher2 = Module(new AESCore)

  aes_cypher.io.input := io.input1
  aes_cypher.io.valid := io.valid
  aes_cypher.io.key :=  VecInit(0x00.U(8.W), 0x01.U(8.W), 0x02.U(8.W), 0x03.U(8.W), 0x04.U(8.W), 0x05.U(8.W), 0x06.U(8.W), 0x07.U(8.W), 0x08.U(8.W), 0x09.U(8.W), 0x0a.U(8.W), 0x0b.U(8.W), 0x0c.U(8.W), 0x0d.U(8.W), 0x0e.U(8.W), 0x0f.U(8.W))
  aes_cypher.io.is_enc := true.B
  aes_cypher1.io.input := io.input2
  aes_cypher1.io.valid := io.valid
  aes_cypher1.io.key :=  VecInit(0x00.U(8.W), 0x01.U(8.W), 0x02.U(8.W), 0x03.U(8.W), 0x04.U(8.W), 0x05.U(8.W), 0x06.U(8.W), 0x07.U(8.W), 0x08.U(8.W), 0x09.U(8.W), 0x0a.U(8.W), 0x0b.U(8.W), 0x0c.U(8.W), 0x0d.U(8.W), 0x0e.U(8.W), 0x0f.U(8.W))
  aes_cypher1.io.is_enc := true.B
  aes_cypher2.io.input := io.cond
  aes_cypher2.io.valid := io.valid
  aes_cypher2.io.key :=  VecInit(0x00.U(8.W), 0x01.U(8.W), 0x02.U(8.W), 0x03.U(8.W), 0x04.U(8.W), 0x05.U(8.W), 0x06.U(8.W), 0x07.U(8.W), 0x08.U(8.W), 0x09.U(8.W), 0x0a.U(8.W), 0x0b.U(8.W), 0x0c.U(8.W), 0x0d.U(8.W), 0x0e.U(8.W), 0x0f.U(8.W))
  aes_cypher2.io.is_enc := true.B

  // instantiate the AES

  io.output_input1 := aes_cypher.io.output
  io.output_input2 := aes_cypher1.io.output
  io.output_cond := aes_cypher2.io.output
}
