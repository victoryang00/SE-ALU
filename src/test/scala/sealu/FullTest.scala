import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import aes._

class SEALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SEALU"

  it should "do add" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
      dut.io.in.check.poke(false.B)
      dut.io.in.inst_data.poke("b000000".U)
      dut.io.in.input1_data.poke(12.U)
      dut.io.in.input2_data.poke(13.U)
      dut.io.in.valid.poke(true.B)
      dut.clock.step(20)
      dut.io.output.result.expect(25.U)
      dut.io.output.valid.expect(true.B)
    }
  }
}