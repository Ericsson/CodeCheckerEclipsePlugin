# Checkstyle for CodeChecker Eclipse plugin

## Maven
### Usage
Maven is configured to automatically run checksytle plugin during the build, no other action is needed. The current configuration won't fail the build, only emit warnings.

__Adhering to to these style checks is mandatory.__

In the future the the severity level will be changed to error, and will fail the build.

The `checkstyle.xml` file that defines the checks to be used is under the root of the project.

The Maven checkstyle plugin also configured in this folders pom.xml under the build section.

## Eclipse IDE

* ### Installation:
    * Install`Checkstyle Plug-in` from Eclipse marketplace.

* ### Confugration:
    * To configure Checkstyle plug-in go to `preferences > Checkstyle`.
    * Add the checkstyle.xml with `new` as an `External configuration file`, and specify a name also.
    ![Config Pages][conf]
    * _This step is optional:`Set as Default` the new configuration_
    * Under the project preferences tick `Checkstyle active for this project`
    and make sure that the __configuration you added__ is being used.
    ![Config Pages][projconf]

* ### Usage:
    * Checkstyle automatically monitors the project for imperfections.
    * The lines containing checkstyle problems are yellowed.
    * There is a view (Checkstyle violations) that can be opened.
    * There is a right click context item. (Apply Checkstyle fixes).

## Codacy

[Codacy](https://app.codacy.com/project/CodeChecker/CodeCheckerEclipsePlugin/dashboard) is set to use the same checkers as the project.

[conf]: img/checkstyle/ide_config.png "Configuration"
[projconf]: img/checkstyle/ide_proj_conf.png "Project Configuration"
