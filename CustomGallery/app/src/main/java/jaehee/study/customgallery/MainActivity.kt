package jaehee.study.customgallery

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import jaehee.study.customgallery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnGoToGallery.setOnClickListener {
            goToCustomGallery()
        }

    }

    private fun goToCustomGallery() {
        val intent = Intent(this, MediaActivity::class.java).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        resultLauncher.launch(intent)
    }

    private val resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data != null) {
            try {
                val resultImage =
                    result.data?.getStringArrayExtra("selectedImages")?.get(0).toString()
                val intent: Intent? = Intent.parseUri(resultImage, Intent.URI_INTENT_SCHEME)
                val uri = Uri.parse(intent?.dataString)

                val iStream = contentResolver.openInputStream(uri!!)

                val options = BitmapFactory.Options()

                // 1. inJustDecodeBounds = true 설정으로 이미지 크기만 가져오기
                BitmapFactory.decodeStream(
                    iStream,
                    null,
                    options
                )
//                iStream?.close()

                var width = options.outWidth
                var height = options.outHeight
                var samplesize = 1

                while (true) {
                    if (width / 2 < 1000 || height / 2 < 1000) break
                    width /= 2
                    height /= 2
                    samplesize *= 2
                }
                options.inSampleSize = samplesize

                // 2. inJustDecodeBounds = false 설정으로 실제 이미지 디코딩
//                options.inJustDecodeBounds = false
                val bitmap = BitmapFactory.decodeStream(
                    contentResolver.openInputStream(uri),
                    null,
                    options
                )
                // 스트림을 다시 닫음
//                iStream?.close()

            } catch (e: Exception) {
                Log.e("e", "nav error: ${e.message}")
            }
        } else {
            finish()
        }
    }
}