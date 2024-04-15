package org.mifos.mobilewallet.mifospay.registration.presenter

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.mifos.mobilewallet.core.base.UseCase.UseCaseCallback
import org.mifos.mobilewallet.core.base.UseCaseHandler
import org.mifos.mobilewallet.core.data.fineract.api.FineractApiManager
import org.mifos.mobilewallet.core.data.fineract.entity.UserWithRole
import org.mifos.mobilewallet.core.domain.model.Account
import org.mifos.mobilewallet.core.domain.model.Currency
import org.mifos.mobilewallet.core.domain.model.client.Client
import org.mifos.mobilewallet.core.domain.model.client.Client1
import org.mifos.mobilewallet.core.domain.model.client.NewClient
import org.mifos.mobilewallet.core.domain.model.user.NewUser
import org.mifos.mobilewallet.core.domain.model.user.UpdateUserEntityClients
import org.mifos.mobilewallet.core.domain.model.user.User
import org.mifos.mobilewallet.core.domain.model.user.User1
import org.mifos.mobilewallet.core.domain.usecase.client.CreateClient
import org.mifos.mobilewallet.core.domain.usecase.client.FetchClientData
import org.mifos.mobilewallet.core.domain.usecase.client.SearchClient
import org.mifos.mobilewallet.core.domain.usecase.user.AuthenticateUser
import org.mifos.mobilewallet.core.domain.usecase.user.CreateUser
import org.mifos.mobilewallet.core.domain.usecase.user.DeleteUser
import org.mifos.mobilewallet.core.domain.usecase.user.FetchUserDetails
import org.mifos.mobilewallet.core.domain.usecase.user.UpdateUser
import org.mifos.mobilewallet.mifospay.base.BaseView
import org.mifos.mobilewallet.mifospay.data.local.LocalRepository
import org.mifos.mobilewallet.mifospay.data.local.PreferencesHelper
import org.mifos.mobilewallet.mifospay.registration.RegistrationContract
import org.mifos.mobilewallet.mifospay.registration.RegistrationContract.SignupView
import org.mifos.mobilewallet.mifospay.utils.Constants
import org.mifos.mobilewallet.mifospay.utils.DebugUtil
import org.mifos.mobilewallet.mifospay.utils.PasswordStrength
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt


class SignupPresenter @Inject constructor(
    private val mUseCaseHandler: UseCaseHandler,
    private val mPreferencesHelper: PreferencesHelper
) : RegistrationContract.SignupPresenter {
    var mSignupView: SignupView? = null

    @JvmField
    @Inject
    var searchClientUseCase: SearchClient? = null

    @JvmField
    @Inject
    var createClientUseCase: CreateClient? = null

    @JvmField
    @Inject
    var createUserUseCase: CreateUser? = null

    @JvmField
    @Inject
    var updateUserUseCase: UpdateUser? = null

    @JvmField
    @Inject
    var authenticateUserUseCase: AuthenticateUser? = null

    @JvmField
    @Inject
    var fetchClientDataUseCase: FetchClientData? = null

    @JvmField
    @Inject
    var deleteUserUseCase: DeleteUser? = null

    @JvmField
    @Inject
    var fetchUserDetailsUseCase: FetchUserDetails? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var mobileNumber: String? = null
    private var email: String? = null
    private var businessName: String? = null
    private var addressLine1: String? = null
    private var addressLine2: String? = null
    private var pincode: String? = null
    private var city: String? = null
    private var countryName: String? = null
    private var username: String? = null
    private var password: String? = null
    private var stateId: String? = null
    private var countryId: String? = null
    private var mifosSavingsProductId = 0
    private var usersId: Long=0
    override fun attachView(baseView: BaseView<*>?) {
        mSignupView = baseView as SignupView?
        mSignupView!!.setPresenter(this)
    }

    override fun checkPasswordStrength(password: String?) {
        val p = PasswordStrength(password)
        mSignupView!!.updatePasswordStrength(
            p.strengthStringId,
            p.colorResId, p.value
        )
    }

    override fun registerUser(
        firstName: String?, lastName: String?,
        mobileNumber: String?, email: String?, businessName: String?,
        addressline1: String?, addressline2: String?, pincode: String?,
        city: String?, countryName: String?, username: String?, password: String?,
        stateId: String?, countryId: String?, mifosSavingProductId: Int) {
        // 0. Unique Mobile Number (checked in MOBILE VERIFICATION ACTIVITY)
        // 1. Check for unique external id and username
        // 2. Create user
        // 3. Create Client
        // 4. Update User and connect client with user
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.businessName = businessName
        addressLine1 = addressline1
        addressLine2 = addressline2
        this.pincode = pincode
        this.city = city
        this.countryName = countryName
        this.username = username
        this.password = password
        this.stateId = stateId
        this.countryId = countryId
        this.mobileNumber = mobileNumber
        mifosSavingsProductId = mifosSavingProductId
        var flag =false
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        val mFirebaseDatabase: DatabaseReference = mFirebaseDatabaseInstances.getReference("moneypay").child("newusers")
        mFirebaseDatabase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (snapshots in snapshot.children){
                        val user=snapshots.getValue(NewUser::class.java)
                        if (user?.username.equals(username)) {
                            flag=true
                            mSignupView!!.onRegisterFailed("Username already exists.")
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                DebugUtil.log(error.message)
            }
        })
        if (!flag) {
            createUser()
        }
//        mUseCaseHandler.execute(searchClientUseCase, SearchClient.RequestValues("$username@mifos"),
//            object : UseCaseCallback<SearchClient.ResponseValue?> {
//                override fun onSuccess(response: SearchClient.ResponseValue?) {
//                    mSignupView!!.onRegisterFailed("Username already exists.")
//                }
//
//                override fun onError(message: String) {
//                    createUser()
//                }
//            })
    }

    private fun createUser() {
        val newUser = NewUser(username, firstName, lastName, email, password)
//        mUseCaseHandler.execute(createUserUseCase, CreateUser.RequestValues(newUser),
//            object : UseCaseCallback<CreateUser.ResponseValue?> {
//                override fun onSuccess(response: CreateUser.ResponseValue?) {
//                    response?.userId?.let { createClient(it) }
//                }
//
//                override fun onError(message: String) {
//                    DebugUtil.log(message)
//                    mSignupView!!.onRegisterFailed(message)
//                }
//            })
        usersId = Random.nextLong(2,  99999+ 1)
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        val mFirebaseDatabase: DatabaseReference = mFirebaseDatabaseInstances.getReference("moneypay").child("newusers").child(usersId.toString())
        mFirebaseDatabase.setValue(newUser).addOnCompleteListener {
            if (it.isSuccessful) {
                val userId:Int=usersId.toInt()
                val newRegisteredUser=User()
                newRegisteredUser.userName=username
                newRegisteredUser.authenticationKey=""
                newRegisteredUser.userId=usersId
                FirebaseDatabase.getInstance().getReference("moneypay").child("RegisteredUsers").child(
                    usersId.toString()
                ).setValue(newRegisteredUser)

                val newRegisteredUserWithRole=UserWithRole()
                newRegisteredUserWithRole.username=username
                newRegisteredUserWithRole.id=usersId.toString()
                newRegisteredUserWithRole.email=email
                FirebaseDatabase.getInstance().getReference("moneypay").child("RegisteredUserWithRole").child(
                    usersId.toString()
                ).setValue(newRegisteredUserWithRole)
                saveUserDetails(newRegisteredUser,newRegisteredUserWithRole)
                createClient(userId)
            }
        }.addOnFailureListener{ err->
            DebugUtil.log(err.localizedMessage)
            mSignupView!!.onRegisterFailed(err.localizedMessage)
        }
    }

    private fun createClient(userId: Int) {
        DebugUtil.log("mob:::::", mobileNumber)
        val newClient = NewClient(
            businessName, username, addressLine1,
            addressLine2, city, pincode, stateId, countryId, mobileNumber,
            mifosSavingsProductId
        )
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        val clientId=Random.nextLong(2,  999999)
        val mFirebaseDatabase: DatabaseReference = mFirebaseDatabaseInstances.getReference("moneypay").child("newclients").child(userId.toString()).child(clientId.toString())
        mFirebaseDatabase.setValue(newClient).addOnCompleteListener {
            if (it.isSuccessful) {
                val client= Client()
                client.name=username
                client.image=""
                client.externalId=username+"@moneypay"
                client.clientId=clientId
                client.displayName= firstName+" "+lastName
                client.mobileNo=mobileNumber
                FirebaseDatabase.getInstance().getReference("moneypay").child("RegisteredClients").child(
                    clientId.toString()
                ).setValue(client).addOnCompleteListener{it1->
                    if (it1.isSuccessful) {
                        saveClientDetails(client)
                        val account=Account()
                        account.image=""
                        account.name= firstName+" "+lastName
                        account.number="MWA00000"+ Random.nextInt(2000,5000)
                        val currency= Currency()
                        currency.code="INR"
                        currency.displaySymbol= "₹"
                        currency.displayLabel= "Balance"
                        account.currency=currency
                        account.productId=0
                        account.balance=5000.00
                        account.id=Random.nextLong(6000000,7000000)
                        FirebaseDatabase.getInstance().getReference("moneypay").child("accounts").child(
                            clientId.toString()
                        ).setValue(account).addOnCompleteListener{it2->
                            if (it2.isSuccessful) {
                                mSignupView!!.onRegisterSuccess("Registered Successfully")
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener{ err->
            DebugUtil.log(err.localizedMessage)
            mSignupView!!.onRegisterFailed(err.localizedMessage)
        }
//        mUseCaseHandler.execute(createClientUseCase,
//            CreateClient.RequestValues(newClient),
//            object : UseCaseCallback<CreateClient.ResponseValue?> {
//                override fun onSuccess(response: CreateClient.ResponseValue?) {
//                    DebugUtil.log(response?.clientId)
//                    val clients = ArrayList<Int>()
//                    response?.clientId?.let { clients.add(it) }
//                    updateClient(clients, userId)
//                }
//
//                override fun onError(message: String) {
//                    // delete user
//                    DebugUtil.log(message)
//                    mSignupView!!.onRegisterFailed(message)
//                    deleteUser(userId)
//                }
//            })
    }

    private fun  updateClient(clients: ArrayList<Int>, userId: Int) {
        mUseCaseHandler.execute(updateUserUseCase,
            UpdateUser.RequestValues(UpdateUserEntityClients(clients), userId),
            object : UseCaseCallback<UpdateUser.ResponseValue?> {
                override fun onSuccess(response: UpdateUser.ResponseValue?) {
                    loginUser(username, password)
                }

                override fun onError(message: String) {
                    // connect client later
                    DebugUtil.log(message)
                    mSignupView!!.onRegisterFailed("update client error")
                }
            })
    }

    private fun loginUser(username: String?, password: String?) {
//        authenticateUserUseCase!!.requestValues = AuthenticateUser.RequestValues(username, password)
//        val requestValue = authenticateUserUseCase!!.requestValues
//        mUseCaseHandler.execute(authenticateUserUseCase, requestValue,
//            object : UseCaseCallback<AuthenticateUser.ResponseValue?> {
//                override fun onSuccess(response: AuthenticateUser.ResponseValue?) {
//                    response?.user?.let { createAuthenticatedService(it) }
//                    fetchClientData()
//                    response?.user?.let { fetchUserDetails(it) }
//                }
//
//                override fun onError(message: String) {
//                    mSignupView!!.onRegisterSuccess("Login Failed")
//                }
//            })
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
                            mSignupView!!.loginSuccess()
                            return@forEach
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                mSignupView!!.onRegisterSuccess("Login Failed")
            }
        })
    }

    private fun fetchUserDetails(user: User) {
//        mUseCaseHandler.execute(fetchUserDetailsUseCase,
//            FetchUserDetails.RequestValues(user.userId),
//            object : UseCaseCallback<FetchUserDetails.ResponseValue?> {
//                override fun onSuccess(response: FetchUserDetails.ResponseValue?) {
//                    response?.userWithRole?.let { saveUserDetails(user, it) }
//                }
//
//                override fun onError(message: String) {
//                    DebugUtil.log(message)
//                }
//            })
        saveUserDetails(user, UserWithRole())
    }

    private fun fetchClientData() {
//        mUseCaseHandler.execute(fetchClientDataUseCase, null,
//            object : UseCaseCallback<FetchClientData.ResponseValue?> {
//                override fun onSuccess(response: FetchClientData.ResponseValue?) {
//                    response?.userDetails?.let { saveClientDetails(it) }
//                    if (response?.userDetails?.name != "") {
//                        mSignupView!!.loginSuccess()
//                    }
//                }
//
//                override fun onError(message: String) {
//                    mSignupView!!.onRegisterSuccess("Fetch Client Error")
//                }
//            })
        saveClientDetails(Client())
    }

    private fun createAuthenticatedService(user: User) {
        val authToken = Constants.BASIC + user.authenticationKey
        mPreferencesHelper.saveToken(authToken)
        FineractApiManager.createSelfService(mPreferencesHelper.token)
    }

    private fun saveUserDetails(
        user: User,
        userWithRole: UserWithRole
    ) {
        val userName = user.userName
        val userID = user.userId
        mPreferencesHelper.saveUsername(userName)
        mPreferencesHelper.userId = userID
        mPreferencesHelper.saveEmail(userWithRole.email)
    }

    private fun saveClientDetails(client: Client) {
        mPreferencesHelper.saveFullName(client.displayName)
        mPreferencesHelper.clientId = client.clientId
        mPreferencesHelper.saveMobile(client.mobileNo)
        mPreferencesHelper.clientVpa= client.externalId
    }

    private fun deleteUser(userId: Int) {
        mUseCaseHandler.execute(deleteUserUseCase, DeleteUser.RequestValues(userId),
            object : UseCaseCallback<DeleteUser.ResponseValue?> {
                override fun onSuccess(response: DeleteUser.ResponseValue?) {}
                override fun onError(message: String) {}
            })
    }
}