package sealu.aes

import chisel3.util._
import chisel3._
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

// refer to https://github.com/chipsalliance/rocket-chip/blob/38728ef3e57ee226caf444d95fd745935b639c4d/src/main/scala/zk/zkn.scala#L28
object AES {
  val enc: Seq[Int] = Seq(
    0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
    0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
    0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
    0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
    0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
    0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
    0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
    0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
    0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
    0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
    0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
    0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
    0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
    0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
    0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
    0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
  )

  val dec: Seq[Int] = Seq(
    0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
    0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
    0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
    0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
    0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
    0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
    0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
    0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
    0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
    0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
    0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
    0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
    0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
    0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
    0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
    0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
  )

  val rcon: Seq[Int] = Seq(0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36)
}

abstract class CryptoAlgorithm(protected var key: Array[Byte]) {
  protected var debug = true // Assuming there's a debug flag

  @throws[Exception]
  def encrypt(message: Array[Byte]): Array[Byte]

  @throws[Exception]
  def decrypt(message: Array[Byte]): Array[Byte]
}

class AES_ECB(key: Array[Byte]) extends CryptoAlgorithm(key) {
  @throws[Exception]
  override def encrypt(message: Array[Byte]): Array[Byte] = {
    val cipher = Cipher.getInstance("AES/ECB/NOPADDING")
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"))
    cipher.doFinal(message)
  }

  @throws[Exception]
  override def decrypt(messageBytes: Array[Byte]): Array[Byte] = {
    val cipher = Cipher.getInstance("AES/ECB/NOPADDING")
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"))
    cipher.doFinal(messageBytes)
  }
}

object AESUtils {
  def convert(s: String): BigInt = {
    var post = ""
    for (i <- 0 until 16) {
      post = s.slice(2 * i, 2 * i + 2).concat(post)
    }
    BigInt(post, 16)
  }

  def pad(key: BigInt): Array[Byte] = {
    val targetLengthBytes = 16
    // Convert BigInt to byte array of a specific length, filled with leading zeros if necessary
    val keyBytes = {
      val byteArray = key.toByteArray
      val padded = Array.fill[Byte](targetLengthBytes)(0) // Start with an array filled with zeros
      System.arraycopy(byteArray, 0, padded, targetLengthBytes - byteArray.length, byteArray.length) // Copy the BigInt bytes
      padded
    }
    keyBytes
  }

  def hexStringToByteArray(s: String): Array[Byte] = {
    val paddedHexString = s.padTo(32, '0').sliding(2, 2).toArray
    paddedHexString.map(hexPair => Integer.parseInt(hexPair, 16).toByte)
  }

  def byteArrayToHexString(bytes: Array[Byte]): String = {
    bytes.map(byte => f"$byte%02x").mkString
  }
}

//Calculates the entire schedule
class KeyScheduleBundle extends Bundle {
  val key_in = Input(Vec(16, UInt(8.W)))
  val key_schedule = Output(Vec(10, Vec(16, UInt(8.W))))
}

trait keyConnect {
  def connectRCON(prev: RCON, next: RCON) = {
    next.io.last_rcon := prev.io.next_rcon
  }

  def connectKeyStage(prev: KeyExpansion, next: KeyExpansion, rcon: RCON) = {
    next.io.key_in := prev.io.key_out
    next.io.rcon := rcon.io.next_rcon
  }
}

class KeySchedule extends Module with keyConnect {
  val io = IO(new KeyScheduleBundle())

  val r2 = Module(new RCON())
  val r3 = Module(new RCON())
  val r4 = Module(new RCON())
  val r5 = Module(new RCON())
  val r6 = Module(new RCON())
  val r7 = Module(new RCON())
  val r8 = Module(new RCON())
  val r9 = Module(new RCON())
  val r10 = Module(new RCON())

  r2.io.last_rcon := 1.U(8.W)
  connectRCON(r2, r3)
  connectRCON(r3, r4)
  connectRCON(r4, r5)
  connectRCON(r5, r6)
  connectRCON(r6, r7)
  connectRCON(r7, r8)
  connectRCON(r8, r9)
  connectRCON(r9, r10)

  val k1 = Module(new KeyExpansion())
  val k2 = Module(new KeyExpansion())
  val k3 = Module(new KeyExpansion())
  val k4 = Module(new KeyExpansion())
  val k5 = Module(new KeyExpansion())
  val k6 = Module(new KeyExpansion())
  val k7 = Module(new KeyExpansion())
  val k8 = Module(new KeyExpansion())
  val k9 = Module(new KeyExpansion())
  val k10 = Module(new KeyExpansion())

  k1.io.rcon := 1.U(8.W)
  k1.io.key_in := io.key_in

  connectKeyStage(k1, k2, r2)
  connectKeyStage(k2, k3, r3)
  connectKeyStage(k3, k4, r4)
  connectKeyStage(k4, k5, r5)
  connectKeyStage(k5, k6, r6)
  connectKeyStage(k6, k7, r7)
  connectKeyStage(k7, k8, r8)
  connectKeyStage(k8, k9, r9)
  connectKeyStage(k9, k10, r10)

  io.key_schedule(0) := k1.io.key_out
  io.key_schedule(1) := k2.io.key_out
  io.key_schedule(2) := k3.io.key_out
  io.key_schedule(3) := k4.io.key_out
  io.key_schedule(4) := k5.io.key_out
  io.key_schedule(5) := k6.io.key_out
  io.key_schedule(6) := k7.io.key_out
  io.key_schedule(7) := k8.io.key_out
  io.key_schedule(8) := k9.io.key_out
  io.key_schedule(9) := k10.io.key_out
}

class DataBundle extends Bundle {
  val data_in = Input(Vec(16, UInt(8.W)))
  val data_out = Output(Vec(16, UInt(8.W)))
}

class AESCipherExtraStage(enc:Boolean) extends Module {
  val io = IO(new DataBundleWithKeyIn())

  val shift_rows = Module(new ShiftRows(enc))
  val add_round_key = Module(new AddRoundKey())

  if (enc) {
    val sub_byte = Module(new SubByte(AES.enc))
    add_round_key.io.key_in := io.key_in

    //Chain modules together
    sub_byte.io.data_in := io.data_in
    shift_rows.io.data_in := sub_byte.io.data_out
    add_round_key.io.data_in := shift_rows.io.data_out
    io.data_out := add_round_key.io.data_out
  }else{
    val inv_sub_byte = Module(new SubByte(AES.dec))
    add_round_key.io.key_in := io.key_in

    //Chain modules together
    add_round_key.io.data_in := io.data_in
    shift_rows.io.data_in := add_round_key.io.data_out
    inv_sub_byte.io.data_in := shift_rows.io.data_out
    io.data_out := inv_sub_byte.io.data_out
  }
}

class DataBundleWithKeyIn extends DataBundle {
  val key_in = Input(Vec(16, UInt(8.W)))
}

class AESCore extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(128.W))
    val valid = Input(Bool())
    val key = Input(Vec(16, UInt(8.W)))
    val key_schedule = Input(Vec(10, Vec(16, UInt(8.W))))
    val key_valid = Input(Bool())
    val is_enc = Input(Bool())
    val output = Output(UInt(128.W))
    val ready = Output(Bool())
  })
  val key_schedule = io.key_schedule
  val data_in_top = io.input.asTypeOf(Vec(16, UInt(8.W)))
  val key_in_top = io.key
  //State Machine ---------------------------------------
  val numStages = 10 //for AES128

  val start = io.valid && io.key_valid
  val counter = RegInit(0.U(4.W))
  val running = counter > 1.U

  counter := Mux(running, counter - 1.U,
    Mux(start, numStages.U, counter))

  val mux_select_stage = counter === numStages.U
//  when(io.key_valid) {
//    printf("key_schedule: 0 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(0)(0), key_schedule(0)(1), key_schedule(0)(2), key_schedule(0)(3), key_schedule(0)(4), key_schedule(0)(5), key_schedule(0)(6), key_schedule(0)(7), key_schedule(0)(8), key_schedule(0)(9), key_schedule(0)(10), key_schedule(0)(11), key_schedule(0)(12), key_schedule(0)(13), key_schedule(0)(14), key_schedule(0)(15))
//    printf("key_schedule: 1 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(1)(0), key_schedule(1)(1), key_schedule(1)(2), key_schedule(1)(3), key_schedule(1)(4), key_schedule(1)(5), key_schedule(1)(6), key_schedule(1)(7), key_schedule(1)(8), key_schedule(1)(9), key_schedule(1)(10), key_schedule(1)(11), key_schedule(1)(12), key_schedule(1)(13), key_schedule(1)(14), key_schedule(1)(15))
//    printf("key_schedule: 2 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(2)(0), key_schedule(2)(1), key_schedule(2)(2), key_schedule(2)(3), key_schedule(2)(4), key_schedule(2)(5), key_schedule(2)(6), key_schedule(2)(7), key_schedule(2)(8), key_schedule(2)(9), key_schedule(2)(10), key_schedule(2)(11), key_schedule(2)(12), key_schedule(2)(13), key_schedule(2)(14), key_schedule(2)(15))
//    printf("key_schedule: 3 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(3)(0), key_schedule(3)(1), key_schedule(3)(2), key_schedule(3)(3), key_schedule(3)(4), key_schedule(3)(5), key_schedule(3)(6), key_schedule(3)(7), key_schedule(3)(8), key_schedule(3)(9), key_schedule(3)(10), key_schedule(3)(11), key_schedule(3)(12), key_schedule(3)(13), key_schedule(3)(14), key_schedule(3)(15))
//    printf("key_schedule: 4 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(4)(0), key_schedule(4)(1), key_schedule(4)(2), key_schedule(4)(3), key_schedule(4)(4), key_schedule(4)(5), key_schedule(4)(6), key_schedule(4)(7), key_schedule(4)(8), key_schedule(4)(9), key_schedule(4)(10), key_schedule(4)(11), key_schedule(4)(12), key_schedule(4)(13), key_schedule(4)(14), key_schedule(4)(15))
//    printf("key_schedule: 5 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(5)(0), key_schedule(5)(1), key_schedule(5)(2), key_schedule(5)(3), key_schedule(5)(4), key_schedule(5)(5), key_schedule(5)(6), key_schedule(5)(7), key_schedule(5)(8), key_schedule(5)(9), key_schedule(5)(10), key_schedule(5)(11), key_schedule(5)(12), key_schedule(5)(13), key_schedule(5)(14), key_schedule(5)(15))
//    printf("key_schedule: 6 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(6)(0), key_schedule(6)(1), key_schedule(6)(2), key_schedule(6)(3), key_schedule(6)(4), key_schedule(6)(5), key_schedule(6)(6), key_schedule(6)(7), key_schedule(6)(8), key_schedule(6)(9), key_schedule(6)(10), key_schedule(6)(11), key_schedule(6)(12), key_schedule(6)(13), key_schedule(6)(14), key_schedule(6)(15))
//    printf("key_schedule: 7 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(7)(0), key_schedule(7)(1), key_schedule(7)(2), key_schedule(7)(3), key_schedule(7)(4), key_schedule(7)(5), key_schedule(7)(6), key_schedule(7)(7), key_schedule(7)(8), key_schedule(7)(9), key_schedule(7)(10), key_schedule(7)(11), key_schedule(7)(12), key_schedule(7)(13), key_schedule(7)(14), key_schedule(7)(15))
//    printf("key_schedule: 8 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(8)(0), key_schedule(8)(1), key_schedule(8)(2), key_schedule(8)(3), key_schedule(8)(4), key_schedule(8)(5), key_schedule(8)(6), key_schedule(8)(7), key_schedule(8)(8), key_schedule(8)(9), key_schedule(8)(10), key_schedule(8)(11), key_schedule(8)(12), key_schedule(8)(13), key_schedule(8)(14), key_schedule(8)(15))
//    printf("key_schedule: 9 VecInit(0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W), 0x%x.U(8.W)),\n", key_schedule(9)(0), key_schedule(9)(1), key_schedule(9)(2), key_schedule(9)(3), key_schedule(9)(4), key_schedule(9)(5), key_schedule(9)(6), key_schedule(9)(7), key_schedule(9)(8), key_schedule(9)(9), key_schedule(9)(10), key_schedule(9)(11), key_schedule(9)(12), key_schedule(9)(13), key_schedule(9)(14), key_schedule(9)(15))
//  }
  //Computations -----------------------------------------
  //Initial round
  when(io.is_enc) {
    val stage0_addRoundKey = Module(new AddRoundKey())
    stage0_addRoundKey.io.key_in := key_in_top
    stage0_addRoundKey.io.data_in := data_in_top
    val stage0_data_out = stage0_addRoundKey.io.data_out

    // Round 1
    val stage1_cipher = Module(new AESCipherStage(true))
    stage1_cipher.io.data_in := stage0_data_out
    stage1_cipher.io.key_in := key_schedule(0)
    val stage1_data_out = stage1_cipher.io.data_out
    //  printf("stage1_key: %x\n",stage1_data_out.asUInt)

    //stages 2-9
    val data_reg = Reg(Vec(16, UInt(8.W)))

    val AESStage = Module(new AESCipherStage(true))
    AESStage.io.data_in := data_reg
    AESStage.io.key_in := key_schedule(10.U - counter)

    val data_next = AESStage.io.data_out
    data_reg := Mux(mux_select_stage, stage1_data_out, data_next)

    // output round
    val stage10 = Module(new AESCipherExtraStage(true))
    stage10.io.data_in := data_reg
    stage10.io.key_in := key_schedule(9)
    //  printf("data_reg:  %x",data_reg.asUInt)
    io.output := stage10.io.data_out.asTypeOf(UInt(128.W))
    //  printf("data_out: %x",data_out_top)
    io.ready := !running
  }.otherwise{
    val stage0 = Module(new AESCipherExtraStage(false))
    stage0.io.key_in := key_schedule(9)
    stage0.io.data_in := data_in_top
    val stage0_data_out = stage0.io.data_out

    //stages 1-8
    val data_reg    = Reg(Vec(16, UInt(8.W)))

    val InvAESStage = Module(new AESCipherStage(false))
    InvAESStage.io.data_in     := data_reg
    InvAESStage.io.key_in      := key_schedule(counter - 1.U)

    val data_next   = InvAESStage.io.data_out
    data_reg    := Mux(mux_select_stage, stage0_data_out, data_next)

    // output round
    val stage9 = Module( new AESCipherStage(false))
    stage9.io.data_in := data_reg
    stage9.io.key_in := key_schedule(0)

    val stage10 = Module(new AddRoundKey())
    stage10.io.key_in := key_in_top
    stage10.io.data_in := stage9.io.data_out
    val stage10_data_out = stage10.io.data_out
//      printf("data_reg:  %x",data_reg.asUInt)

    io.output := stage10.io.data_out.asTypeOf(UInt(128.W))
//      printf("data_out: %x",io.output)

    io.ready := !running
  }
}
class AESCipherStage(enc: Boolean) extends Module {
  val io = IO(new
      Bundle {
    val data_in = Input(Vec(16, UInt(8.W)))
    val key_in = Input(Vec(16, UInt(8.W)))
    val data_out = Output(Vec(16, UInt(8.W)))
  })

  val add_round_key = Module(new AddRoundKey())
  val mix_columns = Module(new MixColumn128(enc))
  val shift_rows = Module(new ShiftRows(enc))

  add_round_key.io.key_in := io.key_in

  //Chain modules together
  if (!enc) {
    val inv_sub_byte = Module(new SubByte(AES.dec))

    add_round_key.io.data_in := io.data_in
    mix_columns.io.data_in := add_round_key.io.data_out
    shift_rows.io.data_in := mix_columns.io.data_out
    inv_sub_byte.io.data_in := shift_rows.io.data_out
    io.data_out := inv_sub_byte.io.data_out
  } else {
    val sub_byte = Module(new SubByte(AES.enc))

    sub_byte.io.data_in := io.data_in
    shift_rows.io.data_in := sub_byte.io.data_out
    mix_columns.io.data_in := shift_rows.io.data_out
    add_round_key.io.data_in := mix_columns.io.data_out
    io.data_out := add_round_key.io.data_out
  }
}

class AddRoundKey extends Module {
  val io = IO(new DataBundleWithKeyIn())

  for (i <- 0 until 16) {
    io.data_out(i) := io.data_in(i) ^ io.key_in(i) //RoundKey
  }
}

class SubByte(table: Seq[Int]) extends Module {
  val io = IO(new DataBundle())

  def subByte(in: UInt): UInt = {
    val sbox = Module(new SBox(table))
    sbox.io.in := in
    sbox.io.out
  }

  for (i <- 0 until 16) {
    io.data_out(i) := subByte(io.data_in(i))
  }
}


class SBox(table: Seq[Int]) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })

  io.out := VecInit(table.map(_.U(8.W)))(io.in)
}

class RCON extends Module {
  val io = IO(new Bundle {
    val last_rcon = Input(UInt(8.W))
    val next_rcon = Output(UInt(8.W))
  })

  when((io.last_rcon & 0x80.U(8.W)).orR) {
    io.next_rcon := (io.last_rcon << 1) ^ 0x1B.U(8.W)
  }.otherwise {
    io.next_rcon := io.last_rcon << 1
  }

}

class KeyExpansion extends Module {
  val io = IO(new Bundle {
    val key_in = Input(Vec(16, UInt(8.W)))
    val rcon = Input(UInt(8.W))
    val key_out = Output(Vec(16, UInt(8.W)))
  })

  def subByte(in: UInt): UInt = {
    val sbox = Module(new SBox(AES.enc))
    sbox.io.in := in
    sbox.io.out
  }
  //Wi
  io.key_out(0) := io.key_in(0) ^ subByte(io.key_in(13)) ^ io.rcon
  io.key_out(1) := io.key_in(1) ^ subByte(io.key_in(14))
  io.key_out(2) := io.key_in(2) ^ subByte(io.key_in(15))
  io.key_out(3) := io.key_in(3) ^ subByte(io.key_in(12))

  //Wi+1...3
  for (i <- 4 until 16) {
    io.key_out(i) := io.key_in(i) ^ io.key_out(i - 4)
  }
}


//shuffle the 16-bit array
class ShiftRows(enc: Boolean) extends Module {
  val io = IO(new DataBundle())
  if (enc) {
    io.data_out(0) := io.data_in(0)
    io.data_out(1) := io.data_in(5)
    io.data_out(2) := io.data_in(10)
    io.data_out(3) := io.data_in(15)

    io.data_out(4) := io.data_in(4)
    io.data_out(5) := io.data_in(9)
    io.data_out(6) := io.data_in(14)
    io.data_out(7) := io.data_in(3)

    io.data_out(8) := io.data_in(8)
    io.data_out(9) := io.data_in(13)
    io.data_out(10) := io.data_in(2)
    io.data_out(11) := io.data_in(7)

    io.data_out(12) := io.data_in(12)
    io.data_out(13) := io.data_in(1)
    io.data_out(14) := io.data_in(6)
    io.data_out(15) := io.data_in(11)
  } else {
    io.data_out(0) := io.data_in(0)
    io.data_out(5) := io.data_in(1)
    io.data_out(10) := io.data_in(2)
    io.data_out(15) := io.data_in(3)

    io.data_out(4) := io.data_in(4)
    io.data_out(9) := io.data_in(5)
    io.data_out(14) := io.data_in(6)
    io.data_out(3) := io.data_in(7)

    io.data_out(8) := io.data_in(8)
    io.data_out(13) := io.data_in(9)
    io.data_out(2) := io.data_in(10)
    io.data_out(7) := io.data_in(11)

    io.data_out(12) := io.data_in(12)
    io.data_out(1) := io.data_in(13)
    io.data_out(6) := io.data_in(14)
    io.data_out(11) := io.data_in(15)
  }
}

class MMDataBundle extends Bundle {
  val data_in = Input(Vec(4, UInt(8.W)))
  val data_out = Output(Vec(4, UInt(8.W)))
}

trait MixColumnsFunctions {
  def gmul2(a: UInt): UInt = {
    Mux((a & 0x80.U(8.W)).orR, (a << 1) ^ 0x1b.U(8.W), a << 1)
  }

  def gmul3(a: UInt): UInt = {
    Mux((a & 0x80.U(8.W)).orR, (a << 1) ^ a ^ 0x1b.U(8.W), (a << 1) ^ a)
  }

  def gmul4(a: UInt): UInt = {
    gmul2(gmul2(a))
  }

  def gmul8(a: UInt): UInt = {
    gmul2(gmul2(gmul2(a)))
  }

  def gmul9(a: UInt): UInt = {
    a ^ gmul8(a)
  }

  def gmul11(a: UInt): UInt = {
    a ^ gmul2(a) ^ gmul8(a)
  }

  def gmul13(a: UInt): UInt = {
    a ^ gmul4(a) ^ gmul8(a)
  }

  def gmul14(a: UInt): UInt = {
    gmul2(a) ^ gmul4(a) ^ gmul8(a)
  }
}

class MixColumnsMM(enc: Boolean) extends Module with MixColumnsFunctions {
  val io = IO(new MMDataBundle())
  if (enc) {
    io.data_out(0) := gmul2(io.data_in(0)) ^ gmul3(io.data_in(1)) ^ io.data_in(2) ^ io.data_in(3)
    io.data_out(1) := io.data_in(0) ^ gmul2(io.data_in(1)) ^ gmul3(io.data_in(2)) ^ io.data_in(3)
    io.data_out(2) := io.data_in(0) ^ io.data_in(1) ^ gmul2(io.data_in(2)) ^ gmul3(io.data_in(3))
    io.data_out(3) := gmul3(io.data_in(0)) ^ io.data_in(1) ^ io.data_in(2) ^ gmul2(io.data_in(3))
  } else {
    io.data_out(0) := gmul14(io.data_in(0)) ^ gmul11(io.data_in(1)) ^ gmul13(io.data_in(2)) ^ gmul9(io.data_in(3))
    io.data_out(1) := gmul9(io.data_in(0)) ^ gmul14(io.data_in(1)) ^ gmul11(io.data_in(2)) ^ gmul13(io.data_in(3))
    io.data_out(2) := gmul13(io.data_in(0)) ^ gmul9(io.data_in(1)) ^ gmul14(io.data_in(2)) ^ gmul11(io.data_in(3))
    io.data_out(3) := gmul11(io.data_in(0)) ^ gmul13(io.data_in(1)) ^ gmul9(io.data_in(2)) ^ gmul14(io.data_in(3))
  }
}

//Column Mixing provides the primary obfuscation in AES
class MixColumn128(enc: Boolean) extends Module {
  val io = IO(new DataBundle())

  val d_in = io.data_in.asTypeOf(Vec(4, Vec(4, UInt(8.W))))
  val d_out = Wire(Vec(4, Vec(4, UInt(8.W))))
  io.data_out := d_out.asTypeOf(Vec(16, UInt(8.W)))

  val MM0 = Module(new MixColumnsMM(enc))
  val MM1 = Module(new MixColumnsMM(enc))
  val MM2 = Module(new MixColumnsMM(enc))
  val MM3 = Module(new MixColumnsMM(enc))

  MM0.io.data_in := d_in(0)
  d_out(0) := MM0.io.data_out

  MM1.io.data_in := d_in(1)
  d_out(1) := MM1.io.data_out

  MM2.io.data_in := d_in(2)
  d_out(2) := MM2.io.data_out

  MM3.io.data_in := d_in(3)
  d_out(3) := MM3.io.data_out
}
