package cn.mycommons.rconvert.action

data class ResDto(
    val name: String,
    val pkgList: List<String>,
) {

    fun findPkg(): String {
        return pkgList.first()
    }
}