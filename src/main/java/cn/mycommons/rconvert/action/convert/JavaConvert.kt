package cn.mycommons.rconvert.action.convert

import cn.mycommons.rconvert.action.ConvertKit
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

        val filePackage = psiFile.packageName
        val rImportPackage = psiFile.importList?.importStatements
            ?.mapNotNull { it.qualifiedName }
            ?.filter { it.endsWith(".R") }
            ?.map { it.removeSuffix(".R") }
            ?.firstOrNull()


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
//            if (qn.startsWith("R.")) {
//                replaceExpression(it, "abc.R.string.abc")
//            } else if (qn.contains(".R.")) {
//                replaceExpression(it, "abc.R.string.abc")
//            }

            val t = ConvertKit.findTarget(qn, filePackage, rImportPackage)
            logger.println("findTarget: $qn -> $t")

            if (t.first && t.second != null) {
                replaceExpression(it, t.second!!)
            }
        }
    }

    private fun replaceExpression(expression: PsiReferenceExpression, s: String) {
        val e2 = psiFactory.createReferenceFromText(s, null)
        expression.replace(e2)
        logger.println("replaceExpression = $expression, newExpression = $s")
    }
}
