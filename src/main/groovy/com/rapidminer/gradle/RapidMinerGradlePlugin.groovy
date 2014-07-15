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

			sourceCompatibility = 1.7
			targetCompatibility = 1.7
			
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
						artifactId extension.id
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
