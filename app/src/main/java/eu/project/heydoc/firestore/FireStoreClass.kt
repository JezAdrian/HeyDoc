package eu.project.heydoc.firestore

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
            }.addOnFailureListener {
                    e-> Log.e(activity.javaClass.simpleName,"Error")
            }
    }

    fun signInUser(activity: SignInActivity){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).get().addOnSuccessListener {
                    document ->
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser !=null)
                    activity.signInSuccess(loggedInUser)
            }.addOnFailureListener {
                    e->
                Log.e(activity.javaClass.simpleName,"Error")
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