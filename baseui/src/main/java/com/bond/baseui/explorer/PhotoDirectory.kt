package com.bond.baseui.explorer

/**
 *
 *   @author zhumingwei
 *   @date 2018/7/5 下午6:18
 *   @email zdf312192599@163.com
 */
data class PhotoDirectory(var id: String = "", var coverPath: String ="", var name: String ="", var dateAdded: Long =0, var photos: MutableList<Photo>? = mutableListOf()) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhotoDirectory) return false
        if (this.id != other.id) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    fun addPhoto(id: Int, path: String) {
        photos?.add(Photo(id, path))
    }

    fun getPhotoPaths(): List<String> {
        return mutableListOf<String>().apply {
            photos?.forEach {
                this.add(it.path)
            }
        }.toList()
    }
}