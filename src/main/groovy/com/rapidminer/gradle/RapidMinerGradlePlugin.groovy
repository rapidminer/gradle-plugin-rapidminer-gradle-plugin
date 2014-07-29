package com.rapidminer.gradle

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

/**
 * 
 * @author Nils Woehler
 *
 */
class RapidMinerGradlePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		PluginConfiguration extension = project.extensions.create('gradlePlugin', PluginConfiguration)

		project.configure(project) {
			apply plugin: 'groovy'
			apply plugin: 'rapidminer-release'
			apply plugin: 'rapidminer-code-quality'
			apply plugin: 'maven-publish'

			sourceCompatibility = JavaVersion.VERSION_1_7
			targetCompatibility = JavaVersion.VERSION_1_7

			buildDir = 'target'

			group 'com.rapidminer.gradle'

			// define Maven publication
			publishing {
				publications {
					mavenJava(MavenPublication) {
						from components.java
						// the code below works because we are in the
						// projects configure method. Otherwise we would have
						// to use conventionalMappings
						artifactId = extension.id
					}
				}
				repositories {
					maven {
						url "${artifactory_contextUrl}/${->project.version.contains('-SNAPSHOT') ?  'libs-snapshot-local' : 'libs-release-local'}"
						credentials {
							username = "${artifactory_user}"
							password = "${artifactory_password}"
						}
					}
				}
			}

			release { releaseTasks = [build, publish] }

			dependencies {
				compile gradleApi()
				compile localGroovy()
			}
		}
	}

}
