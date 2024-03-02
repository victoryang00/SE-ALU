package sealu

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import chiseltest._
import aes._

class SEALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SEALU"

  it should "be able to add a bunch the default param" in {
    val p = SEALUParams()
    test(new SEALU(p)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//      val plaintext: Seq[Byte] = p.init_plain.map(_.toByte)
      val cypher: Seq[Byte] = p.init_cipher.map(_.toByte)
      // which key be indexed ?? I don't know how to implement.
      val key_input1 = p.init_cipher.head // aes key
      val aes_input1 = new aes.AES_ECB(aes.AESUtils.pad(key_input1)) // aes key
      val dec_data1 = aes_input1.decrypt(p.init_plain.head.toByteArray)

      val key_input2 = p.init_cipher(1) // aes key
      val aes_input2 = new aes.AES_ECB(aes.AESUtils.pad(key_input2)) // aes key
      val dec_data2 = aes_input2.decrypt(p.init_plain(1).toByteArray)

      val enc_data = p.init_plain(2).toByteArray
      print(f"${AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data1))}%x\n")
      print(f"${AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))}%x\n")
      print(f"${AESUtils.convert(aes.AESUtils.byteArrayToHexString(enc_data))}%x\n")
      for (i <- 0 until 1) {
        dut.io.in.inst_data.poke("b000000".U)
        dut.io.in.input1_data.poke(AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data1))) // decrypted
        dut.io.in.input2_data.poke(AESUtils.convert(aes.AESUtils.byteArrayToHexString(dec_data2))) // decrypted
        dut.io.in.inputcond_data.poke(0.U)
        dut.io.in.cache_valid.poke(VecInit(true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B,true.B))
        dut.io.in.valid.poke(true.B)
        for (i <- 0 until 10) {
          dut.clock.step()
        }
        dut.io.output.result.expect(AESUtils.convert(aes.AESUtils.byteArrayToHexString(enc_data))) // encrypted
        dut.io.output.valid.expect(false.B)
        dut.io.output.counter.expect(100.U)
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
        dut.io.in.inst_data.poke("b010000".U)
        dut.io.in.input1_data.poke(1.U) // decrypted
        dut.io.in.input2_data.poke(1.U) // decrypted
        dut.io.in.inputcond_data.poke(0.U)
        dut.io.in.valid.poke(true.B)
        for (i <- 0 until 10) {
          dut.clock.step()
        }
        dut.io.output.result.expect(0.U) // encrypted
        dut.io.output.valid.expect(false.B)
        dut.io.output.counter.expect(100.U)
      }
    }
  }
}