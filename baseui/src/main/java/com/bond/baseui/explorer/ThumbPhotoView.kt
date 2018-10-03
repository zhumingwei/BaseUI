package com.bond.baseui.explorer

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.bond.baseui.R
import kotlinx.android.synthetic.main.item_pickphoto_view.view.*

/**
 * @author zhumingwei
 * @date 2018/7/5 下午1:42
 * @email zdf312192599@163.com
 */
class ThumbPhotoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {


    internal var mask: View


    init {
        val view = View.inflate(context, R.layout.item_pickphoto_view, this)
        mask = view.findViewById(R.id.mask)
    }


    fun loadData(folderPath: String, pickMode: Int) {
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        photo_thumbview.setLayoutParams(params)
        val uri = Uri.Builder().scheme("file").path(folderPath).build()

        photo_thumbview
                .override(200, 200)
                .load(uri)


    }


    fun showSelected(showSelected: Boolean) {
        if (showSelected) {
            photo_thumbview_selected.visibility = View.VISIBLE
            mask.visibility = View.VISIBLE
        } else {
            photo_thumbview_selected.visibility = View.INVISIBLE
            mask.visibility = View.INVISIBLE
        }
    }

    fun toggleSelect(queuePosition: Int) {
        // Logger.d("queuePosition:"+queuePosition);
        if (queuePosition == 0) {
            photo_thumbview_position.text = ""
            photo_thumbview_position.visibility = View.GONE
        } else {
            photo_thumbview_position.visibility = View.VISIBLE
            photo_thumbview_position.text = queuePosition.toString()
        }
    }
}
