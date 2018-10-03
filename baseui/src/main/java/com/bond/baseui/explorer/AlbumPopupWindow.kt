package com.bond.baseui.explorer

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import com.bond.baseui.R
import com.bond.baseui.util.UIUtil
import com.bond.baseui.widget.BondImageView

/**
 * @author zhumingwei
 * @date 2018/7/5 下午6:12
 * @email zdf312192599@163.com
 * 选择相册
 */
class AlbumPopupWindow(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ListPopupWindow(context, attrs, defStyleAttr) {
    private var albumListAdapter: AlbumListAdapter

    init {
        albumListAdapter = AlbumListAdapter(context)
        setAdapter(albumListAdapter)
        albumListAdapter.changeSelect(0)
        var width = UIUtil.getScreenwidth(context)
        setContentWidth(width)
        height = width
        isModal = true
    }

    public fun addData(photoDirectories:List<PhotoDirectory> ){
        albumListAdapter.addData(photoDirectories)
    }

     fun getItem(position: Int): PhotoDirectory {
        return albumListAdapter.getItem(position)
    }

    fun setSelectedIndex(position: Int) {
        this.albumListAdapter.changeSelect(position)
    }

    override fun show() {
        if (isShowing)return
        super.show()
        setSelection(albumListAdapter.selected)
    }
}

class AlbumListAdapter(var context: Context) : BaseAdapter() {

    private var photoDirectories: MutableList<PhotoDirectory> = mutableListOf()
    var selected: Int = 0
    var inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(context)
    }

    fun addData(photoDirectories: List<PhotoDirectory>) {
        this.photoDirectories.addAll(photoDirectories)
        notifyDataSetChanged()
    }

    fun clearAdapter() {
        this.photoDirectories.clear()
        notifyDataSetChanged()
    }

    fun changeSelect(position: Int) {
        this.selected = position
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        var holder: ViewHolder = convertView?.let {
            view = it
            it.getTag() as ViewHolder
        } ?: kotlin.run {
            view = inflater.inflate(R.layout.item_pickphoto_album, parent, false)
            var vh = ViewHolder(view!!)
            view!!.setTag(vh)
            vh
        }

        val albumInfo = getItem(position)
        holder.photo_album_dis_name.text = albumInfo.name
        holder.photo_album_element_count.text = albumInfo.getPhotoPaths().size.toString()
        if (this.selected == position) {
            holder.photo_album_selected.visibility = View.VISIBLE
        } else {
            holder.photo_album_selected.visibility = View.GONE
        }

        if (TextUtils.isEmpty(albumInfo.coverPath)) {
            holder.photo_album_cover.setImageResource(R.drawable.default_error)
        } else {
            holder.photo_album_cover.override(200, 200).placeholder(R.drawable.default_error).load(albumInfo.coverPath)
        }

        return view!!

    }

    override fun getItem(position: Int): PhotoDirectory {
        return photoDirectories.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return photoDirectories.size
    }

}

class ViewHolder(var view: View) {
    var photo_album_cover: BondImageView

    var photo_album_selected: ImageView

    var photo_album_dis_name: TextView

    var photo_album_element_count: TextView

    init {
        photo_album_cover = view.findViewById(R.id.photo_album_cover)
        photo_album_selected = view.findViewById(R.id.photo_album_selected)
        photo_album_dis_name = view.findViewById(R.id.photo_album_dis_name)
        photo_album_element_count = view.findViewById(R.id.photo_album_element_count)
    }
}
