import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import aes._

class SEALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SEALU"

  it should "be able to add a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)) { dut =>
      val aesKey = new AES_ECB(p.key)
      val expected = aesKey.encrypt((p.pt1 + p.pt2).toByteArray)
      println("Not poked yet")
      dut.io.in.inst_data.poke("b000000".U)
      println("Poked Inst")
      dut.io.in.input1_data.poke(p.ct1)
      println("Poked Input1")
      dut.io.in.input2_data.poke(p.ct2)
      println("Poked Input2")
      dut.io.in.inputcond_data.poke(0.U)
      println("Poked Condition")
      dut.io.in.valid.poke(true.B)
      println("Poked in.valid")
      dut.clock.step(10)
      println("Stepped clock by 10")
      dut.io.output.result.expect(BigInt(expected).U(64.W))
      dut.io.output.valid.expect(true.B)
    }
  }
  // cmov a, b, c  a = b if b else c
  // add a, b, c
  // sub a, b, c
//  it should "be able to cmov a bunch the default param" in {
//    val p = SEALUParams()
//    test(new SEALU(p)) { dut =>
//      val key_input1 = p.key
//      val aes_input1 = new aes.AES_ECB(aes.AESUtils.hexStringToByteArray(key_input1)) // aes key
//      val dec_data1 = aes_input1.decrypt(aes.AESUtils.pad(p.init_plain.head))
//
//      val key_input2 = p.key
//      val aes_input2 = new aes.AES_ECB(aes.AESUtils.hexStringToByteArray(key_input2)) // aes key
//      val dec_data2 = aes_input2.decrypt(aes.AESUtils.pad(p.init_plain(1)))
//
//      println(f"${BigInt(dec_data1)}%x")
//      println(f"${BigInt(dec_data2)}%x")
//
//      dut.io.in.inst_data.poke("b010000".U)
//      dut.io.in.input1_data.poke(BigInt(dec_data1)) // decrypted
//      dut.io.in.input2_data.poke(BigInt(dec_data2)) // decrypted
//      dut.io.in.valid.poke(true.B)
//      dut.clock.step(20)
//      dut.io.output.result.expect(0.U) // encrypted
//      dut.io.output.valid.expect(false.B) // update the validity?
//    }
//  }
}