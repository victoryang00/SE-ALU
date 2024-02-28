package sealu.aes

import chisel3._
// decrypt the data

class Encode extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(128.W))
    val valid = Input(Bool())

    val output = Output(UInt(128.W))
  })
  // instantiate the AES
  val aes_invcypher = Module(new AESCore)
  aes_invcypher.io.input := io.input
  aes_invcypher.io.valid := io.valid
  aes_invcypher.io.key :=  VecInit(0x00.U(8.W), 0x01.U(8.W), 0x02.U(8.W), 0x03.U(8.W), 0x04.U(8.W), 0x05.U(8.W), 0x06.U(8.W), 0x07.U(8.W), 0x08.U(8.W), 0x09.U(8.W), 0x0a.U(8.W), 0x0b.U(8.W), 0x0c.U(8.W), 0x0d.U(8.W), 0x0e.U(8.W), 0x0f.U(8.W))
  aes_invcypher.io.is_enc := false.B
  io.output := io.input
  // pad with 128 bits
}