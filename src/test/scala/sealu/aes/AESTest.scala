package sealu.aes

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._

class AESTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "AES"

  it should "be decode correct" in {
    test(new Decode()) { dut =>
      //      val key=0.U(128.W)
      //      val data ="he673f208fbc35dcdba27c63cec8144f3".U
      //      val ref_out = "h5e7f53cec97c565a59926d963e763603".U
      val key = AESUtils.convert("593847fb7c86cf74a3e54bd76988a510")
      val keyBytes = AESUtils.pad(key)
      val aes = new AES_ECB(keyBytes)
      val data = "6584f7dbb46faa4ee051b044691e256d"
      val ref_out = aes.decrypt(AESUtils.hexStringToByteArray(data))

      println(f"${AESUtils.convert(AESUtils.byteArrayToHexString(keyBytes))}%x\n")
      println(f"${AESUtils.convert(data)}%x\n")
      print(f"${AESUtils.convert(AESUtils.byteArrayToHexString(ref_out))}%x\n")
      dut.io.input1.poke(AESUtils.convert(data))
      dut.io.input2.poke(AESUtils.convert(data))
      dut.io.cond.poke(AESUtils.convert(data))
      dut.io.key.poke(AESUtils.convert(AESUtils.byteArrayToHexString(keyBytes)))
      dut.io.valid.poke(true.B)
      for (_ <- 0 until 10) {
        dut.clock.step()
      }
      dut.io.output_input1.expect(AESUtils.convert(AESUtils.byteArrayToHexString(ref_out)))
      dut.io.output_input2.expect(AESUtils.convert(AESUtils.byteArrayToHexString(ref_out)))
      dut.io.output_cond.expect(AESUtils.convert(AESUtils.byteArrayToHexString(ref_out)))
      dut.io.ready.expect(true.B)
    }
  }

  it should "be encode correct" in {
    test(new Encode()) { dut =>
      val key = AESUtils.convert("0")
      val keyBytes = AESUtils.pad(key)
      val aes = new AES_ECB(keyBytes)
      val data = "5e7f53cec97c565a59926d963e763603"
      val ref_out = aes.encrypt(AESUtils.hexStringToByteArray(data))

      println(f"${AESUtils.convert(AESUtils.byteArrayToHexString(keyBytes))}%x\n")
      println(f"${AESUtils.convert(data)}%x\n")
      print(f"${AESUtils.convert(AESUtils.byteArrayToHexString(ref_out))}%x\n")

      dut.io.input.poke(AESUtils.convert(data))
      dut.io.key.poke(AESUtils.convert(AESUtils.byteArrayToHexString(keyBytes)))
      dut.io.valid.poke(true.B)
      for (i <- 0 until 10) {
        dut.clock.step()
      }
      dut.io.output.expect(AESUtils.convert(AESUtils.byteArrayToHexString(ref_out)))
      dut.io.ready.expect(true.B)
    }
  }

}
