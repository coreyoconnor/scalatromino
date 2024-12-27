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
        buildTools = with pkgs; [
          pkg-config
          which
          glib.dev
        ];
        libraries = with pkgs; [
          boehmgc
          cairo
          gdk-pixbuf
          graphene
          harfbuzz
          libunwind
          glib
          gtk4
          pango
          vulkan-headers
          vulkan-loader
          zlib
        ];
      in
      rec {
        packages = {
          default = (sbt.mkSbtDerivation.${system}).withOverrides({ stdenv = pkgs.llvmPackages.stdenv; }) {
            pname = "scalatromino";
            version = "0.1.0";
            src = self;
            depsSha256 = "sha256-V+z1OfjlWT5bsxGd3kqKi0MDuLxybUrSg/Yrz7yhL2k=";
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
            buildInputs = libraries;
            nativeBuildInputs = buildTools;
            hardeningDisable = [ "fortify" ];
          };
        };

        devShell = pkgs.devshell.mkShell {
          imports = [ typelevel-nix.typelevelShell "${pkgs.devshell.extraModulesDir}/language/c.nix"];
          name = "scalatromino-devshell";
          typelevelShell = {
            jdk.package = pkgs.jdk21;
            native.enable = true;
          };
          packagesFrom = [ packages.default ];
          language.c = {
            inherit libraries;
            includes = libraries;
          };
        };
    });
}
