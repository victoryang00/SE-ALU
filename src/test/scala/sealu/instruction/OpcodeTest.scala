package instruction

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._

class OpcodeTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Opcode"

  it should "be able 0+1=1" in {
    test(ALU()) { dut =>
      dut.io.inArgs.bits.in1.poke(0.U)
      dut.io.inArgs.bits.in2.poke(1.U)
      dut.io.inArgs.bits.condition.poke(0.U)
      dut.io.instruction.poke(0.U)
      dut.io.inArgs.valid.poke(1.U)
      dut.clock.step()
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(1.U)
    }
  }

  it should "be able 1 cmov 1 2" in {
    test(ALU()) { dut =>
      dut.io.inArgs.bits.in1.poke(0.U)
      dut.io.inArgs.bits.in2.poke(1.U)
      dut.io.inArgs.bits.condition.poke(0.U)
      dut.io.instruction.poke("b010000".U)
      dut.io.inArgs.valid.poke(true.B)
      dut.clock.step()
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(1.U)
    }
  }

  it should "be able 1  1 2" in {
    test(ALU()) { dut =>
      dut.io.inArgs.bits.in1.poke(0.U)
      dut.io.inArgs.bits.in2.poke(1.U)
      dut.io.inArgs.bits.condition.poke(0.U)
      dut.io.instruction.poke("b010000".U)
      dut.io.inArgs.valid.poke(true.B)
      dut.clock.step()
      dut.io.out.valid.expect(true.B)
      dut.io.out.bits.expect(1.U)
    }
  }
}
