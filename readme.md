# About

[![Build Status](https://travis-ci.org/Ericsson/CodeCheckerEclipsePlugin.svg?branch=master)](https://travis-ci.org/Ericsson/CodeCheckerEclipsePlugin)
This is a C/C++ code analysis plugin for Eclipse that shows bugs detected by the [Clang Static Analyzer](http://clang-analyzer.llvm.org/) and [Clang Tidy](http://clang.llvm.org/extra/clang-tidy/) analyzers.

# Requirements

* Linux operating system
* Recent [CodeChecker](https://github.com/Ericsson/codechecker)
* Clang 3.6
* [Eclipse](www.eclipse.org): the plugin is currently tested with Eclipse Neon, but any recent Eclipse version should work
* Eclipse CDT
* Java 1.7
* [Thrift](https://thrift.apache.org/) 0.9.1 or later

### Install or build Thrift 0.9.1
##### install:
using a package manager provided by your OS:  
Ubuntu:  
```sudo apt-get install -y thrift-compiler```

##### build from source:
~~~~~~~~.sh
curl http://archive.apache.org/dist/thrift/0.9.1/thrift-0.9.1.tar.gz | tar zx
cd thrift-0.9.1
./configure
make
# add `compiler/cpp/` to PATH where the thrift binary is available
export PATH=$PATH:$PWD/compiler/cpp/
~~~~~~~~



# Build and install CodeChecker Eclipse Plugin
## Build
* run `mvn package` in the project's directory

## Install
* Copy the `cc.codechecker.eclipse.plugin-0.0.1-SNAPSHOT.jar`:
    * From: ./eclipse-plugin/eclipse/cc.codechecker.eclipse.plugin/target/
    * To: the dropins directory of Eclipse

# How to use
Make sure that before staring Eclipse:

* CodeChecker/bin directory is included in PATH (e.g.: `export PATH="/home/<username>/CodeChecker/bin/:$PATH"`)
* Python virtualenv with CodeChecker dependencies is sourced (e.g.: `source /home/<username>/venv/bin/activate`)

__Currently the plugin is usable with a CDT project.__

#### 1. Setup Perspective
In Eclipse, select Window, Perspectives, and activate the CodeChecker perspective.
Alternatively, you can manually add the two windows under the CodeChecker category into any perspective, using the Window - Views menu.

![Window->Perspective->Open Perspective->Other](docs/allperspective.png)

#### 2. Setup Nature
The plugin is activated on a per project basis, first you have to add the CodeChecker Nature to a CDT project using it's context menu in the project explorer.

![CodeChecker Nature Add](docs/nature.png)

#### 3. Configure CodeChecker and checkers
After that, the settings can be customized in the project preferences window under the CodeChecker panel. For the plugin to work correctly, it is mandatory to add the correct path to the root of the CodeChecker package. You can download CodeChecker from [here](https://github.com/Ericsson/codechecker).

![CodeChecker Configure](docs/config.png)
![CodeChecker Checkers Configure](docs/checkershow.png)

After the plugin is successfully configured for a project, it'll listen to build events, and automatically rechecks the project with CodeChecker when needed.

#### 4. Set up eclipse C/C++ build environment

The runs only if your project can be built by Eclipse (as it intercepts gcc build commands).
Please check that your builder is set up correctly in properties/C/C++ Build settings.
If all goes well, you should be able to build your project from Eclipse using the Project/Build Project menu item.

#### 5. Analyze C/C++ project and view results
By default it displays the problems related to the currently selected file on the problems view, and the details for a selected bugpath in the Details view. Selecting a problem or jumping to a details item is possible with double clicking on it.

*Please note that codechecker will only show analysis results for files that you built. This means that if you want to see all bugs for your project you will need to do a full (clean) build. After you update a source file, analysis results will be updated when you rebuild your project.*

The problems view can be customized: it supports custom search options with a filter editor, where saving or loading filters is also possible.

![CodeChecker Runtime Example](docs/example.png)

## Troubleshooting and Development

For further information see [developer documentation](docs/developer.md).
