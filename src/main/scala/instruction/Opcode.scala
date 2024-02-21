package sealu.instruction

import chisel3._
import chisel3.util.BitPat

object Instructions {
  def ENC = BitPat("b10????")

  def ADD = BitPat("b000000")

  def CMOV = BitPat("b01????")
}

class Opcode extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(6.W))
    val valid = Input(Bool())

    val inst_out = Output(UInt(6.W))
  })
  val opcode = io.inst(4, 0)



}

