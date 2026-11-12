// Vendored from https://github.com/freechipsproject/ip-contributions (Apache-2.0)
// Author: Martin Schoeberl (martin@jopdesign.com)
package chisel.lib.uart

import chisel3._
import chisel3.util._

class UartIO extends DecoupledIO(UInt(8.W))

class Tx(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val txd     = Output(UInt(1.W))
    val channel = Flipped(new UartIO())
  })

  val BIT_CNT = ((frequency + baudRate / 2) / baudRate - 1).asUInt

  val shiftReg = RegInit(0x7ff.U)
  val cntReg   = RegInit(0.U(20.W))
  val bitsReg  = RegInit(0.U(4.W))

  io.channel.ready := (cntReg === 0.U) && (bitsReg === 0.U)
  io.txd           := shiftReg(0)

  when(cntReg === 0.U) {
    cntReg := BIT_CNT
    when(bitsReg =/= 0.U) {
      val shift = shiftReg >> 1
      shiftReg := Cat(1.U, shift(9, 0))
      bitsReg  := bitsReg - 1.U
    }.otherwise {
      when(io.channel.valid) {
        shiftReg := Cat(Cat(3.U, io.channel.bits), 0.U)
        bitsReg  := 11.U
      }.otherwise {
        shiftReg := 0x7ff.U
      }
    }
  }.otherwise {
    cntReg := cntReg - 1.U
  }
}

class Rx(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val rxd     = Input(UInt(1.W))
    val channel = new UartIO()
  })

  val BIT_CNT   = ((frequency + baudRate / 2) / baudRate - 1)
  val START_CNT = ((3 * frequency / 2 + baudRate / 2) / baudRate - 2)

  val rxReg   = RegNext(RegNext(io.rxd, 0.U), 0.U)
  val falling = !rxReg && (RegNext(rxReg) === 1.U)

  val shiftReg = RegInit(0.U(8.W))
  val cntReg   = RegInit(BIT_CNT.U(20.W))
  val bitsReg  = RegInit(0.U(4.W))
  val valReg   = RegInit(false.B)

  when(cntReg =/= 0.U) {
    cntReg := cntReg - 1.U
  }.elsewhen(bitsReg =/= 0.U) {
    cntReg   := BIT_CNT.U
    shiftReg := Cat(rxReg, shiftReg >> 1)
    bitsReg  := bitsReg - 1.U
    when(bitsReg === 1.U) {
      valReg := true.B
    }
  }.elsewhen(falling) {
    cntReg  := START_CNT.U
    bitsReg := 8.U
  }

  when(valReg && io.channel.ready) {
    valReg := false.B
  }

  io.channel.bits  := shiftReg
  io.channel.valid := valReg
}
