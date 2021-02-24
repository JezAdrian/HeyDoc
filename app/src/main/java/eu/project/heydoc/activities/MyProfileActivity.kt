package eu.project.heydoc.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import eu.project.heydoc.R
import eu.project.heydoc.firestore.FireStoreClass
import eu.project.heydoc.models.User
import eu.project.heydoc.utils.Constants
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.HashMap


class MyProfileActivity : BaseActivity() {


    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageUri : String = ""
    private lateinit var mUserDetails: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FireStoreClass().loadUserData(this@MyProfileActivity)


        iv_profile_user_image.setOnClickListener {


            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle(resources.getString(R.string.action))
            val pictureDialogItems = arrayOf(resources.getString(R.string.gallery),resources.getString(R.string.camera))
            pictureDialog.setItems(pictureDialogItems) { _, which ->
                when (which) {
                    0 -> choosePhotoFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
        }
        btn_update.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_my_profile_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile)
        }

        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }


    /**
     * A function to set the existing details in UI.
     */
    fun setUserDataInUI(user: User) {
        mUserDetails = user


        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_myProfile_name.setText(user.name)
        et_myProfile_email.setText(user.email)
        if (user.mobile != 0L) {
            et_myProfile_mobile.setText(user.mobile.toString())
        }
    }

    /**
     * A function to make a Downloadable Image URL which we can store in FireStore
     */
    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if(mSelectedImageFileUri != null){
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE_" + System.currentTimeMillis()+ "." + getFileExtension(mSelectedImageFileUri))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                Log.i("FireBase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString() )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    mProfileImageUri = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener{
                    exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }

    fun updateUserProfileData(){
        var userHashMap = HashMap<String, Any>()
        var anyChangesMade = false
        if(mProfileImageUri.isEmpty() || mProfileImageUri != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageUri
            anyChangesMade = true
        }
        if(et_myProfile_name.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = et_myProfile_name.text.toString()
            anyChangesMade = true
        }
        if(et_myProfile_mobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = et_myProfile_mobile.text.toString().toLong()
            anyChangesMade = true
        }
        if (anyChangesMade ){
            FireStoreClass().updateUserProfileData(this,userHashMap)
            hideProgressDialog()
        }
    }


    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, PICK_CAMERA_IMAGE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()

    }

    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    /**
     * Overriding a function to choose a picture form camera or gallery to our profilepicture
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if (requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null
            ) {
                // The uri of selection image from phone storage.
                mSelectedImageFileUri = data.data

                try {
                    Glide
                        .with(this@MyProfileActivity)
                        .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                        .centerCrop() // Scale type of the image.
                        .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                        .into(iv_profile_user_image) // the view in which the image will be loaded.
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }else if(requestCode == PICK_CAMERA_IMAGE ){
                val thumbNail : Bitmap = data!!.extras!!.get("data") as Bitmap
                mSelectedImageFileUri = saveImageToInternalStorage(thumbNail)
                Log.e("cameraFile", "$mSelectedImageFileUri")

                try {
                    Glide
                        .with(this@MyProfileActivity)
                        .asBitmap()
                        .load(thumbNail)
                        .centerCrop() // Scale type of the image.
                        .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                        .into(iv_profile_user_image) // the view in which the image will be loaded.
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * A function to convert a BitMap to JPG a return his URI
     */
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 ,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse("file://" + file.absolutePath)
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("It's look like you have turned off permissions required.").setPositiveButton("GO TO SETTINGS"){
                _,_ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",packageName,null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){ dialog, _ ->
            dialog.dismiss()
        }.show()
    }


    private fun getFileExtension( uri: Uri?):String?{

        return if (uri!!.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))
        }else{
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString());

        }

    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    companion object {
        //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
        private const val PICK_CAMERA_IMAGE = 3
        private const val IMAGE_DIRECTORY = "Images"

    }

}