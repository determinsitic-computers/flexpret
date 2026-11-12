{
  description = "FlexPRET development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        # riscv32-embedded uses ilp32d (double-float) as its default ABI, which
        # is incompatible with FlexPRET's rv32i soft-float target (-mabi=ilp32).
        # Use a custom crossSystem that defaults to ilp32 so libgcc is soft-float.
        riscvPkgs = import nixpkgs {
          inherit system;
          crossSystem = {
            config = "riscv32-none-elf";
            libc = "newlib";
            gcc = {
              arch = "rv32i";
              abi = "ilp32";
            };
          };
        };
        riscvToolchain = riscvPkgs.buildPackages.gcc;
        riscvBinutils = riscvPkgs.buildPackages.binutils;
      in {
        devShells.default = pkgs.mkShell {
          packages = [
            # RISC-V bare-metal toolchain (riscv32-none-elf-gcc)
            riscvToolchain
            riscvBinutils

            # Simulation
            pkgs.verilator

            # Chisel / Scala build
            pkgs.sbt
            pkgs.jdk21

            # Common utilities
            pkgs.gnumake
            pkgs.cmake
            pkgs.python3
            pkgs.git
          ];

          shellHook = ''
            export RISCV_TOOLCHAIN_ROOT=${riscvToolchain}
            export RISCV_TOOL_PATH_PREFIX=${riscvToolchain}
            export RISCV_TOOLCHAIN_PREFIX=riscv32-none-elf-
            echo "FlexPRET dev shell"
            echo "  riscv32-none-elf-gcc: $(riscv32-none-elf-gcc --version 2>&1 | head -1)"
            echo "  verilator:            $(verilator --version 2>&1 | head -1)"
            echo "  sbt:                  $(sbt --version 2>&1 | tail -1)"
          '';
        };
      });
}
