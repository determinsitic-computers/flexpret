// Vendored from https://github.com/t-crest/soc-comm (BSD-2-Clause)
package wishbone

import chisel3._

class WishboneIO(addrWith: Int) extends Bundle {
  val addr   = Output(UInt(addrWith.W))
  val wrData = Output(UInt(32.W))
  val rdData = Input(UInt(32.W))
  val we     = Output(Bool())
  val sel    = Output(UInt(4.W))
  val stb    = Output(Bool())
  val ack    = Input(Bool())
  val cyc    = Output(Bool())

  def setDefaults(): Unit = {
    addr   := 0.U
    wrData := 0.U
    we     := false.B
    cyc    := false.B
    sel    := 0.U
    stb    := 0.U
  }

  def setDefaultsFlipped(): Unit = {
    rdData := 0.U
    ack    := false.B
  }

  def driveReadReq(_addr: UInt): Unit = {
    addr   := _addr
    wrData := 0.U
    we     := false.B
    cyc    := true.B
    sel    := 15.U
    stb    := true.B
  }

  def driveWriteReq(_addr: UInt, data: UInt): Unit = {
    addr   := _addr
    wrData := data
    we     := true.B
    cyc    := true.B
    sel    := 15.U
    stb    := true.B
  }
}
