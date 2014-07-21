package com.rapidminer.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

/**
 * 
 * @author nwoehler
 *
 */
class RapidMinerGradlePlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		PluginConfiguration extension = project.extensions.create('gradlePlugin', PluginConfiguration)
		
		project.configure(project) {
			apply plugin: 'groovy'
			apply plugin: 'rapidminer-publish'
			apply plugin: 'rapidminer-release'
			apply plugin: 'rapidminer-code-quality'

			sourceCompatibility = 1.7
			targetCompatibility = 1.7
			
			buildDir = 'target'
			
			group 'com.rapidminer.gradle'

			// define publish repositories and publications to upload
			uploadConfig {
				releaseRepo 'libs-release-local'
				snapshotRepo 'libs-snapshot-local'
				contextUrl "${artifactory_contextUrl}"
				publication 'mavenJava'
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
			}
			
			release {
				releaseTasks << artifactoryPublish
			}
			
			dependencies {
				compile gradleApi()
				compile localGroovy()
			}
		}
	}

}
