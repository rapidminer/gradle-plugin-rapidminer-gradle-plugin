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
				if(project.hasProperty('nexusUser') &&
					project.hasProperty('nexusPassword')) {
					logger.info 'Found Nexus properties. Applying remote repository publishing configuration.'
					repositories {
						maven {
							url "https://maven.rapidminer.com/content/repositories/${->project.version.contains('-SNAPSHOT') ?  'snapshots' : 'releases-public'}"
							credentials {
								username = "${nexusUser}"
								password = "${nexusPassword}"
							}
						}
					}
				} else {
					logger.info 'Not applying remote repository publishing configuration as Nexus repository is not configured properly.'
				}
			}

			dependencies {
				compile gradleApi()
				compile localGroovy()

				testCompile('com.netflix.nebula:nebula-test:3.1.0') {
					exclude group: 'org.codehaus.groovy'
				}
			}

			// All code below needs to be executed in afterEvaluate as we need access to the configured 'gradlePlugin' extension
			afterEvaluate {
				pluginBundle {
					website = 'https://www.rapidminer.com'
					vcsUrl = "https://github.com/gradle/gradle-plugin-rapidminer-${extension.id}"
					tags = ['rapidminer']
					description = readChangesDescription(project)

					// add all custom tags as well
					extension.tags.each { tag -> tags << tag }

					plugins {
						rapidminerPlugin {
							id = "com.rapidminer.${extension.id}"
							displayName = "com.rapidminer.${extension.id}"
						}
					}
				}

				// Needs to be done in afterEvaluate as release tasks aren't available before yet
				release {
					releaseRepositoryUrl = "https://maven.rapidminer.com/content/repositories/releases-public"
					snapshotRepositoryUrl= "https://maven.rapidminer.com/content/repositories/snapshots"

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

	def String readChangesDescription(project){
		File changesFile = project.file('CHANGES.md')
		if(!changesFile.exists()){
			project.logger.info 'Changes file does not exists. Returning empty changes description.'
			return '-'
		}

		// Load changes text and find latest version changes start
		String changesText = changesFile.text
		int firstVersionIndex = changesText.indexOf('####')
		if(firstVersionIndex == -1){
			project.logger.info 'Could not find latest changes definition starting with "####". ' +
					'Returning empty changes description.'
			return '-'
		} else {
			firstVersionIndex += 4
		}

		// find last release version changes start
		int nextVersionIndex = changesText.substring(firstVersionIndex).indexOf('####')
		if(nextVersionIndex == -1){
			project.logger.info 'Could not find changes definition of last release version starting with' +
					' "####". Returning empty changes description.'
			return '-'
		}

		// Cut full changes text to latest version changes and extract changes entries
		String latestVersionChangesText = changesText.substring(firstVersionIndex, firstVersionIndex + nextVersionIndex).trim()
		int firstChangesEntryIndex = latestVersionChangesText.indexOf('*')
		if(firstChangesEntryIndex == -1){
			project.logger.info 'Could not find first changes entry starting with "*". Returning empty changes description.'
			return '-'
		} else {
			return latestVersionChangesText.substring(firstChangesEntryIndex)
		}

	}

}
