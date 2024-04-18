package cn.mycommons.rconvert.action.convert

import cn.mycommons.rconvert.util.logger
import cn.mycommons.rconvert.core.FileType
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile
import java.time.Duration
import java.time.LocalDateTime

class RConvert(
    private val project: Project,
    private val list: List<Pair<PsiFile, FileType>>,
) {

    private val logger = logger(this)

    fun convert() {
        val begin = LocalDateTime.now()
        showNotification("Convert begin, size = ${list.size}")

        doConvert()

        val time = Duration.between(begin, LocalDateTime.now())
        showNotification("Convert finish, size = ${list.size}, time = $time")
    }

    private fun doConvert() {
        list.forEachIndexed { idx, it ->
            logger.info("convertFile: ${idx + 1}/${list.size}, ${it.first.virtualFile.path}, type = ${it.second}")

            kotlin.runCatching {
                when (it.second) {
                    FileType.JavaFile -> {
                        JavaConvert(project).updateJavaFile(it.first as PsiJavaFile)
                    }

                    FileType.KotlinFile -> {
                        KotlinConvert(project).updateKotlinFile(it.first as KtFile)
                    }
                }
            }.onSuccess { r ->
                logger.info("convertFile: ${idx + 1}/${list.size}, ${it.first.virtualFile.path}, type = ${it.second} success")
            }.onFailure { e ->
                logger.error("convertFile: ${idx + 1}/${list.size}, ${it.first.virtualFile.path}, type = ${it.second} fail", e)
            }
        }
        logger.info("convertFile finish: list.size = ${list.size}")
    }

    private fun showNotification(s: String) {
        // 创建一个通知对象
        val notification = Notification(
            "RClass",
            "Convert Notice",
            s,
            NotificationType.INFORMATION
        )

        // 显示通知
        Notifications.Bus.notify(notification, project)
    }
}