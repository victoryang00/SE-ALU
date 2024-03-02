package sealu.instruction

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._

class OpcodeTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Opcode"

  it should "be able 0+1=1" in {
    test(new Opcode()) { dut =>
      dut.io.input_1.poke(0.U)
      dut.io.input_2.poke(1.U)
      dut.io.cond.poke(0.U)
      dut.io.inst.poke(0.U)
      dut.io.valid.poke(true.B)
      dut.io.output.expect(1.U)
    }
  }

  it should "be able 1 cmov 1 2" in {
    test(new Opcode()) { dut =>
      dut.io.input_1.poke(0.U)
      dut.io.input_2.poke(1.U)
      dut.io.cond.poke(0.U)
      dut.io.inst.poke("b010000".U)
      dut.io.valid.poke(true.B)
      dut.io.output.expect(1.U)
    }
  }

  it should "be able 1  1 2" in {
    test(new Opcode()) { dut =>
      dut.io.input_1.poke(0.U)
      dut.io.input_2.poke(1.U)
      dut.io.cond.poke(0.U)
      dut.io.inst.poke("b010000".U)
      dut.io.valid.poke(true.B)
      dut.io.output.expect(1.U)
    }
  }
}
