package cn.mycommons.rconvert.core

/**
 * 名字装换规则
 */
interface IGenerateRule {

    fun gen(name: String): String
}