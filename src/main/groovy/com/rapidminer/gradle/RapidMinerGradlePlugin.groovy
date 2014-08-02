/*
 * Copyright 2013-2014 RapidMiner GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
			
			def maxForks = Runtime.runtime.availableProcessors() -1
			tasks.withType(org.gradle.api.tasks.testing.Test) {
				maxParallelForks = maxForks
			}

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

			release { 
				releaseRepositoryUrl = "${artifactory_contextUrl}/libs-release-local"
				snapshotRepositoryUrl= "${artifactory_contextUrl}/libs-snapshot-local"
				releaseTasks = [build, publish] 
			}

			dependencies {
				compile gradleApi()
				
				// testing
				testCompile 'junit:junit:4.11'
				testCompile('org.spockframework:spock-core:0.7-groovy-2.0') { exclude group: 'org.codehaus.groovy' }
				
				// Adds TempDirectory annotation
				testCompile('com.energizedwork:spock-extensions:1.0')  {
					exclude group: 'org.codehaus.groovy'
					exclude group: 'org.spockframework'
				}
				
				testCompile('com.netflix.nebula:nebula-test:1.12.0') {
					exclude group: 'org.codehaus.groovy'
					exclude group: 'org.spockframework'
				}
			}
		}
	}

}
