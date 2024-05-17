package cn.mycommons.rconvert.action

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object ConvertKit {

    const val dir = "/Users/ios-builder2/workspace/java-project/r-class-demo/r-ws-all"

    val cache: MutableMap<String, Map<String, List<String>>> = mutableMapOf()
    val gson = Gson()

    init {
        if (cache.isEmpty()) {
            File(dir).listFiles()
                ?.filter { it.name.endsWith(".json") }
                ?.forEach { f ->
                    val s = f.readText()
                    val t = object : TypeToken<Map<String, List<String>>>() {}.type
                    val m: Map<String, List<String>> = gson.fromJson(s, t)
                    val type = f.name.replace(".json", "")
                    cache[type] = m.entries.associate {
                        it.key to it.value
                    }
                }
        }
    }

    fun findTarget(qn: String, filePackage: String? = null, rImportPackage: String? = null): Pair<Boolean, String?> {
        // val qn = it.qualifiedName
        val pkg = if (qn.startsWith("R.")) {
            ""
        } else if (qn.contains(".R.")) {
            val idx = qn.indexOf("R.")
            qn.substring(0, idx - 1)
        } else {
            ""
        }
        // nb 的导入，不修改
        if (pkg.contains("android.") || pkg.contains("androidx.") || pkg.contains(".google.")) {
            return false to qn
        }

        val ss = qn.split(".").takeLast(2)
        val type = ss.first()
        val name = ss.last()

        // println("pkg: $pkg")

        val map = cache[type] ?: mapOf()

        if (map.contains(name)) {
            val pkgList = map[name]!!
            // package相同，则不修改
            if (pkgList.contains(pkg)) {
                return false to qn
            }
            // 在当前package下，则不导入
            if (filePackage != null && pkgList.contains(filePackage)) {
                return false to filePackage
            }

            // 在当前import下，则不导入
            if (rImportPackage != null && pkgList.contains(rImportPackage)) {
                return false to filePackage
            }

            //  TODO XIAQIULEI 先选择第一个
            val pkg2 = pkgList.first()

            // 找到了，需要修改
            if (qn.startsWith("R.")) {
                return true to "$pkg2.$qn"
            }

            return true to pkg2 + ".R.${type}.${name}"

        }
        // 没有找到，不修改
        return false to null
    }
}