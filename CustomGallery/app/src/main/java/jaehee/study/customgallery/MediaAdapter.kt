package jaehee.study.customgallery

import android.net.Uri

data class Media(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String,
)

class MediaAdapter( private val mediaList: List<Media>) {

}