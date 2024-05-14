package cn.mycommons.rconvert.action.convert

import cn.mycommons.rconvert.action.ConvertKit
import cn.mycommons.rconvert.util.logger
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class KotlinConvert(project: Project) {

    private val logger = logger(this)

    private val psiFactory = KtPsiFactory(project)

    fun updateKotlinFile(
        psiFile: KtFile,
    ) {
        val set = mutableSetOf<KtDotQualifiedExpression>()
        object : KtTreeVisitorVoid() {

            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                val t = expression.text
                if (t.startsWith("R.")) {
                    if (t.split(".").count() >= 3) {
                        set.add(expression)
                    }
                } else if (t.contains(".R.")) {
                    val ss = t.split(".R.")
                    if (ss.size == 2 && ss[1].contains(".")) {
                        set.add(expression)
                    }
                }
                // logger.println("visitDotQualifiedExpression.element = ${expression}, ${expression.text}")
                super.visitDotQualifiedExpression(expression)
            }
        }.apply { psiFile.accept(this) }

        logger.info("KtDotQualifiedExpressions = ${set.size}")

        val filePackage = psiFile.packageFqName.asString()
        val rImportPackage = psiFile.importList?.imports
            ?.mapNotNull { it.importedFqName?.asString() }
            ?.filter { it.endsWith(".R") }
            ?.map { it.removeSuffix(".R") }
            ?.firstOrNull()

        val regex = Regex("[a-zA-Z0-9._]+")

        set.onEach {
            logger.info("KtDotQualifiedExpression: $psiFile -> ${it.text}")
        }.filter {
            it.text.matches(regex)
        }.filter {
            val r = !it.text.startsWith("android.R.")
            if (!r) {
                logger.info("skip ${it.text}")
            }
            r
        }.forEach {
//            if (it.text.startsWith("R.")) {
//                replaceExpression(it, "abc.R.string.abc")
//            } else if (it.text.contains(".R.")) {
//                replaceExpression(it, "abc.R.string.abc")
//            }

            val t = ConvertKit.findTarget(it.text, filePackage, rImportPackage)
            logger.println("findTarget: ${it.text} -> $t")

            if (t.first && t.second != null) {
                replaceExpression(it, t.second!!)
            }
        }
    }

    private fun replaceExpression(expression: KtDotQualifiedExpression, s: String) {
        val e2 = psiFactory.createExpression(s)
        expression.replace(e2)
        logger.println("replaceExpression = $expression, newExpression = $s")
    }
}
