# SnipSniper

The Screenshot Tool

  - Up to 7 profiles!
  - Multi Monitor!
  - Customize each profile for its usecase!
  - Screenshot Editor!
  - Image viewer!

<p float="left">
  <img title="SnipSniper Icon" src="https://github.com/CaptureCoop/SnipSniper/raw/master/src/main/resources/net/snipsniper/resources/img/svg/snipsniper.svg" width="10%"/>
  <img title="Editor Icon" src="https://github.com/CaptureCoop/SnipSniper/raw/master/src/main/resources/net/snipsniper/resources/img/svg/editor.svg" width="10%"/>
  <img title="Viewer Icon" src="https://github.com/CaptureCoop/SnipSniper/raw/master/src/main/resources/net/snipsniper/resources/img/svg/viewer.svg" width="10%"/>
  <img title="Console Icon" src="https://github.com/CaptureCoop/SnipSniper/raw/master/src/main/resources/net/snipsniper/resources/img/svg/console.svg" width="10%"/>
</p>

### Installation

SnipSniper currently works fully on Windows and is in development for Linux, which means that a few features might not be complete yet on Linux.

Download SnipSniper:
- [Latest stable release](https://github.com/CaptureCoop/SnipSniper/releases/latest/)
- [Latest development build](https://github.com/CaptureCoop/SnipSniper/actions/workflows/dev.yml)

There are dedicated Windows Versions available, but of course the classic standalone jar also works perfectly fine.

On Windows we also offer our own installer, available [here](https://github.com/CaptureCoop/SnipSniperInstaller/releases/latest/download/SnipSniperInstaller.jar)

### Building

To build SnipSniper we recommend an IDE like IntelliJ that supports Gradle Projects.

Simply import the SnipSniper project and build the gradle project.

We recommend a java version above or equal to Java 8.

Should you want to build more then the jar, as in the Windows Portable/Installer Versions you can use our make.bat file, which works under Windows.

For this to work you need to have 7zip (for portable) and nsis (for installer) installed, and you need to provide a jdk under "jvm-creator".

### Libraries

SnipSniper uses these libraries in order to function properly

* [jnativehook](https://github.com/kwhat/jnativehook) - Global keyboard and mouse hooking for Java.

* [Apache Commons lang3](http://commons.apache.org/proper/commons-lang/) - Provides highly reusable static utility methods.

* [Apache Commons Text](https://commons.apache.org/proper/commons-text/) -  Apache Commons Text is a library focused on algorithms working on strings.

* [org.json](https://www.json.org/) - JSON is a light-weight, language independent, data interchange format.

* [FlatLaf](https://www.formdev.com/flatlaf/) - FlatLaf is a modern open-source cross-platform Look and Feel for Java Swing desktop applications.

* [mslinks](https://mvnrepository.com/artifact/com.erigir/mslinks/0.0.2+5) - Used for creating Shortcuts.

### Credits

* [Flags](http://www.famfamfam.com/lab/icons/flags/) We use images provided by fafmfamfam.com for country flags

License
----

MIT

Copyright 2020 - 2022 CaptureCoop.org

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


