package cn.mycommons.rconvert.action.convert

import cn.mycommons.rconvert.util.logger
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiReferenceExpression

class JavaConvert(
    project: Project,
) {

    private val logger = logger(this)

    private val psiFactory = PsiElementFactory.getInstance(project)

    fun updateJavaFile(
        psiFile: PsiJavaFile,
    ) {
        val set = mutableSetOf<PsiReferenceExpression>()
        object : JavaRecursiveElementVisitor() {

            override fun visitReferenceExpression(expression: PsiReferenceExpression) {
                val qn = expression.qualifiedName
                if (qn.startsWith("R.")) {
                    set.add(expression)
                } else if (qn.contains(".R.")) {
                    set.add(expression)
                } else {
                    super.visitReferenceExpression(expression)
                }
            }
        }.apply { psiFile.accept(this) }

        logger.info("PsiReferenceExpressions = ${set.size}")
        set.onEach {
            logger.info("PsiReferenceExpression: $psiFile -> ${it.qualifiedName}")
        }.filter {
            val r = !it.text.startsWith("android.R.")
            if (!r) {
                logger.info("skip ${it.text}")
            }
            r
        }.forEach {
            val qn = it.qualifiedName
            if (qn.startsWith("R.")) {
                replaceExpression(it, "abc.R.string.abc")
            } else if (qn.contains(".R.")) {
                replaceExpression(it, "abc.R.string.abc")
            }
        }
    }

    private fun replaceExpression(expression: PsiReferenceExpression, s: String) {
        val e2 = psiFactory.createReferenceFromText(s, null)
        expression.replace(e2)
        logger.println("replaceExpression = $expression, newExpression = $s")
    }
}
