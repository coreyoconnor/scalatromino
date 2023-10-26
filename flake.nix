{
  description = "scala-gtk-example";

  # you probably have this one already
  inputs.nixpkgs.url = "github:NixOS/nixpkgs";

  # add this line
  inputs.sbt.url = "github:zaninime/sbt-derivation/master";

  # recommended for first style of usage documented below, but not necessary
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
          default = sbt.mkSbtDerivation.x86_64-linux {
            pname = "scala-gtk-example";
            version = "0.1.0";
            src = self;
            depsSha256 = "sha256-BS7bEtrkJTEj2aJ91L1NAA06W0CKIYlG0v3w7qqgyE8=";
            buildPhase = ''
              sbt 'show stage'
            '';
            installPhase = ''
              mkdir -p $out/bin
              cp target/scala-gtk-example $out/bin/
            '';
            LLVM_BIN = pkgs.clang + "/bin";
            buildInputs = with pkgs; [
              gtk4
            ];
            nativeBuildInputs = with pkgs; [
              boehmgc
              clang
              ccls
              libevdev
              libunwind
              pkg-config
              stdenv
              which
              zlib
            ];
          };
        }
      );
    };
}
