
## Troubleshooting

* In case of compile error, attach the maven output
* Runtime logging is still a big todo, currently most log will appear on stdout/stderr

## Development Notes

* Requires the Tycho Connector, Eclipse will automatically install it for you when you first import the project
* Requires Eclipse PDE (Plug-in Development Environment)
* WindowMaker plugin is recommended
* Adherence to the [Google Java Style](https://google.github.io/styleguide/javaguide.html) is mandatory
    * An IntelliJ Idea autoformatter XML configuration file is included in the repo for convenience

## Version Update

Execute the following command in CodeCheckerEclipsePlugin/eclipse-plugin.
``` mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=X.X.X-SNAPSHOT -Dartifacts=cc.codechecker.eclipse.plugin ```

## Log

Logging in the plugin is handled by `log4j` from Apache. The default loglevel is `ERROR`, and the following loglevels are used, in increasing severity order:

 * `TRACE`
 * `DEBUG`
 * `INFO`
 * `WARN`
 * `ERROR`
 * `FATAL`

The log configuration file's location is `eclipse-plugin/eclipse/cc.codechecker.eclipse.plugin/log4j.properties` before compilation.

After compiling the plugin, you may change the log level (which specifies which *least severe* category will be logged) by opening the built `cc.codechecker.eclipse.plugin-0.0.1-SNAPSHOT.jar` file (with `mc` or any `zip` editor) and changing the `log4j.properties` file therein.

The loglevel is specified as follows:

    log4j.appender.log.threshold=ERROR

After modifying the jar file, you will need to restart eclipse for the changes to take effect.
