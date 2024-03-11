package aes

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._

object TestValues {
  val key = BigInt("010102030405060708090a0b0c0d0e0f", 16).toByteArray
  val ptexts = Seq(
    BigInt("0f0e0d0c0b0a09080706050403020100", 16).toByteArray.takeRight(16),
    BigInt("07060504030201000f0e0d0c0b0a0908", 16).toByteArray.takeRight(16),
    BigInt("03020100070605040b0a09080f0e0d0c", 16).toByteArray.takeRight(16),
  )
  val ctexts = Seq(
    BigInt("77fa25b0879ce4c394aafc20ac4b39cb", 16).toByteArray.takeRight(16),
    BigInt("d0a545aed00983332224a415ab54ef7b", 16).toByteArray.takeRight(16),
    BigInt("ea3390b8c4afd58f95aa2cf5fce1cf7f", 16).toByteArray.takeRight(16),
  )
}

class AESModelTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AESModel"
  val model = new AES_ECB(TestValues.key)

  it should "encrypt correctly" in {
    for (i <- TestValues.ptexts.indices) {
      //      println(TestValues.ctexts(i).map("%02X" format _).mkString)
      //      val c = model.encrypt(TestValues.ptexts(i))
      //      println(c.map("%02X" format _).mkString)
      assert(model.encrypt(TestValues.ptexts(i)) sameElements TestValues.ctexts(i))
    }
  }

  it should "decrypt correctly" in {
    for (i <- TestValues.ctexts.indices) {
      assert(model.decrypt(TestValues.ctexts(i)) sameElements TestValues.ptexts(i))
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

  def doTest(isEnc: Boolean, inputs: Seq[Array[Byte]], outputs: Seq[Array[Byte]]): TestResult = {
    test(new AESEncDec(isEnc)).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
      for ((inp, out) <- inputs.zip(outputs)) {
        dut.io.input.poke(BigInt(Array[Byte](0) ++ inp).U)
        dut.io.key.poke(BigInt(Array[Byte](0) ++ TestValues.key).U)
        dut.io.valid.poke(true.B)

        dut.clock.step(10)

        dut.io.ready.expect(true.B)
        dut.io.output.expect(BigInt(Array[Byte](0) ++ out).U)
      }
    }
  }

}
