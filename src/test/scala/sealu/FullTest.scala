package sealu

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._


class SEALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SEALU"

  it should "be able to add a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      for (i <- 0 until 100) {
        dut.io.in.inst_data.poke(0.U)
        dut.io.in.input1_data.poke(1.U) // decrypted
        dut.io.in.input2_data.poke(1.U) // decrypted
        dut.io.in.inputcond_data.poke(0.U)
        dut.io.in.valid.poke(true.B)
        dut.clock.step()
        dut.io.output.result.expect(0.U) // encrypted
        dut.io.output.valid.expect(false.B)
        dut.io.output.counter.expect(100.U)
      }

    }
  }
  // cmov a, b, c  a = b if b else c
  // add a, b, c
  // sub a, b, c
  it should "be able to cmov a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for (i <- 0 until 100) {
        dut.io.in.inst_data.poke("b010000".U)
        dut.io.in.input1_data.poke(1.U) // decrypted
        dut.io.in.input2_data.poke(1.U) // decrypted
        dut.io.in.inputcond_data.poke(0.U)
        dut.io.in.valid.poke(true.B)
        dut.clock.step()
        dut.io.output.result.expect(0.U) // encrypted
        dut.io.output.valid.expect(false.B)
        dut.io.output.counter.expect(100.U)
      }
    }
  }
}