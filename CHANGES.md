## Change Log

#### 0.4.1
* Set default Java version back to 1.7 to avoid compatibility issues

#### 0.4.0
* Changes default Java version to 1.8
* Updates 'plugin-publish-plugin' to version 0.9.3
* Updates 'com.rapidminer.code-quality' to version 0.4.0
* Updates 'nebula-test' to version 3.1.0

#### 0.3.5
* Removed check for nexusBase URL property

#### 0.3.4
* Hard-coded Maven repository for release plugin

#### 0.3.3
* Updates Maven repo URL for new Nexus Maven repo

#### 0.3.2
* Increases nebula-test dependency to version 2.2.1

#### 0.3.1
* Removes description and displayName 'gradlePlugin' properties
* Read latest changes from CHANGES.md when publishing new plugin version to https://plugins.gradle.org/

#### 0.3.0
* Changes plugin publishing mechanism from Bintray to new 'com.gradle.plugin-publish' plugin
* Removes deprecated plugin properties files

#### 0.2.10
* Only configure remote repository if contextURL, user, and password are set
* Fixes missing property exception in case publishing to Maven repository task is not configured yet

#### 0.2.9
* Adds shortened plugin name 'com.rapidminer.gradle-plugin' to comply with plugins.gradle.org portal standards

#### 0.2.8
* Adds bintrayUpload to release tasks
* Fixes publication referenced by bintrayUpload task

#### 0.2.7
* Adds sources jar to plugin publication
* Adds Bintray plugin

#### 0.2.6
* Rename of mavenJava publication to plugin publication
* Adds Gradle 2.1 compatible plugin name 'com.rapidminer.gradle.gradle-plugin'

#### 0.2.5
* Does not set max parallel forks for testing anymore

#### 0.2.4
* Updates 'rapidminer-release' plugin to version 0.2.0
* Replaces 'rapidminer-publish' plugin by 'maven-publish' plugin
* Adds JUnit, nebula-test and Spock as test dependencies
* Changes release tasks to build and publish
* Use default Gradle build dir 'build/' instead of 'target/'

#### 0.2.3
* Updates 'rapidminer-code-quality' plugin to version 0.2.1

#### 0.2.1 - 0.2.2
* Updating to the latest 'rapidminer-release' plugin

#### 0.2.0
* Adds rapidminer-code-quality as applied plugin
* Changes default buildDir to 'target'
* Updates version of 'rapidminer-release' plugin to version 0.1.3

#### 0.1.1
* Fixes compile dependencies

#### 0.1.0
* Extension release
