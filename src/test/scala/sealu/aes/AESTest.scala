package sealu.aes

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import sealu.aes

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

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
//https://kavaliro.com/wp-content/uploads/2014/03/AES.pdf
class AESTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AES"
  val data = (BigInt(0x0203030101010101L) << 64) + BigInt(0x0101010101010101L)
  val ref_out = (BigInt(0x3c5cd4828515ad38L) << 64) + (BigInt(0xe12659ceL) << 32) + BigInt(0xd16b2314L)
  it should "be decode correct" in {
    test(new Decode()) { dut =>

      dut.io.input1.poke(data)
      dut.io.input2.poke(data)
      dut.io.cond.poke(data)
      dut.io.valid.poke(true.B)
      for (i <- 0 until 10) {
        dut.clock.step()
      }
      dut.io.output_input1.expect(ref_out)
      dut.io.output_input2.expect(ref_out)
      dut.io.output_cond.expect(ref_out)
      dut.io.ready.expect(true.B)
    }
  }

  it should "be encode correct" in {
    test(new Encode()) { dut =>
      dut.io.input.poke(data)
      dut.io.valid.poke(true.B)
      for (i <- 0 until 10) {
        dut.clock.step()
      }
      dut.io.output.expect(ref_out)
      dut.io.ready.expect(true.B)
    }
  }

}
