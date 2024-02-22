package sealu

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._


class SEALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SEALU"

  // change another key
  it should "be able to fit the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for (i <- 0 until 100) {
        dut.clock.step()
      }
      dut.io.output.result.expect(0.U)
      dut.io.output.valid.expect(false.B)
      dut.io.output.ready.expect(true.B)
      dut.io.output.counter.expect(100.U)
    }
  }
}