// Vendored from https://github.com/t-crest/soc-comm (BSD-2-Clause)
package wishbone

import chisel3._

abstract class WishboneDevice(addrWidth: Int) extends Module {
  val io = IO(new Bundle {
    val port = Flipped(new WishboneIO(addrWidth))
  })
}
