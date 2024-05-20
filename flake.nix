{
  description = "scalatromino";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-23.11";

  inputs.sbt.url = "github:zaninime/sbt-derivation/master";

  inputs.sbt.inputs.nixpkgs.follows = "nixpkgs";

  inputs.systems.url = "github:nix-systems/default";

  outputs = {
    self,
    nixpkgs,
    sbt,
    systems
  }:
    let
      eachSystem = nixpkgs.lib.genAttrs (import systems);
    in
    {
      packages = eachSystem (system:
        let pkgs = nixpkgs.legacyPackages.${system}; in
        {
          default = (sbt.mkSbtDerivation.${system}).withOverrides({ stdenv = pkgs.llvmPackages_15.stdenv; }) {
            pname = "scalatromino";
            version = "0.1.0";
            src = self;
            depsSha256 = "sha256-oanfXUUAoIwTMr+gRm/TwzhQr0orxFseAkpRBJq5FnY=";
            buildPhase = ''
              sbt 'show compile'
            '';
            depsWarmupCommand = ''
              sbt '+Test/updateFull ; +Test/compileIncSetup'
            '';
            installPhase = ''
              sbt 'show stage'
              mkdir -p $out/bin
              cp target/scalatromino $out/bin/
            '';
            buildInputs = with pkgs; [
              gtk4
            ];
            nativeBuildInputs = with pkgs; [
              boehmgc
              libunwind
              pkg-config
              stdenv
              which
              zlib
            ];
            env.NIX_CFLAGS_COMPILE = "-Wno-unused-command-line-argument";
            hardeningDisable = [ "fortify" ];
          };
        }
      );
    };
}
