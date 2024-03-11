{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-23.11";
    nixpkgs-verilator.url = "github:NixOS/nixpkgs/c459a3638a5bbcd92f14d0b73338ceb1609a0a1d";
    flake-utils.url = "github:numtide/flake-utils";
  };
  outputs = { nixpkgs, nixpkgs-verilator, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        pkgs-verilator = import nixpkgs-verilator { inherit system; };
      in {
        devShells.default =
          pkgs.mkShell {
            name = "rules_nixpkgs_shell";
            packages = with pkgs; [ sbt scala pkgs-verilator.verilator ];
          };
      });
}
