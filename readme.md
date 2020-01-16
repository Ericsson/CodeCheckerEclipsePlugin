# CodeChecker Eclipse Plugin

[![Build Status](https://travis-ci.org/Ericsson/CodeCheckerEclipsePlugin.svg?branch=master)](https://travis-ci.org/Ericsson/CodeCheckerEclipsePlugin)

This is a C/C++ code analysis plugin for Eclipse that shows bugs detected by the [Clang Static Analyzer](http://clang-analyzer.llvm.org/) and [Clang Tidy](http://clang.llvm.org/extra/clang-tidy/) analyzers, using [CodeChecker](https://github.com/Ericsson/codechecker) as a backend,

## Requirements

* Linux operating system

* Recent [CodeChecker](https://github.com/Ericsson/codechecker) (6.8.0 and up)

* Every eclipse version since [Phtoton](https://www.eclipse.org/downloads/packages/release/photon/r) (v4.8.0) is supported
  * Should work with v4.7.0 Oxygen, but this version is untested currently.

* Eclipse CDT

* Java SE 1.8 (for building, an equivalent jdk).

## Build and install CodeChecker Eclipse Plugin

### Build

Run `mvn -f mavendeps/pom.xml p2:site && mvn clean verify` in the root of the project.

### Install

* Add the generated update site thats located under `path/to/cloned/project/releng/org.codechecker.eclipse.update/target/repository/` to `Help -> Install New Software...` in Eclipse
  * Alternatively, use the archive from the Releases page, as an update site.
* Select the newly added repository if not already selected
* Mark CodeChecker Eclipse Plugin then hit next.
* If an alert box comes up with unsigned content. Just accept it.

## How to use
Make sure that before staring Eclipse:

* CodeChecker/bin directory is included in PATH (e.g.: `export PATH="/home/<username>/CodeChecker/bin/:$PATH"`)

__Currently the plugin is only usable with a CDT project.__

#### 1. Setup Perspective
In Eclipse, select Window, Perspectives, and activate the CodeChecker perspective.
Alternatively, you can manually add the two windows under the CodeChecker category into any perspective, using the Window - Views menu.  
__Make sure to check if CDT is installed properly if you do not see the CodeChecker perspective on the list!__

![Window->Perspective->Open Perspective->Other](docs/allperspective.png)

#### 2. Setup Nature
The plugin is activated on a per project basis, first you have to add the __CodeChecker Nature__ to a CDT project using the projects context menu in the project explorer with `Add CodeChecker Nature` command.

![CodeChecker Nature Add](docs/nature.png)

#### 3. Configure CodeChecker and checkers

After the __CodeChecker Nature__  is added to the the project, the plugin can be configured globally in `Window -> Preferences -> CodeChecker` panel, or for the individual project, from the `right click context menu -> Properties -> CodeChecker` page.
In the first section, you select how and which CodeChecker will be used. After that set some analysis related settings. You should build CodeChecker in a __standalone package__ configuration.

``` bash
cd codechecker
make standalone_package
export PATH=$PWD/build/CodeChecker/bin:$PATH
```

Then add it to the PATH environment variable, and use the Search in PATH option. Alternatively you can specify a different instance with the Pre built package option. But be aware that the plugin not supports virtual environment, that CodeChecker needs in the default configuration. You can download and compile CodeChecker from [here](https://github.com/Ericsson/codechecker).

To specify checkers or profiles, please add them to the Extra analysis options field.

At the bottom the command that is similar to that to be executed is displayed, for easier commandline reproduction.

![CodeChecker Configure](docs/config.png)

After the plugin is successfully configured for a project, it'll listen to build events, and automatically rechecks the project with CodeChecker when needed.

#### 4. Analyze C/C++ project and view results

By default the plugin displays the problems related to the currently selected file on the current file reports view. Here the viewer can decide to check the individual reports selected from the reports tree, and view the related bug-path. Double clicking on a bug-path item will jump and set the cursor to that line in the editor.

The analysis is triggered when opening a file, or on a file save event, for that particular file. Currently there is no full project analysis. *Please note that codechecker will only show analysis results for files that you built. in the Current project View*

![CodeChecker Runtime Example](docs/example.png)

## Contributing

For further information see [developer documentation](docs/developer.md).
