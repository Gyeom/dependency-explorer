package com.github.gyeom.dependencyexplorer

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import java.awt.Desktop
import java.io.File
import java.net.URI

class OpenDependencyAction : AnAction("Open in Maven Repository") {

    companion object {
        private val DEPENDENCY_REGEX = Regex(
            """([a-zA-Z0-9_]+)\("([a-zA-Z0-9_.-]+):([a-zA-Z0-9_.-]+)(?::([a-zA-Z0-9_.-]+|\$\{?[a-zA-Z0-9_]+}?)?)?"\)"""
        )
        private val GRADLE_DSL_REGEX = Regex(
            """([a-zA-Z0-9_]+)\s*['"]([a-zA-Z0-9_.-]+):([a-zA-Z0-9_.-]+):([a-zA-Z0-9_.-]+)['"]"""
        )
        private val GRADLE_DSL_NAMED_REGEX = Regex(
            """([a-zA-Z0-9_]+)\s*group:\s*['"]([a-zA-Z0-9_.-]+)['"],\s*name:\s*['"]([a-zA-Z0-9_.-]+)['"],\s*version:\s*['"]([a-zA-Z0-9_.-]+)['"]"""
        )
        private const val MAVEN_BASE_URL = "https://mvnrepository.com/artifact"
    }

    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val project = event.project

        if (editor == null || project == null) {
            showErrorDialog("No editor or project found.")
            return
        }

        val selectedLine = getSelectedLineText(editor)

        val variables = resolveVariableValues(project.basePath ?: "")

        val dependency = parseDependency(selectedLine, variables)
        if (dependency == null) {
            showErrorDialog("No valid dependency found on the selected line.")
            return
        }

        openInBrowser(buildMavenUrl(dependency))
    }

    override fun update(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)

        event.presentation.isEnabledAndVisible = false

        if (editor != null && (virtualFile?.name?.endsWith(".gradle") == true || virtualFile?.name?.endsWith(".gradle.kts") == true)) {
            val selectedLine = getSelectedLineText(editor)
            if (DEPENDENCY_REGEX.containsMatchIn(selectedLine) ||
                GRADLE_DSL_REGEX.containsMatchIn(selectedLine) ||
                GRADLE_DSL_NAMED_REGEX.containsMatchIn(selectedLine)
            ) {
                event.presentation.isEnabledAndVisible = true
            }
        }
    }

    private fun showErrorDialog(message: String) {
        Messages.showErrorDialog(message, "Error")
    }

    private fun getSelectedLineText(editor: Editor): String {
        val document = editor.document
        val caret = editor.caretModel.currentCaret
        val lineStart = document.getLineStartOffset(caret.logicalPosition.line)
        val lineEnd = document.getLineEndOffset(caret.logicalPosition.line)
        return document.text.substring(lineStart, lineEnd).trim()
    }

    private fun parseDependency(lineText: String, variables: Map<String, String>): Dependency? {
        // Check standard dependency format
        val matchResult = DEPENDENCY_REGEX.find(lineText)
        if (matchResult != null) {
            val (_, group, artifact, version) = matchResult.destructured
            val resolvedVersion = resolveVersion(version, variables)
            return Dependency(group, artifact, resolvedVersion.ifEmpty { null })
        }

        // Check Gradle DSL short format
        val dslMatch = GRADLE_DSL_REGEX.find(lineText)
        if (dslMatch != null) {
            val (_, group, artifact, version) = dslMatch.destructured
            return Dependency(group, artifact, version)
        }

        // Check Gradle DSL named format
        val namedMatch = GRADLE_DSL_NAMED_REGEX.find(lineText)
        if (namedMatch != null) {
            val (_, group, artifact, version) = namedMatch.destructured
            return Dependency(group, artifact, version)
        }

        return null
    }

    private fun resolveVersion(version: String, variables: Map<String, String>): String {
        return version.replace("""\$\{([a-zA-Z0-9_]+)}""".toRegex()) { match ->
            variables[match.groupValues[1]] ?: match.value
        }.replace("""\$([a-zA-Z0-9_]+)""".toRegex()) { match ->
            variables[match.groupValues[1]] ?: match.value
        }
    }

    private fun resolveVariableValues(projectPath: String): Map<String, String> {
        val variables = mutableMapOf<String, String>()
        try {
            val process = ProcessBuilder(
                "${projectPath}/gradlew",
                "-q",
                "properties"
            ).directory(File(projectPath))
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            val regex = Regex("""(\w+):\s+(.*)""")
            regex.findAll(output).forEach { matchResult ->
                val key = matchResult.groupValues[1]
                val value = matchResult.groupValues[2].trim()
                variables[key] = value
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return variables
    }

    private fun buildMavenUrl(dependency: Dependency): String {
        return if (dependency.version != null) {
            "$MAVEN_BASE_URL/${dependency.group}/${dependency.artifact}/${dependency.version}"
        } else {
            "$MAVEN_BASE_URL/${dependency.group}/${dependency.artifact}"
        }
    }

    private fun openInBrowser(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: Exception) {
            showErrorDialog("Failed to open browser: ${e.message}")
        }
    }

    data class Dependency(
        val group: String,
        val artifact: String,
        val version: String?
    )
}