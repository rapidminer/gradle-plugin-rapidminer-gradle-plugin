## Introduction
The 'rapidminer-gradle-plugin' is a meta plugin which bundles configuration for all RapidMiner Gradle plugins.
It defines the correct source and target compatibility, sets the plugin group to 'com.rapidminer.gradle' and configures the release and upload tasks.

## How to use
	buildscript {
		dependencies {
			classpath 'com.rapidminer.gradle:gradle-plugin:$VERSION'
		}
	}
	
	apply plugin: 'com.rapidminer.gradle.gradle-plugin'
	
	gradlePlugin {
		
		/*
		 * The ID of the plugin which will be used as artifactId when being published to Artifactory. 
		 */
		id 'gradle-plugin'
	} 
	
## Applied Plugins
- groovy
- rapidminer-release 
- rapidminer-code-quality
 - maven-publish

## Added Tasks
_Adds no tasks_