package my.johnshaw21345.camera2study

import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.media.ThumbnailUtils
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mTakePhoto: Button? = null
    private var mRecordVideo: Button? = null
    private var mSurfaceView: SurfaceView? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mCamera: Camera? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var mIsRecording = false
    private var mEditFileName: EditText? = null
    private var mFlipButton: Button? = null
    private var mBackCamera = true
    private var mFileInfo: TextView? = null
    private var mThumbnail: ImageView? = null
    private var videoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mThumbnail = findViewById(R.id.thumbnail_view)
        mFileInfo = findViewById(R.id.file_info)
        mFlipButton = findViewById(R.id.btn_flip)
        mEditFileName = findViewById(R.id.edit_filename)
        mSurfaceView = findViewById(R.id.camera_view)
        mTakePhoto = findViewById(R.id.btn_shot)
        mRecordVideo = findViewById(R.id.btn_shot)
        startCamera()
        mTakePhoto?.setOnClickListener(View.OnClickListener { takePicture() })
        mRecordVideo?.setOnLongClickListener(View.OnLongClickListener {
            recordVideo()
        })

        mRecordVideo?.setOnTouchListener { v, event ->
            if (mIsRecording) {
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        recordVideo()
                    }
                }
            }
            return@setOnTouchListener false
        }

        mFlipButton?.setOnClickListener{
            if(mBackCamera){
                try{
                    mCamera!!.release()
                    mBackCamera = false
                    mCamera!!.setPreviewDisplay(mSurfaceView!!.holder)
                    mCamera!!.startPreview()
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }else{
                try{
                    mCamera!!.release()
                    mBackCamera = true
                    mCamera!!.setPreviewDisplay(mSurfaceView!!.holder)
                    mCamera!!.startPreview()
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }
        }

    }

    private fun startCamera() {
        try {
            if(mBackCamera){
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                setCameraDisplayOrientation()

            }else {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
                setCameraDisplayOrientation()

            }

        } catch (e: Exception) {
            // error
        }
        mSurfaceHolder = mSurfaceView!!.holder
        mSurfaceHolder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    /**
                     *   补充完整缺失代码 C1
                     */
                    mCamera!!.setPreviewDisplay(holder)
                    mCamera!!.startPreview()

                } catch (e: IOException) {
                    // error
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
                try {
                    mCamera!!.stopPreview()
                } catch (e: Exception) {
                    // error
                }
                try {
                    mCamera!!.setPreviewDisplay(holder)
                    mCamera!!.startPreview()
                } catch (e: Exception) {
                    //error
                }

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
    }

    private fun setCameraDisplayOrientation() {
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 90
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
        val result = (info.orientation - degrees + 360) % 360
        mCamera!!.setDisplayOrientation(result)
    }

    private fun takePicture() {
        mCamera!!.takePicture(null, null, Camera.PictureCallback { bytes, camera ->
            val pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE) ?: return@PictureCallback
            try {
                /**
                 *   补充完整缺失代码 C2
                 */
                val fos = FileOutputStream(pictureFile)
                fos.write(bytes)
                fos.close()
            } catch (e: FileNotFoundException) {
                //error
            } catch (e: IOException) {
                //error
            }
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = false
            bmOptions.inPurgeable = true
            val mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val bitmap = BitmapFactory.decodeFile(mediaStorageDir?.path + File.separator + pictureFile.name,bmOptions)
            mThumbnail!!.setImageBitmap(bitmap)
            mFileInfo?.text =   "File Type: Photo" + "\n" +
                    "File Name: " + pictureFile.name + "\n" +
                    "File Dir: " + mediaStorageDir?.path + File.separator + "\n" +
                    "File Size:" + bitmap.width + "*" + bitmap.height
            mCamera!!.startPreview()
        })
    }

    private fun getOutputMediaFile(type: Int): File? {
        // Android/data/com.bytedance.camera.demo/files/Pictures
        val mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!mediaStorageDir!!.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val mediaFile: File
        mediaFile = if (type == MEDIA_TYPE_IMAGE) {

            if (mEditFileName?.length() == 0) {
                File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
            }else{
                File(mediaStorageDir.path + File.separator + mEditFileName?.text + ".jpg")
            }


        } else if (type == MEDIA_TYPE_VIDEO) {
            if (mEditFileName?.length() == 0) {
                File(mediaStorageDir.path + File.separator + "VID_" + timeStamp + ".mp4")
            }else{
                File(mediaStorageDir.path + File.separator + mEditFileName?.text + ".mp4")
            }
        } else {
            return null
        }
        return mediaFile
    }

    private fun prepareVideoRecorder(): Boolean {
        mMediaRecorder = MediaRecorder()
        mCamera!!.unlock()
        mMediaRecorder!!.setCamera(mCamera)
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mMediaRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
        mMediaRecorder!!.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString())
        mMediaRecorder!!.setPreviewDisplay(mSurfaceHolder!!.surface)
        try {
            mMediaRecorder!!.prepare()
        } catch (e: IllegalStateException) {
            releaseMediaRecorder()
            return false
        } catch (e: IOException) {
            releaseMediaRecorder()
            return false
        }
        return true
    }

    private fun recordVideo(): Boolean {
        if (mIsRecording) {
            mMediaRecorder!!.stop()
            releaseMediaRecorder()
            mRecordVideo!!.text = ""

            Log.i("@->",videoFile!!.name)

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = false
            bmOptions.inPurgeable = true
            val mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(videoFile?.absolutePath)
            val bitmap = mmr.frameAtTime
            mThumbnail!!.setImageBitmap(bitmap)
            mFileInfo?.text =   "File Type: Video" + "\n" +
                                "File Name: " + videoFile?.name + "\n" +
                                "File Dir: " + mediaStorageDir?.path + File.separator + "\n" +
                                "File Size:" + bitmap?.width + "*" + bitmap?.height

            mCamera!!.startPreview()




            mCamera!!.lock()
            mIsRecording = false

        } else {
            videoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO)
            if (prepareVideoRecorder()) {
                mMediaRecorder!!.start()
                mIsRecording = true
                mRecordVideo!!.text = "Recording"
            } else {
                releaseMediaRecorder()
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaRecorder()
        releaseCamera()
    }

    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()
            mMediaRecorder!!.release()
            mMediaRecorder = null
            mCamera!!.lock()
        }
    }

    private fun releaseCamera() {
        if (mCamera != null) {
            mCamera!!.release()
            mCamera = null
        }
    }

    companion object {
        private const val MEDIA_TYPE_IMAGE = 1
        private const val MEDIA_TYPE_VIDEO = 2
    }
}