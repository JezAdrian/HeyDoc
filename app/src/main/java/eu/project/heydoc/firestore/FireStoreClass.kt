package eu.project.heydoc.firestore

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import eu.project.heydoc.activities.MainActivity
import eu.project.heydoc.activities.MyProfileActivity
import eu.project.heydoc.activities.SignInActivity
import eu.project.heydoc.activities.SignUpActivity
import eu.project.heydoc.models.User
import eu.project.heydoc.utils.Constants

class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge()).addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener { _ ->
                Log.e(activity.javaClass.simpleName,"Error")
            }
    }

    fun loadUserData(activity: Activity){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserId()).get().addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)!!
                    when(activity){
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser)
                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }

                }.addOnFailureListener { _ ->
                    when(activity){
                        is SignInActivity -> {
                            activity.hideProgressDialog()
                        }
                        is MainActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(activity.javaClass.simpleName,"Error")
                }
    }
    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).update(userHashMap).addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile data updated !!")
                Toast.makeText(activity, "UPDATED SUCCESSFULLY", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener {
                    e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error when creating a board",e)
                Toast.makeText(activity, "error when updating a profile ", Toast.LENGTH_SHORT).show()
            }
    }

    fun getCurrentUserId():String{
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID= currentUser.uid
        }
        return currentUserID
    }

}