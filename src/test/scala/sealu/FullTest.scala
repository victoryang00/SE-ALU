import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import aes._

class SEALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SEALU"

  it should "be able to add a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
      val aesKey = new AES_ECB(p.key)
      val expected = aesKey.encrypt(AESUtils.pad(p.pt1 + p.pt2))
      dut.io.in.check.poke(false.B)
      dut.io.in.inst_data.poke("b000000".U)
      dut.io.in.input1_data.poke(p.ct1)
      dut.io.in.input2_data.poke(p.ct2)
      dut.io.in.inputcond_data.poke(0.U)
      dut.io.in.valid.poke(true.B)
      dut.clock.step(20)
      dut.io.output.result.expect(BigInt(expected).U(64.W))
      dut.io.output.valid.expect(true.B)
    }
  }
  // cmov a, b, c  a = b if b else c
  // add a, b, c
  // sub a, b, c
    it should "be able to xor a bunch the default param" in {
      val p = SEALUParams()
      test(new SEALU(p)).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
        val aesKey = new AES_ECB(p.key)
        val expected = aesKey.encrypt(AESUtils.pad(p.pt1 ^ p.pt2))
        dut.io.in.check.poke(false.B)
        println("Not poked yet")
        dut.io.in.inst_data.poke("b000111".U)
        println("Poked Inst")
        dut.io.in.input1_data.poke(p.ct1)
        println("Poked Input1")
        dut.io.in.input2_data.poke(p.ct2)
        println("Poked Input2")
        dut.io.in.inputcond_data.poke(0.U)
        println("Poked Condition")
        dut.io.in.valid.poke(true.B)
        println("Poked in.valid")
        dut.clock.step(20)
        println("Stepped clock by 20")
        dut.io.output.result.expect(BigInt(expected).U(64.W))
        dut.io.output.valid.expect(true.B)
      }
    }
  it should "be able to cmove a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
      val aesKey = new AES_ECB(p.key)
      val expected = aesKey.encrypt(AESUtils.pad(p.pt1))
      dut.io.in.check.poke(false.B)
      println("Not poked yet")
      dut.io.in.inst_data.poke("b010000".U)
      println("Poked Inst")
      dut.io.in.input1_data.poke(p.ct1)
      println("Poked Input1")
      dut.io.in.input2_data.poke(p.ct2)
      println("Poked Input2")
      dut.io.in.inputcond_data.poke(0.U)
      println("Poked Condition")
      dut.io.in.valid.poke(true.B)
      println("Poked in.valid")
      dut.clock.step(20)
      println("Stepped clock by 20")
      dut.io.output.result.expect(BigInt(expected).U(64.W))
      dut.io.output.valid.expect(true.B)
    }
  }
}