## Introduction
The 'com.rapidminer.gradle-plugin' is a meta plugin which bundles configuration for all RapidMiner Gradle plugins.
It defines the correct source and target Java/Groovy compatibility, sets the plugin group to 'com.rapidminer.gradle' and configures the release and upload tasks.

## How to use (requires Gradle 2.1+)
	plugins {
		id 'com.rapidminer.gradle-plugin' version <plugin version>
	}
	
	gradlePlugin {
		
		/*
		 * The ID of the plugin which will be used as artifactId when being published to Artifactory/Bintray. 
		 */
		id 'gradle-plugin'
	} 
	
## Applied Plugins
- groovy
- maven-publish
- com.rapidminer.release
- com.rapidminer.code-quality
- com.gradle.plugin-publish

## Added Tasks
_Adds no tasks_
