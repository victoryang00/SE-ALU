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
      val plaintext:Seq[Byte] = p.init_plain.map(_.toByte)
      val cypher:Seq[Byte] = p.init_cipher.map(_.toByte)
      // which key be indexed
      val key_input1 = p.init_cipher(0) // aes key
//      val aes_input1 = aes.AES(key_input1) // aes key
//      val encrypted = aes_input1.encrypt(plaintext)
//      val enc_data = VecInit(encrypted.map(_.U)).asUInt
//      val decrypted = SimpleAES.decrypt(encrypted, cypher)
//      val dec_data = VecInit(decrypted.map(_.U)).asUInt
      val enc_data = 0.U
      val dec_data = 0.U
      for (i <- 0 until 1) {
        dut.io.in.inst_data.poke("b000000".U)
        dut.io.in.input1_data.poke(dec_data) // decrypted
        dut.io.in.input2_data.poke(dec_data) // decrypted
        dut.io.in.inputcond_data.poke(0.U)
        dut.io.in.valid.poke(true.B)
        for (i <- 0 until 10) {
          dut.clock.step()
        }
        dut.io.output.result.expect(enc_data) // encrypted
        dut.io.output.valid.expect(false.B)
        dut.io.output.counter.expect(100.U)
      }// check output buffer?

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