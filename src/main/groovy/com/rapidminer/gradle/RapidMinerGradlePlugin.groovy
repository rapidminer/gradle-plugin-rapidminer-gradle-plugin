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
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

/**
 *
 * The Plugin class which configures the project as a Gradle plugin project.
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
			apply plugin: 'maven-publish'
			apply plugin: 'com.rapidminer.release'
			apply plugin: 'com.rapidminer.code-quality'
			apply plugin: 'com.gradle.plugin-publish'

			sourceCompatibility = JavaVersion.VERSION_1_7
			targetCompatibility = JavaVersion.VERSION_1_7

			group 'com.rapidminer.gradle'
			
			// custom tasks for creating source/javadoc jars
			tasks.create(name:'sourcesJar', type: Jar, dependsOn: 'classes') {
				classifier = 'sources'
				from sourceSets.main.allSource
			}
		
			// add source jar tasks as artifacts
			artifacts { archives sourcesJar }

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
				// Only set remote Maven repository if user, password, and contextURL are set
				if(project.hasProperty('artifactory_user') && 
					project.hasProperty('artifactory_password') && 
					project.hasProperty('artifactory_contextUrl')) {
					logger.info 'Found Artifactory properties. Applying remote repository publishing configuration.'
					repositories {
						maven {
							url "${artifactory_contextUrl}${->project.version.contains('-SNAPSHOT') ?  'libs-snapshot-local' : 'libs-release-local'}"
							credentials {
								username = "${artifactory_user}"
								password = "${artifactory_password}"
							}
						}
					}
				} else {
					logger.info 'Not applying remote repository publishing configuration as Artifactory repository is not configured properly.'
				}
			}

			dependencies {
				compile gradleApi()
				compile localGroovy()

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

			// All code below needs to be executed in afterEvaluate as we need access to the configured 'gradlePlugin' extension
			afterEvaluate {
				if(!extension.description){
					throw new RuntimeException('Missing extension description!')
				}
				if(!extension.displayName){
					throw new RuntimeException('Missing display name!')
				}

				pluginBundle {
					website = 'https://www.rapidminer.com'
					vcsUrl = "https://github.com/gradle/gradle-plugin-rapidminer-${extension.id}"
					tags = ['rapidminer']
					description = extension.description

					// add all custom tags as well
					extension.tags.each { tag -> tags << tag }

					plugins {
						rapidminerPlugin {
							id = "com.rapidminer.${extension.id}"
							displayName = extension.displayName
						}
					}
				}

				// Needs to be done in afterEvaluate as release tasks aren't available before yet
				release {
					releaseRepositoryUrl = "${artifactory_contextUrl}/libs-release-local"
					snapshotRepositoryUrl= "${artifactory_contextUrl}/libs-snapshot-local"

					// Configure release tasks
					releaseTasks << build

					if(tasks.findByName('publishPluginPublicationToMavenRepository')){
						releaseTasks << tasks.publishPluginPublicationToMavenRepository
					} else {
						tasks.whenTaskAdded { Task task ->
							if(task.name == 'publishPluginPublicationToMavenRepository') {
								releaseTasks << tasks.publishPluginPublicationToMavenRepository
							}
						}
					}
				}
				
			}
		}
	}

}
