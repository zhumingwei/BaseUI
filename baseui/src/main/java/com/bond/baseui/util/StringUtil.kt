package com.bond.baseui.util

fun String.isPwd():Boolean {
    val reg = "^[A-Za-z\\\\d]{4,}$"
    //密码至少八个字符，至少一个字母和一个数字
//    val reg=  "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"
    return this.matches(reg.toRegex())
}

fun String.isPhoneNum(): Boolean {
    val PHONE_NUMBER_REG = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$"
    return this.matches(PHONE_NUMBER_REG.toRegex())
}

fun String.isMsgCode(): Boolean {
    return this.matches(Regex("^\\d{6}$"))
}

fun <T> Iterable<T>.toArrayList(): ArrayList<T> {
    var list = ArrayList<T>()
    this.forEach {
        list.add(it)
    }
    return list
}