package org.mifos.mobilewallet.mifospay.auth.presenter

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.mifos.mobilewallet.core.base.UseCase.UseCaseCallback
import org.mifos.mobilewallet.core.base.UseCaseHandler
import org.mifos.mobilewallet.core.data.fineract.api.FineractApiManager
import org.mifos.mobilewallet.core.data.fineract.entity.UserWithRole
import org.mifos.mobilewallet.core.domain.model.client.Client
import org.mifos.mobilewallet.core.domain.model.user.NewUser
import org.mifos.mobilewallet.core.domain.model.user.User
import org.mifos.mobilewallet.core.domain.usecase.client.FetchClientData
import org.mifos.mobilewallet.core.domain.usecase.user.AuthenticateUser
import org.mifos.mobilewallet.core.domain.usecase.user.FetchUserDetails
import org.mifos.mobilewallet.mifospay.auth.AuthContract
import org.mifos.mobilewallet.mifospay.auth.AuthContract.LoginView
import org.mifos.mobilewallet.mifospay.base.BaseView
import org.mifos.mobilewallet.mifospay.data.local.PreferencesHelper
import org.mifos.mobilewallet.mifospay.utils.Constants
import org.mifos.mobilewallet.mifospay.utils.DebugUtil
import javax.inject.Inject

/**
 * Created by naman on 16/6/17.
 */
class LoginPresenter @Inject constructor(
    private val mUsecaseHandler: UseCaseHandler,
    private val preferencesHelper: PreferencesHelper
) : AuthContract.LoginPresenter {
    @Inject
    lateinit var authenticateUserUseCase: AuthenticateUser

    @Inject
    lateinit var fetchClientDataUseCase: FetchClientData

    @Inject
    lateinit var fetchUserDetailsUseCase: FetchUserDetails
    private lateinit var mLoginView: LoginView

    override fun handleLoginButtonStatus(usernameContent: String?, passwordContent: String?) {
        if (usernameContent!!.isEmpty() || passwordContent!!.isEmpty()) {
            mLoginView.disableLoginButton()
        } else {
            mLoginView.enableLoginButton()
        }
    }

    override fun loginUser(username: String?, password: String?) {
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        val mFirebaseDatabase: DatabaseReference?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        mFirebaseDatabase= mFirebaseDatabaseInstances.getReference().child("moneypay").child("newusers")
        mFirebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val child= dataSnapshot.children
                    child.forEach {
                        val users=it.getValue(NewUser::class.java)
                        if (users?.username.equals(username) and users?.password.equals(password)) {
                            it.key?.let { it1 -> fetchClientData(it1, users!!.username) }
                            FirebaseDatabase.getInstance().getReference("moneypay").child("RegisteredUsers").addValueEventListener(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (datasnapshot in snapshot.children) {
                                        if (datasnapshot.key!! == it.key) {
                                            datasnapshot.getValue(User::class.java)
                                                ?.let { it1 -> fetchUserDetails(it1) }
                                            break
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                            return@forEach
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                mLoginView.loginFail(error.message)
            }
        })
//        authenticateUserUseCase.requestValues = AuthenticateUser.RequestValues(username, password)
//        val requestValue = authenticateUserUseCase.requestValues
//        mUsecaseHandler.execute(authenticateUserUseCase, requestValue,
//            object : UseCaseCallback<AuthenticateUser.ResponseValue> {
//                override fun onSuccess(response: AuthenticateUser.ResponseValue) {
//                    createAuthenticatedService(response.user)
//                    fetchClientData()
//                    fetchUserDetails(response.user)
//                }
//
//                override fun onError(message: String) {
//                    mLoginView.loginFail(message)
//                }
//            })
    }

    override fun attachView(baseView: BaseView<*>?) {
        mLoginView = baseView as LoginView
        mLoginView.setPresenter(this)
    }

    private fun fetchUserDetails(user: User) {
//        mUsecaseHandler.execute(fetchUserDetailsUseCase,
//            FetchUserDetails.RequestValues(user.userId),
//            object : UseCaseCallback<FetchUserDetails.ResponseValue> {
//                override fun onSuccess(response: FetchUserDetails.ResponseValue) {
//                    saveUserDetails(user, response.userWithRole)
//                }
//
//                override fun onError(message: String) {
//                    DebugUtil.log(message)
//                }
//            })
        FirebaseDatabase.getInstance().getReference("moneypay").child("RegisteredUserWithRole").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (datasnapshot in snapshot.children) {
                        if (datasnapshot.key!! == user.userId.toString()) {
                            datasnapshot.getValue(UserWithRole::class.java)
                                ?.let { saveUserDetails(user, it) }
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun fetchClientData(key: String, iname: String) {
//        mUsecaseHandler.execute(fetchClientDataUseCase, null,
//            object : UseCaseCallback<FetchClientData.ResponseValue> {
//                override fun onSuccess(response: FetchClientData.ResponseValue) {
//                    saveClientDetails(response.userDetails)
//                    if (response.userDetails.name != "") {
//                        mLoginView.loginSuccess()
//                    }
//                }
//
//                override fun onError(message: String) {}
//            })
        FirebaseDatabase.getInstance().getReference("moneypay").child("RegisteredClients").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (datasnapshot in snapshot.children) {
                        val client=datasnapshot.getValue(Client::class.java)
                        if (client?.name.equals(iname)) {
                            client?.let { saveClientDetails(it) }
                            mLoginView.loginSuccess()
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun createAuthenticatedService(user: User) {
        val authToken = Constants.BASIC + user.authenticationKey
        preferencesHelper.saveToken(authToken)
        FineractApiManager.createSelfService(preferencesHelper.token)
    }

    private fun saveUserDetails(
        user: User,
        userWithRole: UserWithRole
    ) {
        val userName = user.userName
        val userID = user.userId
        preferencesHelper.saveUsername(userName)
        preferencesHelper.userId = userID
        preferencesHelper.saveEmail(userWithRole.email)
    }

    private fun saveClientDetails(client: Client) {
        preferencesHelper.saveFullName(client.displayName)
        preferencesHelper.clientId = client.clientId
        preferencesHelper.saveMobile(client.mobileNo)
        preferencesHelper.clientVpa= client.externalId
    }
}