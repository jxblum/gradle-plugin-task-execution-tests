package example.gradle.plugin.echo

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

@SuppressWarnings("unused")
class EchoServerPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {

		EchoServerTask echoServerTask = project.tasks.create('echoServer', EchoServerTask) {
			classpath.from(project.sourceSets.main.runtimeClasspath)
		}

		project.tasks.findByName("test")?.configure { testTask ->
			testTask.dependsOn echoServerTask
			testTask.doFirst {
				systemProperties['example.app.echo.client.port'] = echoServerTask.port
			}
			testTask.doLast {
				println "Stopping Echo server listening on port [$echoServerTask.port]..."
				echoServerTask.process?.destroy()
			}
		}
	}

	static class EchoServerTask extends DefaultTask {

		@InputFiles
		final ConfigurableFileCollection classpath = project.objects.fileCollection()

		@Internal
		def echoServerClassName = "example.app.echo.server.EchoOne"

		@Internal
		def port = 0

		@Internal
		def process

		@TaskAction
		def run() {

			def processOutput = System.err

			this.port = findAvailablePort()
			println "Starting Echo server on port [$this.port]..."

			String classpath = classpath.join(File.pathSeparator)
			String javaHome = appendFileSeparator(init(System.getProperty("java.home"), System.getenv("JAVA_HOME")))
			String javaCommand = javaHome + "bin" + File.separator + "java"

			String[] commandLine = [
			    javaCommand, '-server', '-ea', '-classpath', classpath,
				"-Dexample.app.echo.server.port=$this.port",
				this.echoServerClassName
			]

			//println "Command-Line [$commandLine]"

			this.process = commandLine.execute()
			this.process.consumeProcessOutput(processOutput, processOutput)
		}
	}

	static def appendFileSeparator(value) {
		value.endsWith(File.separator) ? value : value.concat(File.separator)
	}

	static int findAvailablePort() {
		new ServerSocket(0).withCloseable { it.localPort }
	}

	static def init(value, defaultValue) {
		isSet(value) ? value : defaultValue
	}

	static def isSet(value) {
		!(value == null || value.isEmpty())
	}
}
