package sealu.aes

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._

class AESTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AES"

  it should "be decode correct" in {
    test(new Decode()) { dut =>
      dut.io.input1.poke(0.U)
      dut.io.input2.poke(1.U)
      dut.io.cond.poke(0.U)
      dut.io.valid.poke(true.B)
      dut.io.output_input1.expect(1.U)
      dut.io.output_input2.expect(1.U)
      dut.io.output_cond.expect(0.U)
    }
  }

  it should "be encode correct" in {
    test(new Encode()) { dut =>
      dut.io.input.poke(0.U)
      dut.io.valid.poke(true.B)
      dut.io.output.expect(0.U)
    }
  }
}
