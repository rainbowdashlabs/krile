{ pkgs ? import <nixpkgs> {}, ... }:

let
jdk = pkgs.jdk25;
gradle= pkgs.gradle.override { java = jdk; };
in
pkgs.mkShell
{
  packages = with pkgs; [jdk gradle];
}

