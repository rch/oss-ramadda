{ pkgs, ... }:

{
  languages.java.enable = true;
  languages.java.jdk.package = pkgs.jdk17;
  languages.java.maven.enable = true;

  packages = [ pkgs.ant ];

  pre-commit.hooks.markdownlint.enable = true;
  pre-commit.hooks.shellcheck.enable = true;

  processes = {
    ramaddaserver.exec = "sh dist/ramaddaserver/ramadda.sh";
  };
}

