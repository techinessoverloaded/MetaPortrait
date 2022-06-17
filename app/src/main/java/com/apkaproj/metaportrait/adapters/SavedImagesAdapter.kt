package com.apkaproj.metaportrait.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.apkaproj.metaportrait.models.Image
import com.apkaproj.metaportrait.databinding.ItemContainerSavedImageBinding
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.listeners.ImageSelectionListener
import com.apkaproj.metaportrait.listeners.SavedImagesListener
import com.apkaproj.metaportrait.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SavedImagesAdapter(private val savedImages: List<Image>,
                         private val savedImagesListener : SavedImagesListener,
                         private val context: Context,
                         private val imageSelectionListener: ImageSelectionListener):
RecyclerView.Adapter<SavedImagesAdapter.SavedImageViewHolder>()
{
    private val noOfSelectedImages: Int
        get() = selectedImages.size

    private val selectedImages: List<Image>
        get()
        {
            return savedImages.filter { image ->
                image.isChecked
            }
        }

    init
    {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedImageViewHolder
    {
        val binding = ItemContainerSavedImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return SavedImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedImageViewHolder, position: Int)
    {
        with(holder) {
            with(savedImages[position]) {
                binding.imageSaved.setImageBitmap(bitmap)
                if(isChecked)
                {
                    binding.imageSaved.setColorFilter(context.getColor(R.color.img_selector), PorterDuff.Mode.SRC_ATOP)
                }
                else
                {
                    binding.imageSaved.colorFilter = null
                }
                binding.root.setOnLongClickListener {
                    if(noOfSelectedImages == 0)
                    {
                        isChecked = true
                        binding.imageSaved.setColorFilter(context.getColor(R.color.img_selector), PorterDuff.Mode.SRC_ATOP)
                        imageSelectionListener.onImageSelected(true, noOfSelectedImages)
                        return@setOnLongClickListener true
                    }
                    return@setOnLongClickListener false
                }
                binding.root.setOnClickListener {
                    if(noOfSelectedImages > 0)
                    {
                        if(isChecked)
                        {
                            isChecked = false
                            binding.imageSaved.colorFilter = null
                            if(selectedImages.isEmpty())
                            {
                                imageSelectionListener.onImageSelected(false, 0)
                            }
                            else
                            {
                                imageSelectionListener.onImageSelected(false, noOfSelectedImages)
                            }
                        }
                        else
                        {
                            isChecked = true
                            binding.imageSaved.setColorFilter(
                                context.getColor(R.color.img_selector),
                                PorterDuff.Mode.SRC_ATOP
                            )
                            imageSelectionListener.onImageSelected(true, noOfSelectedImages)
                        }
                    }
                    else
                    {
                        savedImagesListener.onImageClicked(file)
                    }
                }
            }
        }
    }

    override fun getItemCount() = savedImages.size

    override fun getItemId(position: Int): Long = position.toLong()

    fun deleteSelectedImages()
    {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        var keyForDecryption: String? = null
        val fireStore = Firebase.firestore
        fireStore.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val userObject = snapshot.toObject(User::class.java)
                keyForDecryption = userObject?.tempKey
                if (userObject == null)
                {
                    Log.d("error","User Object is null")
                    return@addOnSuccessListener
                }
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
                return@addOnFailureListener
            }
        if(keyForDecryption == null)
        {
            Log.d("error","Decryption Key is null")
        }
        val storageRef = Firebase.storage.reference
        val imagesRef = storageRef.child("images")
        val userFolderRef = imagesRef.child(userId)
        if(noOfSelectedImages > 0)
        {
            selectedImages.forEach { image ->
                if(image.file.delete())
                {
                    (savedImages as ArrayList).remove(image)
                    val fileRef = userFolderRef.child(image.file.name)
                    fileRef.delete().addOnSuccessListener {
                        context.displayToast("Deleted Image ${image.file.name} from Cloud too successfully !")
                    }.addOnFailureListener {
                        context.displayToast("Failed to delete Image ${image.file.name} from Cloud !")
                    }
                    notifyDataSetChanged()
                }
                else
                {
                    Toast.makeText(context, "Failed to delete ${image.file.name} !", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun clearSelection()
    {
        savedImages.forEach { image ->
            if(image.isChecked)
                image.isChecked = false
        }
    }

    inner class SavedImageViewHolder(val binding: ItemContainerSavedImageBinding) : RecyclerView.ViewHolder(binding.root)
}
