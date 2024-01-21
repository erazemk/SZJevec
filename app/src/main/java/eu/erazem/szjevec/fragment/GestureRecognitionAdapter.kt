package eu.erazem.szjevec.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.tasks.components.containers.Category
import eu.erazem.szjevec.databinding.RecognizedGestureBinding
import java.util.Locale
import kotlin.math.min

class GestureRecognitionAdapter :
    RecyclerView.Adapter<GestureRecognitionAdapter.ViewHolder>() {

    companion object {
        private const val NO_VALUE = "none"
    }

    private var adapterCategories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun updateResults(categories: List<Category>?) {
        if (categories.isNullOrEmpty()) {
            adapterCategories.clear()
            notifyDataSetChanged()
        } else {
            adapterCategories = MutableList(adapterSize) { null }
            val sortedCategories = categories.sortedByDescending { it.score() }
            val min = min(sortedCategories.size, adapterCategories.size)
            for (i in 0 until min) {
                adapterCategories[i] = sortedCategories[i]
            }
            adapterCategories.sortedBy { it?.index() }
            notifyDataSetChanged()
        }
    }

    fun updateAdapterSize(size: Int) {
        adapterSize = size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = RecognizedGestureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.root.visibility = View.VISIBLE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapterCategories[position].let { category ->
            holder.bind(category?.categoryName(), category?.score())
        }
    }

    override fun getItemCount(): Int = adapterCategories.size

    inner class ViewHolder(val binding: RecognizedGestureBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(label: String?, score: Float?) {
            with(binding) {
                if (label == null || label == "none") {
                    root.visibility = View.GONE
                } else {
                    root.visibility = View.VISIBLE
                    tvLabel.text = label.uppercase()
                    tvScore.text = if (score != null)
                        String.format(
                            Locale.US, "%.2f", score
                        ) else NO_VALUE
                }
            }
        }
    }
}
