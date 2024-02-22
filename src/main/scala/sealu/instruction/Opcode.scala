package sealu.instruction

import chisel3._
import chisel3.util._

// 01 -> CMOV
// 10 -> ENC
// 00 -> ADD, SUB, MUL, MULS, LT, LTS, XOR, OR, AND
object Instruction {
  def ENC: BitPat = BitPat("b10????")

  def ADD: BitPat = BitPat("b000000")
  def SUB: BitPat = BitPat("b000001")
  def MUL: BitPat = BitPat("b000010")
  def MULS: BitPat = BitPat("b000011")
  def LT: BitPat = BitPat("b000100")
  def LTS: BitPat = BitPat("b000101")
  def XOR: BitPat = BitPat("b000111")
  def OR: BitPat = BitPat("b001000")
  def AND: BitPat = BitPat("b001001")
  def CMOV: BitPat = BitPat("b01????")
}

class Opcode extends Module {
  val io = IO(new Bundle {
    val input_1: UInt = Input(UInt(64.W))
    val input_2: UInt = Input(UInt(64.W))
    val cond: UInt = Input(UInt(64.W))

    val inst: UInt = Input(UInt(6.W))
    val valid: Bool = Input(Bool())

    val output: UInt = Output(UInt(64.W))
  })
  private val data = io.inst(3, 0)
  private val op = io.inst(5, 4)

  when(io.valid) {
    when(op === 1.U) {
      when(io.cond =/= 0.U) {
        printf("Inst:cmova\n")
        io.output := io.input_1
      }.otherwise {
        printf("Inst:cmovb\n")
        io.output := io.input_2
      }
    }.elsewhen(op === 2.U) {
      printf("Inst:enc\n")
      io.output := io.input_1
    }.otherwise {
      when(data === 0.U) {
        printf("Inst:add\n")
        io.output := io.input_1 + io.input_2
      }.elsewhen(data === 1.U) {
        printf("Inst:sub\n")
        io.output := io.input_1 - io.input_2
      }.elsewhen(data === 2.U) {
        printf("Inst:mul\n")
        io.output := io.input_1 * io.input_2
      }.elsewhen(data === 3.U) {
        printf("Inst:muls\n")
        io.output := (io.input_1.asSInt * io.input_2.asSInt).asUInt
      }.elsewhen(data === 4.U) {
        printf("Inst:lt\n")
        io.output := (io.input_1 < io.input_2).asUInt
      }.elsewhen(data === 5.U) {
        printf("Inst:lts\n")
        io.output := (io.input_1.asSInt < io.input_2.asSInt).asUInt
      }.elsewhen(data === 7.U) {
        printf("Inst:xor\n")
        io.output := io.input_1 ^ io.input_2
      }.elsewhen(data === 8.U) {
        printf("Inst:or\n")
        io.output := io.input_1 | io.input_2
      }.elsewhen(data === 9.U) {
        printf("Inst:and\n")
        io.output := io.input_1 & io.input_2
      }.otherwise {
        printf("Inst:unknown\n")
        io.output := 0.U
      }
    }
  }.otherwise {
    io.output := 0.U
  }


}

