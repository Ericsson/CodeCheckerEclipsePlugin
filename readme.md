# Readme

## About

This is an Eclipse plugin for the Ericsson Codechecker Project - it's capable of connecting to a remote CodeChecker server for a readonly view, or running one locally.

## Requirements

* Recent CodeChecker
* Eclipse: the plugin is currently tested with Eclipse Mars, but any recent Eclipse version should work
* Eclipse CDT
* Java 1.7
* Thrift 0.9.1 or later

## Installing Thrift 0.9.1

* `curl http://archive.apache.org/dist/thrift/0.9.1/thrift-0.9.1.tar.gz | tar zx`
* `cd thrift-0.9.1/`
* `./configure`
* `make`
* add `thrift-0.9.1/compiler/cpp/` to PATH

## How to build

* run `mvn package` in the project's directory

## Installing

* Copy the `cc.codechecker.eclipse.plugin.<XXX>.jar`:
    * From: the plugins subdirectory of the repository
    * To: the dropins directory of Eclipse

## Troubleshooting

* In case of compile error, attach the maven output
* Runtime logging is still a big todo, currently most log will appear on stdout/stderr

## Development Notes

* Requires the Tycho Connector, Eclipse will automatically install it for you when you first import the project
* Requires Eclipse PDT
* WindowMaker plugin is recommended
* Adherence to the [Google Java Style](https://google.github.io/styleguide/javaguide.html) is mandatory
    * An IntelliJ Idea autoformatter XML configuration file is included in the repo for convenience

## How to use

Make sure that before staring Eclipse:

* CodeChecker/bin directory is included in PATH (e.g.: `export PATH="/home/<username>/CodeChecker/bin/:$PATH"`)
* Python virtualenv with CC dependencies is sourced (e.g.: `source /home/<username>/venv/bin/activate`)

Currently the plugin is usable with a CDT project.

In Eclipse, select Window, Perspectives, and activate the CodeChecker perspective.
Alternatively, you can manually add the two windows under the CodeChecker category into any perspective, using the Window - Views menu.

The plugin is activated on a per project basis, first you have to add the CodeChecker Nature to a CDT project using it's context menu in the project explorer.

After that, the settings can be customized in the project preferences window under the CodeChecker panel. For the plugin to work correctly, it is mandatory to add the correct path to the root of the CodeChecker package.

After the plugin is successfully configured for a project, it'll listen to build events, and automatically rechecks the project with CodeChecker when needed.

By default it displays the problems related to the currently selected file on the problems view, and the details for a selected bugpath in the Details view. Selecting a problem or jumping to a details item is possible with double clicking on it.

The problems view can be customized: it supports custom search options with a filter editor, where saving or loading filters is also possible.
