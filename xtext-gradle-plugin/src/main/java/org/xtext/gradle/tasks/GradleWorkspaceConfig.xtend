package org.xtext.gradle.tasks

import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtext.util.UriUtil
import org.eclipse.xtext.workspace.IProjectConfig
import org.eclipse.xtext.workspace.ISourceFolder
import org.eclipse.xtext.workspace.IWorkspaceConfig
import org.gradle.api.Project

@Data
class GradleWorkspaceConfig implements IWorkspaceConfig {
	Project project

	override findProjectByName(String name) {
		if (name == projectConfig.name) {
			return projectConfig
		}
		return null
	}

	override findProjectContaining(URI member) {
		if (UriUtil.isPrefixOf(projectConfig.path, member)) {
			return projectConfig
		}
		return null
	}

	private def getProjectConfig() {
		new GradleProjectConfig(project)
	}

}

@Data
class GradleProjectConfig implements IProjectConfig {
	Project project

	override getName() {
		project.name
	}

	override getPath() {
		URI.createFileURI(project.projectDir.absolutePath)
	}

	override findSourceFolderContaining(URI member) {
		sourceFolders.findFirst[folder|UriUtil.isPrefixOf(folder.path, member)]
	}

	override getSourceFolders() {
		val sourceDirs = project.extensions.getByType(XtextExtension).sources.srcDirs
		sourceDirs.map [
			new GradleSourceFolder(project.relativePath(it), URI.createFileURI(absolutePath))
		].toSet
	}

}

@Data
class GradleSourceFolder implements ISourceFolder {
	String name
	URI path
}