import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import aes._

class SEALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SEALU"

  it should "be able to add a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // which key be indexed ?? I don't know how to implement.
      val key_input1 = p.key
      val aes_input1 = new aes.AES_ECB(aes.AESUtils.hexStringToByteArray(key_input1)) // aes key
      val dec_data1 = aes_input1.decrypt(aes.AESUtils.pad(p.init_plain.head))

      val key_input2 = p.key
      val aes_input2 = new aes.AES_ECB(aes.AESUtils.hexStringToByteArray(key_input2)) // aes key
      val dec_data2 = aes_input2.decrypt(aes.AESUtils.pad(p.init_plain(1)))

      print(f"${AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data1))}%x\n")
      print(f"${AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))}%x\n")
      for (_ <- 0 until 1) {
        dut.io.in.inst_data.poke("b000000".U)
        dut.io.in.input1_data.poke(AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data1))) // decrypted
        dut.io.in.input2_data.poke(AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))) // decrypted
        dut.io.in.inputcond_data.poke(0.U)
        dut.io.in.valid.poke(true.B)
        for (_ <- 0 until 20) {
          dut.clock.step()
        }
        dut.io.output.result.expect(AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))) // encrypted
        dut.io.output.valid.expect(false.B)
      } // check output buffer?

    }
  }
  // cmov a, b, c  a = b if b else c
  // add a, b, c
  // sub a, b, c
  it should "be able to cmov a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for (i <- 0 until 1) {
        val key_input1 = p.key
        val aes_input1 = new aes.AES_ECB(aes.AESUtils.hexStringToByteArray(key_input1)) // aes key
        val dec_data1 = aes_input1.decrypt(aes.AESUtils.pad(p.init_plain.head))

        val key_input2 = p.key
        val aes_input2 = new aes.AES_ECB(aes.AESUtils.hexStringToByteArray(key_input2)) // aes key
        val dec_data2 = aes_input2.decrypt(aes.AESUtils.pad(p.init_plain(1)))

        print(f"${AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data1))}%x\n")
        print(f"${AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))}%x\n")
        print(f"${AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))}%x\n")

        dut.io.in.inst_data.poke("b010000".U)
        dut.io.in.input1_data.poke(AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data1))) // decrypted
        dut.io.in.input2_data.poke(AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))) // decrypted
        dut.io.in.input2_data.poke(AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))) // decrypted
        dut.io.in.valid.poke(true.B)
        for (i <- 0 until 20) {
          dut.clock.step()
        }
        dut.io.output.result.expect(0.U) // encrypted
        dut.io.output.valid.expect(false.B) // update the validity?
      }
    }
  }
}