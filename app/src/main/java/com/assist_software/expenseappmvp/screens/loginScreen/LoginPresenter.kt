package com.assist_software.expenseappmvp.screens.loginScreen

import com.assist_software.expenseappmvp.data.database.entities.User
import com.assist_software.expenseappmvp.data.database.repositories.UserRepository
import com.assist_software.expenseappmvp.data.utils.Constants
import com.assist_software.expenseappmvp.data.utils.rx.RxSchedulers
import com.assist_software.expenseappmvp.screens.registerScreen.RegisterPresenter
import com.assist_software.expenseappmvp.utils.SharedPrefUtils
import com.assist_software.expenseappmvp.utils.Validations
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LoginPresenter(
    private val view: LoginView,
    private val rxSchedulers: RxSchedulers,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val sharedPref: SharedPrefUtils,
    private val compositeDisposables: CompositeDisposable
) {
    private val user: User = User()

    private fun onRegisterClick(): Disposable {
        return view.goToRegisterScreen()
            .throttleFirst(RegisterPresenter.THROTTLE_DURATION, TimeUnit.SECONDS)
            .subscribe { view.showRegisterScreen() }
    }

    private fun onForgotPasswordClick(): Disposable {
        return view.goToResetPasswordScreen()
            .throttleFirst(RegisterPresenter.THROTTLE_DURATION, TimeUnit.SECONDS)
            .subscribe { view.showResetPasswordScreen() }
    }

    private fun loginUser(): Disposable {
        return view.loginUserClicks()
            .observeOn(rxSchedulers.androidUI())
            .doOnNext { view.fieldValidation(user) }
            .filter {
                !(user.userEmail == "" || user.userPassword == "")
            }
            .observeOn(rxSchedulers.background())
            .subscribe({ signInUserFromFirebase(user.userEmail, user.userPassword) }, {
                Timber.i(it.localizedMessage)
            })
    }

    private fun getUserEmail(): Disposable {
        return view.inputUserEmail()
            .debounce(Constants.AWAIT_INPUT, TimeUnit.MILLISECONDS)
            .observeOn(rxSchedulers.androidUI())
            .filter { email -> Validations.emailValidation(email.toString()) }
            .subscribe { email -> user.userEmail = email.toString() }
    }

    private fun getUserPassword(): Disposable {
        return view.inputUserPassword()
            .debounce(Constants.AWAIT_INPUT, TimeUnit.MILLISECONDS)
            .observeOn(rxSchedulers.androidUI())
            .filter { password -> Validations.passwordValidation(password.toString()) }
            .subscribe { password -> user.userPassword = password.toString() }
    }

    private fun signInUserFromFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { p0 ->
            Observable.just(p0)
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    if (p0.isSuccessful) {
                        saveUserInPreferences(email)
                    }
                    view.showMessage(p0.isSuccessful)
                }, {
                    Timber.e(it.localizedMessage)
                })
        }
    }

    private fun saveUserInPreferences(email: String): Disposable? {
        return userRepository.getUserId(email)
            .observeOn(rxSchedulers.background())
            .doOnSuccess {
                sharedPref.write(Constants.USER_ID, it)
                updateUserPassword(user.userPassword, email) //new implementation
                //view.showMainScreen() // old implementation
            }
            .observeOn(rxSchedulers.androidUI())
            .subscribe()
    }

    //Function to update the password in the local database even tho we never use the local saved password
    private fun updateUserPassword(password: String, email: String): Disposable {
        return Observable.just(userRepository.updateUserPassword(password, email))
            .subscribeOn(rxSchedulers.background())
            .observeOn(rxSchedulers.androidUI())
            .subscribe { view.showMainScreen() }
    }
//--------------------------------------------------------------------------------------------------------------

    fun onCreate() {
        compositeDisposables.addAll(
            onRegisterClick(),
            loginUser(),
            getUserEmail(),
            getUserPassword(),
            onForgotPasswordClick()
        )
    }

    fun onDestroy() {
        compositeDisposables.clear()
    }
}