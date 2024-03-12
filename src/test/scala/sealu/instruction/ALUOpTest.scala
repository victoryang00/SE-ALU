package instruction

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._

class ALUOpTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Opcode"

  it should "add" in {
    doTest(0, (x, y, _) => x + y)
  }

  it should "sub" in {
    doTest(1, (x, y, _) => x - y,
      xRange = defaultRange.map(_ + 1).map(_ * defaultRange.max))
  }

  it should "mul" in {
    doTest(2, (x, y, _) => x * y)
  }

  //  it should "muls" in {
  //    doTest(3, (x, y, _) => x * y,
  //      xRange = defaultSRange, yRange = defaultSRange)
  //  }

  it should "lt" in {
    doTest(4, (x, y, _) => if (x < y) 1 else 0)
  }

  //  it should "lts" in {
  //    doTest(5, (x, y, _) => if (x < y) 1 else 0,
  //      xRange = defaultSRange, yRange = defaultSRange)
  //  }

  it should "xor" in {
    doTest(7, (x, y, _) => x ^ y)
  }

  it should "or" in {
    doTest(8, (x, y, _) => x | y)
  }

  it should "and" in {
    doTest(9, (x, y, _) => x & y)
  }

  it should "cmov" in {
    16.until(32).foreach(inst =>
      doTest(inst, (x, y, z) => if (z != 0) x else y, zRange = defaultRange))
  }

  val defaultRange = 0.to(16)
  val defaultSRange = (-defaultRange.max).to(defaultRange.max)
  val emptyRange = Seq(0)

  def toHex(v: Long) = ("x" + BigInt(v).toByteArray.map("%02x" format _).mkString)

  def doTest(inst: Int,
             result: (Int, Int, Int) => Int,
             xRange: Seq[Int] = defaultRange,
             yRange: Seq[Int] = defaultRange,
             zRange: Seq[Int] = emptyRange,
            ): TestResult = {
    test(ALU()) { dut =>
      xRange.map(x => yRange.map(y => zRange.map(z => {
        dut.io.inArgs.valid.poke(true.B)
        dut.io.inArgs.bits.in1.poke(toHex(x).U)
        dut.io.inArgs.bits.in2.poke(toHex(y).U)
        dut.io.inArgs.bits.condition.poke(toHex(z).U)
        dut.io.instruction.poke(toHex(inst).U)
        dut.io.out.valid.expect(true.B)
        dut.io.out.bits.expect(toHex(result(x, y, z)).U)
      })))
    }
  }
}
