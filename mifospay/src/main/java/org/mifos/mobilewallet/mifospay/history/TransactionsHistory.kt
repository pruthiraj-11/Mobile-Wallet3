package org.mifos.mobilewallet.mifospay.history

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.update
import org.mifos.mobilewallet.core.base.TaskLooper
import org.mifos.mobilewallet.core.base.UseCase.UseCaseCallback
import org.mifos.mobilewallet.core.base.UseCaseFactory
import org.mifos.mobilewallet.core.base.UseCaseHandler
import org.mifos.mobilewallet.core.domain.model.Account
import org.mifos.mobilewallet.core.domain.model.Transaction
import org.mifos.mobilewallet.core.domain.usecase.account.FetchAccount
import org.mifos.mobilewallet.core.domain.usecase.account.FetchAccountTransactions
import org.mifos.mobilewallet.mifospay.history.HistoryContract.TransactionsHistoryAsync
import javax.inject.Inject

class TransactionsHistory @Inject constructor(private val mUsecaseHandler: UseCaseHandler) {
    var delegate: TransactionsHistoryAsync? = null

    @JvmField
    @Inject
    var mFetchAccountUseCase: FetchAccount? = null

    @JvmField
    @Inject
    var fetchAccountTransactionsUseCase: FetchAccountTransactions? = null

    @JvmField
    @Inject
    var mTaskLooper: TaskLooper? = null

    @JvmField
    @Inject
    var mUseCaseFactory: UseCaseFactory? = null
    private var transactions: List<Transaction>?

    init {
        transactions = ArrayList()
    }

    fun fetchTransactionsHistory(accountId: Long) {
//        mUsecaseHandler.execute(fetchAccountTransactionsUseCase,
//            FetchAccountTransactions.RequestValues(accountId),
//            object : UseCaseCallback<FetchAccountTransactions.ResponseValue?> {
//                override fun onSuccess(response: FetchAccountTransactions.ResponseValue?) {
//                    transactions = response?.transactions
//                    delegate!!.onTransactionsFetchCompleted(transactions)
//                }
//
//                override fun onError(message: String) {
//                    transactions = null
//                }
//            })
        val mFirebaseDatabaseInstances: FirebaseDatabase?
        val mFirebaseDatabase: DatabaseReference?
        mFirebaseDatabaseInstances= FirebaseDatabase.getInstance()
        mFirebaseDatabase= mFirebaseDatabaseInstances.getReference().child("moneypay").child("transactions").child(accountId.toString())
        mFirebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val transactions1: MutableList<Transaction> =ArrayList()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val transaction=snapshot.getValue(Transaction::class.java)
                        transactions1.add(transaction!!)
                    }
                    transactions=transactions1
                    delegate!!.onTransactionsFetchCompleted(transactions)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                transactions = null
            }
        })
    }
}