package sealu.aes

import chisel3.util._
import chisel3._

// refer to https://github.com/chipsalliance/rocket-chip/blob/38728ef3e57ee226caf444d95fd745935b639c4d/src/main/scala/zk/zkn.scala#L28
trait ShiftType

object LeftShift extends ShiftType

object RightShift extends ShiftType

object LeftRotate extends ShiftType

object RightRotate extends ShiftType

object barrel {

  /** A Barrel Shifter implementation for Vec type.
   *
   * @param inputs           input signal to be shifted, should be a [[Vec]] type.
   * @param shiftInput       input signal to indicate the shift number, encoded in UInt.
   * @param shiftType        [[ShiftType]] to indicate the type of shifter.
   * @param shiftGranularity how many bits will be resolved in each layer.
   *                         For a smaller `shiftGranularity`, latency will be high, but area is smaller.
   *                         For a large `shiftGranularity`, latency will be low, but area is higher.
   */
  def apply[T <: Data](inputs: Vec[T], shiftInput: UInt, shiftType: ShiftType, shiftGranularity: Int = 1): Vec[T] = {
    val elementType: T = inputs.head.cloneType
    val shiftInputBits = shiftInput.asBools.grouped(shiftGranularity).map { bits =>
      // Ensure bits is a Seq[Bool] that can be directly converted
      VecInit(bits.toSeq).asUInt
    }.toSeq

    shiftInputBits.zipWithIndex.foldLeft(inputs) { (prev, current) =>
      val (shiftBits, layer) = current
      val oneHotEncodedShiftBits = UIntToOH(shiftBits, width = log2Ceil(inputs.length)).asBools

      Mux1H(oneHotEncodedShiftBits, Seq.tabulate(prev.length) { i =>
        // Ensure the output of this block is a Vec[T]
        val layerShift = (i * scala.math.pow(2, layer * shiftGranularity).toInt).min(prev.length - 1)
        val shifted = shiftType match {
          case LeftRotate =>
            val (left, right) = prev.splitAt(layerShift)
            right ++ left
          case LeftShift =>
            val fill = Seq.fill(layerShift)(0.U.asTypeOf(elementType))
            (prev.drop(layerShift) ++ fill).take(prev.length)
          case RightRotate =>
            val (left, right) = prev.splitAt(prev.length - layerShift)
            right ++ left
          case RightShift =>
            val fill = Seq.fill(layerShift)(0.U.asTypeOf(elementType))
            (fill ++ prev.take(prev.length - layerShift)).take(prev.length)
        }
        VecInit(shifted)
      })
    }
  }

  def leftShift[T <: Data](inputs: Vec[T], shift: UInt, layerSize: Int = 1): Vec[T] =
    apply(inputs, shift, LeftShift, layerSize)

  def rightShift[T <: Data](inputs: Vec[T], shift: UInt, layerSize: Int = 1): Vec[T] =
    apply(inputs, shift, RightShift, layerSize)

  def leftRotate[T <: Data](inputs: Vec[T], shift: UInt, layerSize: Int = 1): Vec[T] =
    apply(inputs, shift, LeftRotate, layerSize)

  def rightRotate[T <: Data](inputs: Vec[T], shift: UInt, layerSize: Int = 1): Vec[T] =
    apply(inputs, shift, RightRotate, layerSize)
}

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

class AES extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(128.W))
    val valid = Input(Bool())
    val key = Input(Vec(128, UInt(8.W)))
    val is_enc = Input(Bool())
    val output = Output(UInt(128.W))
  })
  // 128bit
  //State Machine ---------------------------------------
  val numStages = 10 //for AES128

  val start = io.valid
  val counter = RegInit(0.U(4.W))
  val running = counter > 0.U

  counter := Mux(running, counter-1.U,
    Mux(start, numStages.U, counter))

  val mux_select_stage0 = counter === numStages.U

  // Ready Valid
  io.data_in.ready  := !running && io.valid
  io.data_out.valid := !running && io.valid

  //Computations -----------------------------------------
  //Initial round
  val stage0 = Module(new InvAESCipherInitStage())
  stage0.io.key_in := key_schedule(9)
  stage0.io.data_in := data_in_top
  val stage0_data_out = stage0.io.data_out

  //stages 1-8
  val data_reg    = Reg(Vec(16, UInt(8.W)))

  val InvAESStage = Module(new InvAESCipherStage)
  InvAESStage.io.data_in     := data_reg
  InvAESStage.io.key_in      := key_schedule(counter - 1.U)

  val data_next   = InvAESStage.io.data_out
  data_reg    := Mux(mux_select_stage0, stage0_data_out, data_next)

  // output round
  val stage9 = Module( new InvAESCipherStage)
  stage9.io.data_in := data_reg
  stage9.io.key_in := key_schedule(0)

  val stage10 = Module(new AddRoundKey())
  stage10.io.key_in := key_in_top
  stage10.io.data_in := stage9.io.data_out
  val stage10_data_out = stage10.io.data_out

  val data_out_top = stage10.io.data_out.asTypeOf(UInt(128.W))
  io.data_out.bits    := RegEnable(data_out_top, running)

  //Debug
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

class AESSBox extends Module {
  val io = IO(new Bundle {
    val is_enc = Input(Bool())
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })

  val enc = Module(new SBox(AES.enc))
  val dec = Module(new SBox(AES.dec))
  enc.io.in := io.in
  dec.io.in := io.in
  io.out := Mux(io.is_enc,
    enc.io.out, dec.io.out)
}

class GFMul(y: Int) extends Module {
  val io = IO(new Bundle {
    val in: UInt = Input(UInt(8.W))
    val out: UInt = Output(UInt(8.W))
  })

  // x*f(x) = 2*in in GF
  def xt(in: UInt): UInt = (in << 1)(7, 0) ^ Mux(in(7), 0x1b.U(8.W), 0x00.U(8.W))

  // 4*in in GF
  def xt2(in: UInt): UInt = xt(xt(in))

  // 8*in in GF
  def xt3(in: UInt): UInt = xt(xt2(in))

  require(y != 0)
  io.out := VecInit(
    (if ((y & 0x1) != 0) Seq((io.in)) else Nil) ++
      (if ((y & 0x2) != 0) Seq(xt(io.in)) else Nil) ++
      (if ((y & 0x4) != 0) Seq(xt2(io.in)) else Nil) ++
      (if ((y & 0x8) != 0) Seq(xt3(io.in)) else Nil)
  ).reduce(_ ^ _)
}

// shift 16 bytes
class ShiftRows(enc: Boolean) extends Module {
  val io = IO(new Bundle {
    val in: UInt = Input(UInt(128.W))
    val out: UInt = Output(UInt(128.W))
  })

  private val shifts = if (enc) Seq(0, 1, 2, 3) else Seq(0, 3, 2, 1)

  def asBytes(in: UInt): Vec[UInt] = VecInit(in.asBools.grouped(8).map(VecInit(_).asUInt).toSeq)

  private val in_bytes = asBytes(io.in)
  private val out_bytes = Wire(Vec(16, UInt(8.W)))
  for (row <- 0 until 4) {
    for (col <- 0 until 4) {
      val inIndex = (row * 4 + col) % 16
      val outIndex = (row * 4 + ((col + shifts(row)) % 4)) % 16
      out_bytes(outIndex) := in_bytes(inIndex)
    }
  }
  io.out := out_bytes.asUInt
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

  private val out = if (enc) Cat(m(io.in, 3), io.in, io.in, m(io.in, 2))
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

class MixColumn128(enc: Boolean) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(128.W))
    val out = Output(UInt(128.W))
  })

  io.out := VecInit(io.in.asBools.grouped(16).map(VecInit(_).asUInt).map({
    x => {
      val m = Module(new MixColumn8(enc))
      m.io.in := x
      m.io.out
    }
  }).toSeq).asUInt
}