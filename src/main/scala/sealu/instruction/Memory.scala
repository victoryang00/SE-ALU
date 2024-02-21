package sealu.instruction

import chisel3._

class Memory(init_mem: Vec[UInt],size:Int) extends Module {
  val io = IO(new Bundle {
    val readAddr = Input(UInt(4.W)) // 4 bits for addressing 0 to 15 locations
    val writeAddr = Input(UInt(4.W))
    val writeData = Input(UInt(64.W))
    val writeEnable = Input(Bool())
    val readData = Output(UInt(64.W))
  })

  // Initialize the memory
  val init_memory: Vec[UInt] = RegInit(init_mem)

  // Simple memory read logic
  io.readData := init_memory(io.readAddr)

  // Simple memory write logic
  when(io.writeEnable) {
    init_memory(io.writeAddr) := io.writeData
  }
}