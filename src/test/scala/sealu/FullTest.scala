package sealu

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import aes._
object SimpleAES {
  // Simplified AES encryption function
  def encrypt(key: Seq[Byte], value: Seq[Byte]): Seq[Byte] = {
    val cipher: Cipher = Cipher.getInstance("AES/ECB/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, keyToSpec(key))
    cipher.doFinal(value.toArray[Byte]).toSeq
  }

  def decrypt(key: Seq[Byte], encryptedValue: Seq[Byte]): Seq[Byte] = {
    val cipher: Cipher = Cipher.getInstance("AES/ECB/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, keyToSpec(key))
    cipher.doFinal(encryptedValue.toArray[Byte]).toSeq
  }

  private def keyToSpec(key: Seq[Byte]): SecretKeySpec = {
    val keyBytes: Array[Byte] = key.toArray[Byte]
    new SecretKeySpec(keyBytes, "AES")
  }
}
class SEALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SEALU"

  it should "be able to add a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val encrypted = SimpleAES.encrypt(plaintext, key)
      val decrypted = SimpleAES.decrypt(encrypted, key)
      for (i <- 0 until 100) {
        dut.io.in.inst_data.poke("b000000".U)
        dut.io.in.input1_data.poke(decrypted) // decrypted
        dut.io.in.input2_data.poke(decrypted) // decrypted
        dut.io.in.inputcond_data.poke(0.U)
        dut.io.in.valid.poke(true.B)
        dut.clock.step()
        dut.io.output.result.expect(encrypted) // encrypted
        dut.io.output.valid.expect(false.B)
        dut.io.output.counter.expect(100.U)
      }
//      for (i <- 0 until 100) {
//        dut.io.in.inst_data.poke(0.U)
//        dut.io.in.input1_data.poke(1.U) // decrypted
//        dut.io.in.input2_data.poke(1.U) // decrypted
//        dut.io.in.inputcond_data.poke(0.U)
//        dut.io.in.valid.poke(true.B)
//        dut.clock.step()
//        dut.io.output.result.expect(0.U) // encrypted
//        dut.io.output.valid.expect(false.B)
//        dut.io.output.counter.expect(100.U)
//      }

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