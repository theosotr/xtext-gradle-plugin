package org.xtext.gradle.tasks;

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class XtextGenerate extends DefaultTask {

	private XtextExtension xtext

	@InputFiles
	FileCollection xtextClasspath

	@InputFiles
	FileCollection classpath

	def configure(XtextExtension xtext) {
		this.xtext = xtext
		xtext.languages.each {Language lang ->
			lang.outputs.each {  OutputConfiguration output ->
				outputs.dir(output.dir)
			}
		}
		inputs.source(xtext.sources)
	}

	@TaskAction
	def generate() {
		def command = [
			"java",
			"-cp",
			getXtextClasspath().asPath,
			"org.xtext.builder.standalone.Main",
			"-encoding",
			xtext.getEncoding(),
			"-cwd",
			project.getProjectDir().absolutePath,
			"-classpath",
			getClasspath().asPath,
			"-tempdir",
			new File(project.buildDir, "xtext-temp").absolutePath
			
		]

		xtext.languages.each {Language language ->
			command += [
				"-L${language.name}.setup=${language.setup}",
				"-L${language.name}.javaSupport=${language.consumesJava}"
			]
			language.outputs.each {OutputConfiguration output ->
				command += [
					"-L${language.name}.${output.name}.dir=${output.dir}",
					"-L${language.name}.${output.name}.createDir=true"
				]
			}
		}
		command += [
			* xtext.sources.srcDirs*.absolutePath
		]

		def pb = new ProcessBuilder(command as String[])
		pb.redirectErrorStream(true)
		def process = pb.start()
		def input = new BufferedReader(new InputStreamReader(process.getInputStream()))
		def line
		while ((line = input.readLine()) != null) {
			println(line)
		}
		def exitCode = process.waitFor()
		if (exitCode != 0) {
			throw new GradleException("Xtext failed");
		}
	}
}
