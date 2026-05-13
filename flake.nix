{
  description = "karoowind - Hammerhead Karoo 3 extension for Wahoo Headwind";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config = {
            android_sdk.accept_license = true;
            allowUnfree = true;
          };
        };

        androidComposition = pkgs.androidenv.composeAndroidPackages {
          buildToolsVersions = [ "34.0.0" ];
          platformVersions = [ "34" ];
          abiVersions = [ "arm64-v8a" "x86_64" ];
          includeNDK = false;
          includeEmulator = false;
          includeSystemImages = false;
        };

        androidSdk = androidComposition.androidsdk;
        androidSdkRoot = "${androidSdk}/libexec/android-sdk";
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = [
            pkgs.jdk17
            pkgs.gradle
            androidSdk
          ];

          ANDROID_SDK_ROOT = androidSdkRoot;
          ANDROID_HOME = androidSdkRoot;
          JAVA_HOME = "${pkgs.jdk17}";

          shellHook = ''
            echo "karoowind dev environment"
            echo "  Java:    $(java -version 2>&1 | head -1)"
            echo "  Gradle:  $(gradle --version 2>/dev/null | grep Gradle | head -1)"
            echo "  Android SDK: $ANDROID_SDK_ROOT"
          '';
        };
      }
    );
}
