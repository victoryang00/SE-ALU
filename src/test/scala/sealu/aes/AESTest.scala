package aes

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._

object TestValues {
  val key = BigInt("010102030405060708090a0b0c0d0e0f", 16).toByteArray
  val ptexts = Seq(
    "0f0e0d0c0b0a09080706050403020100",
    "07060504030201000f0e0d0c0b0a0908",
    "03020100070605040b0a09080f0e0d0c",
  )
  val ctexts = Seq(
    "77fa25b0879ce4c394aafc20ac4b39cb",

    "d0a545aed00983332224a415ab54ef7b",
    "ea3390b8c4afd58f95aa2cf5fce1cf7f",
  )
}

class AESModelTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AESModel"
  val model = new AES_ECB(TestValues.key)

  it should "encrypt correctly" in {
    for (i <- TestValues.ptexts.indices) {
      val c = model.encrypt(BigInt(TestValues.ptexts(i), 16).toByteArray.takeRight(16))
      val s = c.map("%02x" format _).mkString
      assert(s equals TestValues.ctexts(i))
    }
  }

  it should "decrypt correctly" in {
    for (i <- TestValues.ctexts.indices) {
      val p = model.decrypt(BigInt(TestValues.ctexts(i), 16).toByteArray.takeRight(16))
      val s = p.map("%02x" format _).mkString
      assert(s equals TestValues.ptexts(i))
    }
  }
}

class AESTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AES"

  it should "encrypt correctly" in {
    doTest(isEnc = true, TestValues.ptexts, TestValues.ctexts)
  }

  it should "decrypt correctly" in {
    doTest(isEnc = false, TestValues.ctexts, TestValues.ptexts)
  }

  def doTest(isEnc: Boolean, inputs: Seq[String], outputs: Seq[String]): TestResult = {
    test(new AESEncDec(isEnc)).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
      for ((inp, out) <- inputs.zip(outputs)) {
        dut.io.input.poke(("x" + inp).U)
        dut.io.key.poke(BigInt(TestValues.key).U)
        dut.io.valid.poke(true.B)

        dut.clock.step(10)

        dut.io.ready.expect(true.B)
        dut.io.output.expect(("x" + out).U)
      }
    }
  }

}
