{
  description = "scalatromino";

  inputs = {
    flake-utils.follows = "typelevel-nix/flake-utils";
    nixpkgs.follows = "typelevel-nix/nixpkgs";
    sbt = {
      url = "github:zaninime/sbt-derivation/master";
      inputs.nixpkgs.follows = "typelevel-nix/nixpkgs";
    };
    typelevel-nix.url = "github:typelevel/typelevel-nix";
  };

  outputs = {
    self,
    flake-utils,
    nixpkgs,
    sbt,
    typelevel-nix
  }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ typelevel-nix.overlays.default ];
        };
      in
      {
        packages = {
          default = (sbt.mkSbtDerivation.${system}).withOverrides({ stdenv = pkgs.llvmPackages.stdenv; }) {
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
              boehmgc
              libunwind
              gtk4
              zlib
            ];
            nativeBuildInputs = with pkgs; [
              pkg-config
              which
              glib.dev
            ];
            env.NIX_CFLAGS_COMPILE = "-Wno-unused-command-line-argument";
            hardeningDisable = [ "fortify" ];
          };
        };

        devShell = pkgs.devshell.mkShell {
          imports = [ typelevel-nix.typelevelShell ];
          name = "scalatromino-devshell";
          typelevelShell = {
            jdk.package = pkgs.jdk21;
            native = {
              enable = true;
              libraries = with pkgs; [
                boehmgc
                libunwind
                gtk4
                zlib
              ];
            };
          };
          packages = with pkgs; [
            pkg-config
            which
            glib.dev
          ];
          env = [
            { name = "NIX_CFLAGS_COMPILE"; value = "-Wno-unused-command-line-argument"; }
          ];
        };
    });
}
