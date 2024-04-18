package cn.mycommons.rconvert.action

import cn.mycommons.rconvert.action.convert.RConvert
import cn.mycommons.rconvert.util.logger
import cn.mycommons.rconvert.core.FileType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile
import java.io.File


class NtrAction : AnAction() {

    val logger = logger(this)

    override fun update(event: AnActionEvent) {
        event.presentation.isVisible = true
    }

    override fun actionPerformed(event: AnActionEvent) {
        try {
            doAction(event)
        } catch (e: Exception) {
            e.printStackTrace()
            val project = event.getData(PlatformDataKeys.PROJECT)
            Messages.showMessageDialog(project, e.message, "Warning", Messages.getWarningIcon())
        }
    }

    private fun doAction(event: AnActionEvent) {
        var msg: String? = null
        do {
            val bd = event.project!!.baseDir
            val vf = event.getData(PlatformDataKeys.VIRTUAL_FILE) ?: break
            logger.info("doAction: vf = $vf")

            var files = if (vf.isDirectory) {
                File(vf.path).walkTopDown()
                    .filter { it.isFile }
                    .filter { it.extension == "kt" || it.extension == "java" }
                    .mapNotNull {
                        val r = it.toRelativeString(File(event.project!!.basePath))
                        bd.findFile(r)
                    }.toList()
            } else {
                listOf(vf)
            }

            files = files.filter { !it.path.contains("/build/") }

            logger.info("files = $files.size")
            files.forEach {
                logger.info("files: $it")
            }

            val list = files.mapNotNull {
                when (val psi = PsiManager.getInstance(event.project!!).findFile(it)) {
                    is PsiJavaFile -> {
                        Pair(psi, FileType.JavaFile)
                    }

                    is KtFile -> {
                        Pair(psi, FileType.KotlinFile)
                    }

                    else -> {
                        null
                    }
                }
            }

            logger.info("list = $list.size")

            if (list.isEmpty()) {
                msg = "is not java or kotlin file"
                break
            }

            val project = event.getData(PlatformDataKeys.PROJECT)

            if (project != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    RConvert(project, list).convert()
                }
            }
        } while (false)

        logger.info("msg = $msg")

        if (!msg.isNullOrEmpty()) {
            throw RuntimeException(msg)
        }
    }
}