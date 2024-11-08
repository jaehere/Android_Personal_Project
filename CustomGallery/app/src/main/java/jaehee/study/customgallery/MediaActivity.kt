package jaehee.study.customgallery

import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import jaehee.study.customgallery.databinding.ActivityMediaBinding
import kotlinx.coroutines.launch

class MediaActivity: AppCompatActivity() {
    private lateinit var adapter: ImageAdapter
    private val selectedImages = mutableListOf<Uri>()
    private val allowedImageList = mutableListOf<Uri>()
    companion object{
        private const val SELECTED_IMAGES = "selectedImages"
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

    private lateinit var binding: ActivityMediaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        lifecycleScope.launch {
            requestPermissionLauncher.launch(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VISUAL_USER_SELECTED,
                )
            )
        }

        lifecycleScope.launch {
            binding.submit.setOnClickListener{
                submitSelectedImages()
            }
        }
    }

    private fun submitSelectedImages() {

        if(selectedImages.size == 0){
            Toast.makeText(this, R.string.msg_no_images, Toast.LENGTH_SHORT).show()

        } else {
            // 선택된 이미지 URI를 Intent에 담아 반환
            val intent = Intent()
            intent.putExtra(SELECTED_IMAGES, selectedImages.map {
                it.toString()
            }.toTypedArray())

            setResult(RESULT_OK, intent)
            finish() // 현재 액티비티 종료
        }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                loadAllowedImages() // 권한이 모두 허용된 경우
            }
            else -> {
                Toast.makeText(this, R.string.need_permission, Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun loadAllowedImages() {

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
        )
        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Query all the device storage volumes instead of the primary only
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val images = mutableListOf<Media>()

        contentResolver.query(
            collectionUri as Uri,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val uri = ContentUris.withAppendedId(collectionUri, cursor.getLong(idColumn))
                val name = cursor.getString(displayNameColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                val image = Media(uri, name, size, mimeType)
                images.add(image)

                // 허용된 이미지인지 확인
//                if (isImageAllowed(uri.toString())) {
                allowedImageList.add(uri)
//                }
            }
        }

        if (allowedImageList.isEmpty()) {
            Toast.makeText(this, "허용된 이미지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            adapter = ImageAdapter(allowedImageList) { selectedImageUri ->
                if (!selectedImages.contains(selectedImageUri)) {
                    selectedImages.add(selectedImageUri) // 선택한 이미지 URI 추가
                } else {
                    selectedImages.remove(selectedImageUri) // 다시 선택하면 제거
                }
            }
            binding.recyclerView.adapter = adapter
        }
    }



    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val newPermissionGranted = permissions["android.permission.READ_MEDIA_VISUAL_USER_SELECTED"] == true
        val permissionGranted = permissions["android.permission.READ_MEDIA_IMAGES"] == true
        if (newPermissionGranted && !permissionGranted) {
            lifecycleScope.launch {
                loadAllowedImages()
            }
        } else {
            lifecycleScope.launch {
                loadAllowedImages()
            }
        }

    }
}
