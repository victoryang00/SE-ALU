package sealu.aes

import chisel3.util._
import chisel3._

object AES {
  val key: Seq[Int] =  Seq(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0xd6, 0xaa, 0x74, 0xfd, 0xd2, 0xaf, 0x72, 0xfa, 0xda, 0xa6, 0x78, 0xf1, 0xd6, 0xab, 0x76, 0xfe, 0xb6, 0x92, 0xcf, 0x0b, 0x64, 0x3d, 0xbd, 0xf1, 0xbe, 0x9b, 0xc5, 0x00, 0x68, 0x30, 0xb3, 0xfe, 0xb6, 0xff, 0x74, 0x4e, 0xd2, 0xc2, 0xc9, 0xbf, 0x6c, 0x59, 0x0c, 0xbf, 0x04, 0x69, 0xbf, 0x41, 0x47, 0xf7, 0xf7, 0xbc, 0x95, 0x35, 0x3e, 0x03, 0xf9, 0x6c, 0x32, 0xbc, 0xfd, 0x05, 0x8d, 0xfd, 0x3c, 0xaa, 0xa3, 0xe8, 0xa9, 0x9f, 0x9d, 0xeb, 0x50, 0xf3, 0xaf, 0x57, 0xad, 0xf6, 0x22, 0xaa, 0x5e, 0x39, 0x0f, 0x7d, 0xf7, 0xa6, 0x92, 0x96, 0xa7, 0x55, 0x3d, 0xc1, 0x0a, 0xa3, 0x1f, 0x6b, 0x14, 0xf9, 0x70, 0x1a, 0xe3, 0x5f, 0xe2, 0x8c, 0x44, 0x0a, 0xdf, 0x4d, 0x4e, 0xa9, 0xc0, 0x26, 0x47, 0x43, 0x87, 0x35, 0xa4, 0x1c, 0x65, 0xb9, 0xe0, 0x16, 0xba, 0xf4, 0xae, 0xbf, 0x7a, 0xd2, 0x54, 0x99, 0x32, 0xd1, 0xf0, 0x85, 0x57, 0x68, 0x10, 0x93, 0xed, 0x9c, 0xbe, 0x2c, 0x97, 0x4e, 0x13, 0x11, 0x1d, 0x7f, 0xe3, 0x94, 0x4a, 0x17, 0xf3, 0x07, 0xa7, 0x8b, 0x4d, 0x2b, 0x30, 0xc5)
}
class AES extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(64.W))
    val valid = Input(Bool())
    val key = Input(Vec(176, UInt(8.W)))
    val is_enc = Input(Bool())
    val output = Output(UInt(64.W))
  })

  val sbox = Module(new SBox(AES.key))
  val aes_sbox = Module(new AESSBox)
  val shift_rows = Module(new ShiftRows(true))
  val mix_column = Module(new MixColumn64(true))


  aes_sbox.io.in := sbox.io.out
  aes_sbox.io.is_enc := io.is_enc
  shift_rows.io.in1 := aes_sbox.io.out
  shift_rows.io.in2 := aes_sbox.io.out
  mix_column.io.in := shift_rows.io.out
  // can be used to encrypt or decrypt

}

class SBox(table: Seq[Int]) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })

  io.out := VecInit(table.map(_.U(8.W)))(io.in)
}

class AESSBox extends Module {
  val io = IO(new Bundle {
    val is_enc = Input(Bool())
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })

  val enc = Module(new SBox(AES.key))
  val dec = Module(new SBox(AES.key))
  enc.io.in := io.in
  dec.io.in := io.in
  io.out := Mux(io.is_enc,
    enc.io.out, dec.io.out)
}

class GFMul(y: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })

  // x*f(x) = 2*in in GF
  def xt(in: UInt): UInt = (in << 1)(7,0) ^ Mux(in(7), 0x1b.U(8.W), 0x00.U(8.W))
  // 4*in in GF
  def xt2(in: UInt): UInt = xt(xt(in))
  // 8*in in GF
  def xt3(in: UInt): UInt = xt(xt2(in))

  require(y != 0)
  io.out := VecInit(
    (if ((y & 0x1) != 0) Seq(   (io.in)) else Nil) ++
      (if ((y & 0x2) != 0) Seq( xt(io.in)) else Nil) ++
      (if ((y & 0x4) != 0) Seq(xt2(io.in)) else Nil) ++
      (if ((y & 0x8) != 0) Seq(xt3(io.in)) else Nil)
  ).reduce(_ ^ _)
}

class ShiftRows(enc: Boolean) extends Module {
  val io = IO(new Bundle {
    val in1 = Input(UInt(64.W))
    val in2 = Input(UInt(64.W))
    val out = Output(UInt(64.W))
  })

  val stride = if (enc) 5 else 13
  val indexes = Seq.tabulate(4)(x => (x * stride) % 16) ++
    Seq.tabulate(4)(x => (x * stride + 4) % 16)

  def asBytes(in: UInt): Vec[UInt] = VecInit(in.asBools.grouped(8).map(VecInit(_).asUInt).toSeq)
  val bytes = asBytes(io.in1) ++ asBytes(io.in2)

  io.out := VecInit(indexes.map(bytes(_)).toSeq).asUInt
}

class MixColumn8(enc: Boolean) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(8.W))
    val out = Output(UInt(32.W))
  })

  def m(x: UInt, y: Int): UInt = {
    val m = Module(new GFMul(y))
    m.io.in := x
    m.io.out
  }

  val out = if (enc) Cat(m(io.in, 3), io.in, io.in, m(io.in, 2))
  else Cat(m(io.in, 0xb), m(io.in, 0xd), m(io.in, 9), m(io.in, 0xe))
  io.out := out
}

class MixColumn32(enc: Boolean) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(32.W))
    val out = Output(UInt(32.W))
  })

  def asBytes(in: UInt): Vec[UInt] = VecInit(in.asBools.grouped(8).map(VecInit(_).asUInt).toSeq)
  io.out := asBytes(io.in).zipWithIndex.map({
    case (b, i) => {
      val m = Module(new MixColumn8(enc))
      m.io.in := b
      m.io.out.rotateLeft(i * 8)
    }
  }).reduce(_ ^ _)
}

class MixColumn64(enc: Boolean) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(64.W))
    val out = Output(UInt(64.W))
  })

  io.out := VecInit(io.in.asBools.grouped(32).map(VecInit(_).asUInt).map({
    x => {
      val m = Module(new MixColumn32(enc))
      m.io.in := x
      m.io.out
    }
  }).toSeq).asUInt
}
