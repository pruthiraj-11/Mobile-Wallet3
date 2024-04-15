package org.mifos.mobilewallet.mifospay.password.presenter

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.mifos.mobilewallet.core.base.UseCase.UseCaseCallback
import org.mifos.mobilewallet.core.base.UseCaseHandler
import org.mifos.mobilewallet.core.domain.model.user.NewUser
import org.mifos.mobilewallet.core.domain.model.user.UpdateUserEntityPassword
import org.mifos.mobilewallet.core.domain.usecase.user.AuthenticateUser
import org.mifos.mobilewallet.core.domain.usecase.user.UpdateUser
import org.mifos.mobilewallet.mifospay.base.BaseView
import org.mifos.mobilewallet.mifospay.data.local.PreferencesHelper
import org.mifos.mobilewallet.mifospay.password.EditPasswordContract
import org.mifos.mobilewallet.mifospay.password.EditPasswordContract.EditPasswordView
import org.mifos.mobilewallet.mifospay.utils.Constants
import javax.inject.Inject

class EditPasswordPresenter @Inject constructor(
    private val mUseCaseHandler: UseCaseHandler,
    private val mPreferencesHelper: PreferencesHelper
) : EditPasswordContract.EditPasswordPresenter {
    @JvmField
    @Inject
    var updateUserUseCase: UpdateUser? = null

    @JvmField
    @Inject
    var authenticateUserUseCase: AuthenticateUser? = null
    private var mEditPasswordView: EditPasswordView? = null
    override fun attachView(baseView: BaseView<*>?) {
        mEditPasswordView = baseView as EditPasswordView?
        mEditPasswordView?.setPresenter(this)
    }

    override fun handleSavePasswordButtonStatus(
        currentPassword: String?,
        newPassword: String?,
        newPasswordRepeat: String?
    ) {
        if (currentPassword == "" || newPassword == "" || newPasswordRepeat == "") {
            mEditPasswordView?.disableSavePasswordButton()
        } else {
            if (newPassword == newPasswordRepeat) {
                mEditPasswordView?.enableSavePasswordButton()
            } else {
                mEditPasswordView?.disableSavePasswordButton()
            }
        }
    }

    override fun updatePassword(
        currentPassword: String?,
        newPassword: String?,
        newPasswordRepeat: String?
    ) {
        mEditPasswordView?.startProgressBar()
        if (isNotEmpty(currentPassword) && isNotEmpty(newPassword)
            && isNotEmpty(newPasswordRepeat)
        ) {
            when {
                currentPassword == newPassword -> {
                    mEditPasswordView?.stopProgressBar()
                    mEditPasswordView?.showError(Constants.ERROR_PASSWORDS_CANT_BE_SAME)
                }
                newPassword?.let {
                    newPasswordRepeat?.let { it1 ->
                        isNewPasswordValid(
                            it,
                            it1
                        )
                    }
                } == true -> {
                    if (currentPassword != null) {
                        updatePassword(currentPassword, newPassword)
                    }
                }
                else -> {
                    mEditPasswordView?.stopProgressBar()
                    mEditPasswordView?.showError(Constants.ERROR_VALIDATING_PASSWORD)
                }
            }
        } else {
            mEditPasswordView?.stopProgressBar()
            mEditPasswordView?.showError(Constants.ERROR_FIELDS_CANNOT_BE_EMPTY)
        }
    }

    private fun isNotEmpty(str: String?): Boolean {
        return !str.isNullOrEmpty()
    }

    private fun isNewPasswordValid(newPassword: String, newPasswordRepeat: String): Boolean {
        return newPassword == newPasswordRepeat
    }

    private fun updatePassword(currentPassword: String, newPassword: String) {
        // authenticate and then update
//        mUseCaseHandler.execute(authenticateUserUseCase,
//            AuthenticateUser.RequestValues(
//                mPreferencesHelper.username,
//                currentPassword
//            ),
//            object : UseCaseCallback<AuthenticateUser.ResponseValue?> {
//                override fun onSuccess(response: AuthenticateUser.ResponseValue?) {
//                    mUseCaseHandler.execute(updateUserUseCase,
//                        UpdateUser.RequestValues(
//                            UpdateUserEntityPassword(newPassword),
//                            mPreferencesHelper.userId.toInt()
//                        ),
//                        object : UseCaseCallback<UpdateUser.ResponseValue?> {
//                            override fun onSuccess(response: UpdateUser.ResponseValue?) {
//                                mEditPasswordView?.stopProgressBar()
//                                mEditPasswordView?.closeActivity()
//                            }
//
//                            override fun onError(message: String) {
//                                mEditPasswordView?.stopProgressBar()
//                                mEditPasswordView?.showError(message)
//                            }
//                        })
//                }
//
//                override fun onError(message: String) {
//                    mEditPasswordView?.stopProgressBar()
//                    mEditPasswordView?.showError("Wrong password")
//                }
//            })
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        val mFirebaseDatabase: DatabaseReference?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        mFirebaseDatabase=mFirebaseDatabaseInstances.getReference().child("moneypay").child("newusers")
        mFirebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val child= dataSnapshot.children
                    child.forEach {
                        val users=it.getValue(NewUser::class.java)
                        if (users?.username.equals(mPreferencesHelper.username) and users?.password.equals(currentPassword)) {
                            FirebaseDatabase.getInstance().getReference("moneypay").child("newusers").child(mPreferencesHelper.userId.toString())
                                .child("password").setValue(newPassword).addOnCompleteListener { it1 ->
                                if (it1.isSuccessful) {
                                    FirebaseDatabase.getInstance().getReference("moneypay")
                                        .child("newusers")
                                        .child(mPreferencesHelper.userId.toString())
                                        .child("repeatPassword").setValue(newPassword)
                                        .addOnCompleteListener { it2 ->
                                            if (it2.isSuccessful) {
                                                mEditPasswordView?.stopProgressBar()
                                                mEditPasswordView?.closeActivity()
                                            }
                                        }
                                } else {
                                    mEditPasswordView?.stopProgressBar()
                                    mEditPasswordView?.showError(it1.exception?.message)
                                }
                            }
                            return@forEach
                        } else if (users?.username.equals(mPreferencesHelper.username) and !users?.password.equals(currentPassword)){
                            mEditPasswordView?.stopProgressBar()
                            mEditPasswordView?.showError("Wrong password")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                mEditPasswordView?.stopProgressBar()
                mEditPasswordView?.showError(error.message)
            }
        })
    }
}