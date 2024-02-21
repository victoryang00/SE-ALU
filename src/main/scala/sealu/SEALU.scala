package sealu

import chisel3._
import chisel3.util._
import aes._
import instruction._
import chisel3.util.random._

case class SEALUParams(
                        val key: Vec[Vec[UInt]] = VecInit(VecInit(0x00.U(8.W), 0x01.U(8.W), 0x02.U(8.W), 0x03.U(8.W), 0x04.U(8.W), 0x05.U(8.W), 0x06.U(8.W), 0x07.U(8.W), 0x08.U(8.W), 0x09.U(8.W), 0x0a.U(8.W), 0x0b.U(8.W), 0x0c.U(8.W), 0x0d.U(8.W), 0x0e.U(8.W), 0x0f.U(8.W)),
                          VecInit(0xd6.U(8.W), 0xaa.U(8.W), 0x74.U(8.W), 0xfd.U(8.W), 0xd2.U(8.W), 0xaf.U(8.W), 0x72.U(8.W), 0xfa.U(8.W), 0xda.U(8.W), 0xa6.U(8.W), 0x78.U(8.W), 0xf1.U(8.W), 0xd6.U(8.W), 0xab.U(8.W), 0x76.U(8.W), 0xfe.U(8.W)),
                          VecInit(0xb6.U(8.W), 0x92.U(8.W), 0xcf.U(8.W), 0x0b.U(8.W), 0x64.U(8.W), 0x3d.U(8.W), 0xbd.U(8.W), 0xf1.U(8.W), 0xbe.U(8.W), 0x9b.U(8.W), 0xc5.U(8.W), 0x00.U(8.W), 0x68.U(8.W), 0x30.U(8.W), 0xb3.U(8.W), 0xfe.U(8.W)),
                          VecInit(0xb6.U(8.W), 0xff.U(8.W), 0x74.U(8.W), 0x4e.U(8.W), 0xd2.U(8.W), 0xc2.U(8.W), 0xc9.U(8.W), 0xbf.U(8.W), 0x6c.U(8.W), 0x59.U(8.W), 0x0c.U(8.W), 0xbf.U(8.W), 0x04.U(8.W), 0x69.U(8.W), 0xbf.U(8.W), 0x41.U(8.W)),
                          VecInit(0x47.U(8.W), 0xf7.U(8.W), 0xf7.U(8.W), 0xbc.U(8.W), 0x95.U(8.W), 0x35.U(8.W), 0x3e.U(8.W), 0x03.U(8.W), 0xf9.U(8.W), 0x6c.U(8.W), 0x32.U(8.W), 0xbc.U(8.W), 0xfd.U(8.W), 0x05.U(8.W), 0x8d.U(8.W), 0xfd.U(8.W)),
                          VecInit(0x3c.U(8.W), 0xaa.U(8.W), 0xa3.U(8.W), 0xe8.U(8.W), 0xa9.U(8.W), 0x9f.U(8.W), 0x9d.U(8.W), 0xeb.U(8.W), 0x50.U(8.W), 0xf3.U(8.W), 0xaf.U(8.W), 0x57.U(8.W), 0xad.U(8.W), 0xf6.U(8.W), 0x22.U(8.W), 0xaa.U(8.W)),
                          VecInit(0x5e.U(8.W), 0x39.U(8.W), 0x0f.U(8.W), 0x7d.U(8.W), 0xf7.U(8.W), 0xa6.U(8.W), 0x92.U(8.W), 0x96.U(8.W), 0xa7.U(8.W), 0x55.U(8.W), 0x3d.U(8.W), 0xc1.U(8.W), 0x0a.U(8.W), 0xa3.U(8.W), 0x1f.U(8.W), 0x6b.U(8.W)),
                          VecInit(0x14.U(8.W), 0xf9.U(8.W), 0x70.U(8.W), 0x1a.U(8.W), 0xe3.U(8.W), 0x5f.U(8.W), 0xe2.U(8.W), 0x8c.U(8.W), 0x44.U(8.W), 0x0a.U(8.W), 0xdf.U(8.W), 0x4d.U(8.W), 0x4e.U(8.W), 0xa9.U(8.W), 0xc0.U(8.W), 0x26.U(8.W)),
                          VecInit(0x47.U(8.W), 0x43.U(8.W), 0x87.U(8.W), 0x35.U(8.W), 0xa4.U(8.W), 0x1c.U(8.W), 0x65.U(8.W), 0xb9.U(8.W), 0xe0.U(8.W), 0x16.U(8.W), 0xba.U(8.W), 0xf4.U(8.W), 0xae.U(8.W), 0xbf.U(8.W), 0x7a.U(8.W), 0xd2.U(8.W)),
                          VecInit(0x54.U(8.W), 0x99.U(8.W), 0x32.U(8.W), 0xd1.U(8.W), 0xf0.U(8.W), 0x85.U(8.W), 0x57.U(8.W), 0x68.U(8.W), 0x10.U(8.W), 0x93.U(8.W), 0xed.U(8.W), 0x9c.U(8.W), 0xbe.U(8.W), 0x2c.U(8.W), 0x97.U(8.W), 0x4e.U(8.W)),
                          VecInit(0x13.U(8.W), 0x11.U(8.W), 0x1d.U(8.W), 0x7f.U(8.W), 0xe3.U(8.W), 0x94.U(8.W), 0x4a.U(8.W), 0x17.U(8.W), 0xf3.U(8.W), 0x07.U(8.W), 0xa7.U(8.W), 0x8b.U(8.W), 0x4d.U(8.W), 0x2b.U(8.W), 0x30.U(8.W), 0xc5.U(8.W))),
                        val init_memory: Vec[UInt] = VecInit(0x00.U(32.W), 0x01.U(32.W), 0x02.U(32.W), 0x03.U(32.W), 0x04.U(32.W), 0x05.U(32.W), 0x06.U(32.W), 0x07.U(32.W), 0x08.U(32.W), 0x09.U(32.W), 0x0a.U(32.W), 0x0b.U(32.W), 0x0c.U(32.W), 0x0d.U(32.W), 0x0e.U(32.W), 0x0f.U(32.W)),
                        val size: Int = 16,
                        val plain_text: Int = 0xdeadbeef,
                      )

class SEALUIO(size: Int) extends Bundle {
  val io = new Bundle {
    val input_data = Input(Vec(size, UInt(6.W)))
    val input1_data = Input(Vec(size, UInt(4.W)))
    val input2_data = Input(Vec(size, UInt(4.W)))
    val inputcond_data = Input(Vec(size, UInt(4.W)))
  }
  val output = new Bundle {
    val result = Output(UInt(128.W))
    val valid = Output(Bool())
    val ready = Input(Bool())
    val counter = Output(UInt(8.W))
  }
}

class SEALU(p: SEALUParams) extends Module {
  val io = IO(new SEALUIO(100))

  val mem = Module(new Memory(p.init_memory, p.size))
  val cnter = new Counter(100)

  val sealuop = Module(new Opcode())


  // cypher text initial value
  val cypher_text = RegInit(0.U(8.W))

}