package jaehee.study.customgallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jaehee.study.customgallery.databinding.ItemImageBinding

class ImageAdapter(
    private val images: List<Uri>, // 이미지 URI 리스트
    private val onImageSelected: (Uri) -> Unit // 이미지 선택 콜백

) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    private val selectedImages = mutableSetOf<Uri>() // 선택된 이미지 URI 저장
    private lateinit var binding: ItemImageBinding

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = binding.imageView


        fun bind(uri: Uri) {
            // Glide를 사용하여 이미지 로드
            Glide.with(itemView.context)
                .load(uri)
                .into(imageView)

            // 선택 상태에 따라 알파 값 조정 (0.5 = 어두움)
            imageView.alpha = if (selectedImages.contains(uri)) {
                0.5f // 선택된 경우, 이미지가 어둡게 보이도록 설정
            } else {
                1.0f // 선택되지 않은 경우, 기본 밝기로 설정
            }

            itemView.setOnClickListener {
                // 이미지 선택 토글
                if (selectedImages.contains(uri)) {
                    selectedImages.remove(uri) // 선택 해제
                } else {
                    selectedImages.add(uri) // 선택
                }
                onImageSelected(uri) // 선택 상태 변경 콜백 호출
                notifyItemChanged(adapterPosition) // 해당 아이템만 갱신
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context))
        return ImageViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        holder.bind(images[position]) // 뷰 홀더에 이미지 바인딩
    }

    override fun getItemCount(): Int {
        return images.size // 이미지 개수 반환
    }
}
