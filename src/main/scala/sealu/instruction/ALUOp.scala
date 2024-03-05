package instruction

import chisel3._
import chisel3.util._

class OpInputArgs extends Bundle {
  val in1: UInt = UInt(64.W)
  val in2: UInt = UInt(64.W)
  val condition: UInt = UInt(64.W)
}

trait ALUOp {
  def apply(input: Valid[OpInputArgs]): Valid[UInt]
}


class ALU(ops: Map[BitPat, ALUOp]) extends Module {
  val io = IO(new Bundle {
    val inArgs = Input(Valid(new OpInputArgs))
    val instruction = Input(UInt())
    val out = Output(Valid(UInt(64.W)))
  })
  io.out.bits := 0.U
  io.out.valid := false.B
  for ((instruction, op) <- ops) {
    when(io.inArgs.fire && io.instruction === instruction) {
      io.out := op(io.inArgs)
    }
  }
}

object ALU {
  def doFunc(opInputArgs: Valid[OpInputArgs], op: OpInputArgs => UInt): Valid[UInt] = {
    val res = Wire(Valid(UInt(64.W)))
    res.bits := 0.U
    res.valid := false.B
    when(opInputArgs.fire) {
      res.bits := op(opInputArgs.bits)
      res.valid := true.B
    }
    res
  }

  def enc(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, _.in1)

  def add(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => args.in1 + args.in2)

  def sub(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => args.in1 - args.in2)

  def mul(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => args.in1 * args.in2)

  def muls(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => (args.in1.asSInt * args.in2.asSInt).asUInt)

  def lt(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => args.in1 < args.in2)

  def lts(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => (args.in1.asSInt < args.in2.asSInt).asUInt)

  def xor(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => args.in1 ^ args.in2)

  def or(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => args.in1 | args.in2)

  def and(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => args.in1 & args.in2)

  def cmov(opInputArgs: Valid[OpInputArgs]) =
    doFunc(opInputArgs, args => Mux(args.condition.orR, args.in1, args.in2))


  val ops = Map[BitPat, ALUOp](
    BitPat("b10????") -> enc,
    BitPat("b000000") -> add,
    BitPat("b000001") -> sub,
    BitPat("b000010") -> mul,
    BitPat("b000011") -> muls,
    BitPat("b000100") -> lt,
    BitPat("b000101") -> lts,
    BitPat("b000111") -> xor,
    BitPat("b001000") -> or,
    BitPat("b001001") -> and,
    BitPat("b01????") -> cmov,
  )

  def apply() = {
    new ALU(ops)
  }
}

