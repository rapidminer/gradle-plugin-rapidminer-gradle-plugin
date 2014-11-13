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

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

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
			apply plugin: 'com.rapidminer.gradle.release'
			apply plugin: 'com.rapidminer.gradle.code-quality'
			apply plugin: 'maven-publish'
			apply plugin: 'com.jfrog.bintray'

			sourceCompatibility = JavaVersion.VERSION_1_7
			targetCompatibility = JavaVersion.VERSION_1_7

			group 'com.rapidminer.gradle'
			
			// custom tasks for creating source/javadoc jars
			tasks.create(name:'sourcesJar', type: Jar, dependsOn: 'classes') {
				classifier = 'sources'
				from sourceSets.main.allSource
			}
		
			// add source jar tasks as artifacts
			artifacts {
				archives sourcesJar
			}

			// ensure that each Jenkins build sees updated test results (fails otherwise)
			tasks.create(name: 'updateTestTimestamps') << {
				def timestamp = System.currentTimeMillis()
				test.outputs.files.each { File output ->
					if(output.exists()) {
						output.lastModified = timestamp
						if(output.isDirectory()){
							output.eachFile { File f ->
								f.lastModified = timestamp
							}
						}
					}
				}
			}
			check.dependsOn(updateTestTimestamps)

			// define Maven publication
			publishing {
				publications {
					plugin(MavenPublication) {
						from components.java
						// the code below works because we are in the
						// projects configure method. Otherwise we would have
						// to use conventionalMappings
						artifactId = extension.id
						
						artifact sourcesJar {
							classifier "sources"
						}
					}
				}
				repositories {
					maven {
						url "${artifactory_contextUrl}${->project.version.contains('-SNAPSHOT') ?  'libs-snapshot-local' : 'libs-release-local'}"
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
			
			bintray {
				user = bintrayUser //this comes form gradle.properties file in ~/.gradle
				key = bintrayKey //this comes form gradle.properties file in ~/.gradle
				publications = ['mavenJava']
				pkg { //package will be created if does not exist
					repo = 'open-source'
					userOrg = 'rapidminer'
					name = "gradle-plugin-${->extension.id}"
					licenses = ['Apache-2.0']
				}
			}
		}
	}

}
