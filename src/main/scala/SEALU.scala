package sealu

import chisel3._
import chisel3.util._
import aes._
import instruction._
import chisel3.util.random._

class SEALUInput extends Bundle {
  val in = new Bundle {
    val input_data = Valid(UInt(8.W))
  }
  val out = new Bundle {
    val output_data = Valid(UInt(8.W))
  }

}

class SEALU extends Module {
  val io = IO(new SEALUInput)

  // cypher text initial value
  val cypher_text = RegInit(0.U(8.W))
}