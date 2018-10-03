package com.bond.baseui.explorer

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bond.baseui.R
import com.bond.baseui.ui.BaseActivity
import com.bond.baseui.util.UIUtil
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_photolist.*
import java.util.*
import com.alexvasilkov.gestures.Settings
import com.bond.baseui.itemdecoration.SpaceItemDecoration
import com.bond.baseui.util.FileUtil
import com.bond.baseui.util.ToastUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.alexvasilkov.gestures.GestureController
import com.alexvasilkov.gestures.State
import com.bond.baseui.logger.Logger
import com.bond.baseui.util.toArrayList
import com.bumptech.glide.request.RequestOptions

/**
 * @author zhumingwei
 * @date 2018/7/4 下午2:53
 * @email zdf312192599@163.com
 */
class PickPhotosActivity : BaseActivity() {

    private lateinit var photoPagerAdapter: PhotoPagerAdapter

    private var currentIndex: Int = 0

    private var photoCounts: Int = 0

    private var spanCount: Int = 3
    private var maxPickSize: Int = 0
    private var pickMode: Int = PickConfig.MODE_SINGLE_PICK
    private var useCamera: Boolean = true
    private var toolbar_title: String = ""
    private var useOriginal: Boolean = false
    private var useCrop: Boolean = false
    private var crop_area: Int = PickConfig.CROP_AREA_1_1

    private lateinit var thumbPhotoAdapter: ThumbPhotoAdapter

    private var photoUri: Uri? = null // 照相
    lateinit var albumPopupWindow: AlbumPopupWindow

    lateinit var photo: Photo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPreView()
        initCropView()
        RxPermissions(this).request(Manifest.permission.READ_EXTERNAL_STORAGE).subscribe {
            if (it) {
                getData()
            }
        }
    }

    fun getData() {
        DataStoreHelper.getPhotoDirs(this, bundle, object : DataStoreHelper.PhotosResultCallback {
            override fun onResultCallback(directories: MutableList<PhotoDirectory>?) {
                directories?.let {
                    thumbPhotoAdapter.addData(it.get(0).photos?.toList() ?: listOf())
                    albumPopupWindow.addData(it)
                }

            }

        })
    }

    private fun initCropView() {
        cropping_image.controller.settings
                .setFitMethod(Settings.Fit.OUTSIDE)
                .setFillViewport(true)
                .setMaxZoom(8f)
                .setOverzoomFactor(2f)
                .setPanEnabled(true)
                .setRotationEnabled(false)
        cropping_finder.setSettings(cropping_image.controller.settings)
    }

    private fun initPreView() {
        photoPagerAdapter = PhotoPagerAdapter(this)
        viewpager.adapter = photoPagerAdapter
        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentIndex++
                toolbar.setTitle("${currentIndex} / ${photoCounts}")

            }

        })
        thumbPhotoAdapter.listener = object : ThumbPhotoAdapter.OnPhotoListener {
            override fun onPhotoClick(photos: MutableList<Photo>, selectedImages: MutableList<Photo>, position: Int) {
                if (pickMode == PickConfig.MODE_MULTIPLE_PICK) {
                    toolbar.setTitle("${title}(${selectedImages.size} / ${maxPickSize})")
                    if (photos.size > 0) {
                        setAction(menu_crop_submit_mode)
                    }
                } else if (useCrop) {
                    showCropImageView(photos.get(position))
                } else {
                    val intent = Intent()
                    intent.putParcelableArrayListExtra(PickConfig.EXTRA_PARCELABLE_ARRAYLIST, ArrayList<Photo>().apply {
                        add(Photo(path = photos[position].path))
                    })
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

            override fun onCameraResult(photoPath: String) {
                photo.path = photoPath
            }

            override fun onCameraClick(v: View) {
                RxPermissions(this@PickPhotosActivity).request(Manifest.permission.CAMERA).subscribe {
                    if (it) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//跳转到相机Activity
                        photoUri = FileUtil.getPhotoUri(this@PickPhotosActivity)
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        intent.clipData = ClipData.newUri(this@PickPhotosActivity.contentResolver,
                                PickConfig.PACKAGE, photoUri)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)//告诉相机拍摄完毕输出图片到指定的Uri
                        onCameraResult(photoUri.toString())
                        this@PickPhotosActivity.startActivityForResult(intent, PickConfig.CAMERA_REQUEST_CODE)
                    }
                }
            }

        }

        albumPopupWindow = AlbumPopupWindow(this)
        albumPopupWindow.anchorView = photo_footer
        albumPopupWindow.setOnItemClickListener(onItemClickListener)
        btn_category.text = "所有图片"
        photo_footer.setOnClickListener {
            albumPopupWindow.show()
        }

    }

    var onItemClickListener: AdapterView.OnItemClickListener = object : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            albumPopupWindow.setSelectedIndex(position)
            albumPopupWindow.listView.smoothScrollToPosition(position)
            var albumInfo: PhotoDirectory = albumPopupWindow.getItem(position)
            thumbPhotoAdapter.clearAdapter()
            thumbPhotoAdapter.addData(albumInfo.photos?.toList() ?: listOf())
            btn_category.text = albumInfo.name
            recyclerView.scrollToPosition(0)
            albumPopupWindow.dismiss()

        }
    }

    override fun initView() {
        toolbar.init("选择照片")
        toolbar.navigationIconClick {
            onBackPressed()
        }
        initData()
        recyclerView.layoutManager = GridLayoutManager(this, spanCount)
        recyclerView.addItemDecoration(SpaceItemDecoration(UIUtil.dip2px(this, 0.1f), spanCount))
        thumbPhotoAdapter = ThumbPhotoAdapter(this, spanCount, maxPickSize, pickMode)
        thumbPhotoAdapter.useCamera = useCamera
        recyclerView.adapter = thumbPhotoAdapter
        if (pickMode == PickConfig.MODE_SINGLE_PICK) {
            toolbar.setTitle(toolbar_title)
            checkbox_original.visibility = View.INVISIBLE
        } else {
            toolbar.setTitle("${toolbar_title}(0 / ${maxPickSize})")
        }
        checkbox_original.setOnCheckedChangeListener { buttonView, isChecked ->
            useOriginal = isChecked
        }
    }

    private lateinit var bundle: Bundle

    override fun initData() {
        bundle = intent.getBundleExtra(PickConfig.EXTRA_PICK_BUNDLE)
        spanCount = bundle.getInt(PickConfig.EXTRA_SPAN_COUNT, PickConfig.DEFAULT_SPAN_COUNT)
        pickMode = bundle.getInt(PickConfig.EXTRA_PICK_MODE, PickConfig.MODE_SINGLE_PICK)
        maxPickSize = bundle.getInt(PickConfig.EXTRA_MAX_SIZE, PickConfig.DEFAULT_PICK_SIZE)
        useCrop = bundle.getBoolean(PickConfig.EXTRA_USE_CROP)
        useCamera = bundle.getBoolean(PickConfig.EXTRA_USE_CAMERA)
        crop_area = bundle.getInt(PickConfig.EXTRA_CROP_AREA, PickConfig.CROP_AREA_1_1)
        toolbar_title = bundle.getString(PickConfig.EXTRA_TITLE, "选择图片")
        photo = Photo()
    }


    private var cropPath: String = ""
    fun showCropImageView(photo: Photo) {
        try {
            cropPath = photo.path
            Glide.with(this).asBitmap().load(cropPath).into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    var width = resource.width
                    var height = resource.height
                    if (width > 8500 || height > 8500) {
                        ToastUtil.show("暂不支持超大图片剪裁")
                    } else if (width / height > 4 || height / width > 4) {
                        ToastUtil.show("暂不支持超长图片剪裁")
                    } else {
                        cropping_image.setImageBitmap(resource)
                        cropping_image.visibility = View.VISIBLE
                        cropping_finder.visibility = View.VISIBLE
                        setAction(menu_crop_action_mode)
                        applyFinderShape(true)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyFinderShape(anim: Boolean) {
        cropping_finder.setRounded(false)
        var finderWidth = Math.min(UIUtil.getScreenwidth(this), UIUtil.getScreenHeight(this)) * 3 / 4
        var finderHeight = finderWidth
        when (crop_area) {
            PickConfig.CROP_AREA_16_9 -> {
                finderHeight = finderWidth * 9 / 12
            }
            PickConfig.CROP_AREA_4_3 -> {
                finderHeight = finderWidth * 3 / 4
            }
            PickConfig.CROP_AREA_1_1 -> {
                finderHeight = finderWidth
            }
            else -> {
                finderHeight = finderWidth
            }
        }
        var controller: GestureController = cropping_image.controller
        controller.settings.setMovementArea(finderWidth, finderHeight)
        if (anim) {
            var pivotX = controller.settings.viewportH / 2f
            var pivotY = controller.settings.viewportW / 2f
            var end: State = controller.state.copy()
            end.zoomTo(0.001f, pivotX, pivotY)
            controller.setPivot(pivotX, pivotY)
            controller.animateStateTo(end)
        } else {
            controller.updateState()
        }
        cropping_finder.update(anim)
    }

    override fun getContentViewRes(): Int {
        return R.layout.activity_photolist
    }

    override fun onBackPressed() {
        if (null != cropping_image && cropping_image.getVisibility() == View.VISIBLE) {
            hideCropImageView()
        } else if (null != viewpager && viewpager.visibility == View.VISIBLE) {
            hidePreView()
        } else {
            super.onBackPressed()
        }
    }

    private fun hideCropImageView() {
        cropping_image.setVisibility(View.GONE)
        cropping_finder.setVisibility(View.GONE)
        setAction(menu_empty_mode)
        toolbar.setTitle(currentIndex.toString() + "/" + photoCounts)
        hidePreView()
    }

    private fun hidePreView() {
        viewpager.visibility = View.INVISIBLE
        setAction(menu_empty_mode)
    }


    inner class PhotoPagerAdapter(val context: Context) : PagerAdapter() {
        var photos: MutableList<Photo> = mutableListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        fun clear() {
            photos.clear()
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return photos.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var imageView: ImageView = ImageView(context)
            container.addView(imageView)
            var uri: Uri = Uri.Builder().scheme("file").path(photos.get(position).path).build()
            Glide.with(context).setDefaultRequestOptions(RequestOptions())
                    .load(uri).thumbnail(0.3f).into(imageView)
            return imageView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }

    val menu_empty_mode = 0
    val menu_crop_mode = 1
    val menu_crop_action_mode = 2
    val menu_crop_submit_mode = 3
    var cropped: Bitmap? = null
    fun setAction(mode: Int) {
        when (mode) {
            menu_empty_mode -> {
                toolbar.setAction("")
            }
            menu_crop_mode -> {
                toolbar.setAction("剪裁", { showCropImageView(photoPagerAdapter.photos.get(viewpager.currentItem)) })
            }
            menu_crop_action_mode -> {
                toolbar.setAction("确定") {
                    cropped = cropping_image.crop()
                    try {
                        val file = FileUtil.saveBitmapToExternalCacheDir(this, cropped, cropPath, 75)//存放临时剪裁的文件
                        intent.putExtra(PickConfig.EXTRA_CORP_PATH, file.getAbsolutePath())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        intent.putExtra(PickConfig.EXTRA_CORP_PATH, "")
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            menu_crop_submit_mode -> {
                toolbar.setAction("使用") {
                    if (pickMode == PickConfig.MODE_SINGLE_PICK) {
                        var single: MutableList<String> = mutableListOf()
                        single.add(photoPagerAdapter.photos.get(viewpager.currentItem).path)
                        intent.putStringArrayListExtra(PickConfig.EXTRA_PARCELABLE_ARRAYLIST, single.toArrayList())
                    } else {
                        intent.putParcelableArrayListExtra(PickConfig.EXTRA_PARCELABLE_ARRAYLIST, thumbPhotoAdapter.getSelectedImages(useOriginal).toArrayList())
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        if (photoUri == null) {
            return;
        }
        if (requestCode == PickConfig.CAMERA_REQUEST_CODE) {
            Logger.d(photoUri!!.getPath())
            if (useCrop) {
                showCropImageView(photo)
            } else {
                val intent = Intent()
                val single = ArrayList<String>()
                single.add(photoUri!!.getPath())
                intent.putParcelableArrayListExtra(PickConfig.EXTRA_PARCELABLE_ARRAYLIST, ArrayList<Photo>().apply { Photo().apply { path = photoUri!!.getPath() } })
                intent.data = photoUri
                setResult(RESULT_OK, intent)
                finish()
            }

        }

    }

}


//========================================================================================Photo============================================================================================================


class ThumbPhotoAdapter(var context: Context, var spanCount: Int, var maxPickSize: Int, var pickMode: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val photos: MutableList<Photo> = mutableListOf()
    val selectedPhotos: MutableList<Photo> = mutableListOf()
    private var width = 0
    var listener: OnPhotoListener? = null
    var useCamera: Boolean = true


    companion object {
        val PHOTO_VIEW = 0
        val CAMERA_VIEW = 1

    }

    init {
        width = UIUtil.getScreenwidth(context) / spanCount
    }

    fun addData(photos: List<Photo>) {
        this.photos.addAll(photos)
        notifyDataSetChanged()
    }

    fun clearAdapter() {
        this.photos.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            CAMERA_VIEW -> return CameraHolder(ImageView(context))
            PHOTO_VIEW -> return ThumbHolder(ThumbPhotoView(context))
            else -> return ThumbHolder(ThumbPhotoView(context))
        }
    }

    override fun getItemCount(): Int {
        val total = this.photos.size
        return if (useCamera) total + 1 else total
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ThumbHolder) {
            val newPosition = if (useCamera) position - 1 else position
            (holder as ThumbHolder).setData(getItem(newPosition), newPosition)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && useCamera) {
            CAMERA_VIEW
        } else {
            PHOTO_VIEW
        }
    }

    fun getSelectedImages(useOriginal: Boolean): MutableList<Photo> {
        selectedPhotos.forEach {
            it.isOriginal = useOriginal
        }
        return selectedPhotos
    }

    fun getItem(position: Int): Photo {
        return this.photos.get(position)
    }

    inner class ThumbHolder(var thumbPhotoView: ThumbPhotoView?) : RecyclerView.ViewHolder(thumbPhotoView) {
        fun setData(imageInfo: Photo, position: Int) {
            thumbPhotoView?.layoutParams = FrameLayout.LayoutParams(width, width)
            thumbPhotoView?.loadData(imageInfo.path, pickMode)

            if (selectedPhotos.contains(imageInfo)) {
                thumbPhotoView?.showSelected(true)
            } else {
                thumbPhotoView?.showSelected(false)
            }

            thumbPhotoView?.setOnClickListener {
                if (pickMode == PickConfig.MODE_SINGLE_PICK) {
                    selectedPhotos.clear()
                    listener?.onPhotoClick(photos, selectedPhotos, position)
                    return@setOnClickListener
                }

                if (selectedPhotos.contains(imageInfo)) {
                    selectedPhotos.remove(imageInfo)
                    thumbPhotoView?.showSelected(false)
                } else {
                    if (selectedPhotos.size == maxPickSize) {
                        return@setOnClickListener
                    } else {
                        selectedPhotos.add(imageInfo)
                        thumbPhotoView?.showSelected(true)
                    }
                }
                listener?.onPhotoClick(photos, selectedPhotos, position)
            }
        }
    }

    inner class CameraHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView

        init {
            imageView = itemView as ImageView
            var params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(width, width)
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageResource(R.drawable.ic_pick_camera)
            imageView.layoutParams = params
            imageView.setOnClickListener {
                listener?.onCameraClick(it)
            }
        }
    }


    interface OnPhotoListener {
        fun onPhotoClick(photos: MutableList<Photo>, selectedImages: MutableList<Photo>, position: Int)

        fun onCameraResult(photoPath: String)

        fun onCameraClick(v: View)
    }


}

