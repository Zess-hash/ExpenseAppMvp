package com.assist_software.expenseappmvp.screens.addActionScreen

import android.Manifest
import android.widget.Toast
import com.assist_software.expenseappmvp.R
import com.assist_software.expenseappmvp.data.database.repositories.ExpenseRepository
import com.assist_software.expenseappmvp.data.database.repositories.IncomeRepository
import com.assist_software.expenseappmvp.data.database.repositories.UserRepository
import com.assist_software.expenseappmvp.data.utils.Constants
import com.assist_software.expenseappmvp.data.utils.rx.RxSchedulers
import com.assist_software.expenseappmvp.screens.addActionScreen.adapter.models.CategoryItem
import com.assist_software.expenseappmvp.screens.addActionScreen.enum.CategoryEnum
import com.assist_software.expenseappmvp.utils.SharedPrefUtils
import com.assist_software.expenseappmvp.utils.disposeBy
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AddActionPresenter(
    private val view: AddActionView,
    private val rxSchedulers: RxSchedulers,
    private val rxPermissions: RxPermissions,
    private val userRepository: UserRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val sharedPref: SharedPrefUtils,
    private val compositeDisposables: CompositeDisposable
) {
    private val onItemClick = PublishSubject.create<CategoryItem>()
    private var categorySelected: String = view.activity.getString(R.string.income)

    fun onCreate() {
        itemClickAction()
        view.initCategoryGrid(onItemClick, setCategoryData())
        compositeDisposables.addAll(
            selectDate(),
            loadImageFormStorage(),
            takePhoto(),
            deletePhoto(),
            cameraPermission()
        )
        saveAction()
    }

    fun onDestroy() {
        compositeDisposables.clear()
    }

    private fun itemClickAction() {
        onItemClick.observeOn(rxSchedulers.androidUI())
            .subscribe({ item ->
                categorySelected = item.categoryName.toLowerCase().capitalize()
            }, {
                Timber.i(it.localizedMessage)
            })
            .disposeBy(compositeDisposables)
    }

    private fun setCategoryData(): List<CategoryItem> {
        val list: MutableList<CategoryItem> = mutableListOf()
        enumValues<CategoryEnum>().forEach { it ->
            val selected = it.getName(view.activity)
                .equals(CategoryEnum.INCOME.getName(view.activity), true)
            list.add(
                CategoryItem(
                    it.getIcon(view.activity),
                    it.getName(view.activity),
                    selected
                )
            )
        }
        return list
    }

    private fun selectDate(): Disposable {
        return view.openDatePicker()
            .subscribe {
                view.initDatePicker()
            }
    }

    private fun saveAction() {
        return view.onSaveClick()
            .subscribe({
                if (categorySelected == view.activity.getString(R.string.income)) {
                    saveIncomeTransaction()
                    Toast.makeText(
                        view.activity,
                        view.activity.getString(R.string.budget_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    saveExpenseTransaction()
                    Toast.makeText(
                        view.activity,
                        view.activity.getString(R.string.expense_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, {
                Timber.e(it.localizedMessage)
            }).disposeBy(compositeDisposables)
    }

    private fun cameraPermission(): Disposable {
        return rxPermissions.request(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            .doOnNext {
                if (it) {
                    takePhoto()
                    loadImageFormStorage()
                } else {
                    Toast.makeText(
                        view.activity,
                        view.activity.getString(R.string.camera_permission_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .subscribe()
    }

    private fun takePhoto(): Disposable {
        return view.openCamera()
            .throttleFirst(Constants.THROTTLE_DURATION, TimeUnit.SECONDS)
            .subscribe {
                view.addImageFromCamera()
            }
    }

    private fun loadImageFormStorage(): Disposable {
        return view.openLocalStorage()
            .throttleFirst(Constants.THROTTLE_DURATION, TimeUnit.SECONDS)
            .subscribe {
                view.addImageFromLocalStorage()
            }
    }

    private fun deletePhoto(): Disposable {
        return view.onClickDeletePhoto()
            .throttleFirst(Constants.THROTTLE_DURATION, TimeUnit.SECONDS)
            .subscribe {
                view.removePhotoFromImageView()
            }
    }

    private fun saveIncomeTransaction() {
        sharedPref.read(Constants.USER_ID, "")?.run {
            val income = view.getIncome(this, categorySelected)
            incomeRepository.insertIncome(income)
                .observeOn(rxSchedulers.background())
                .flatMap {
                    incomeRepository.updateUserIncome(this, income.incomeAmount)
                }
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    view.showHomeScreen()
                }, {
                    Timber.e(it.localizedMessage)
                })
                .disposeBy(compositeDisposables)
        }
    }

    private fun saveExpenseTransaction() {
        sharedPref.read(Constants.USER_ID, "")?.run {
            val expense = view.getExpense(this, categorySelected)
            expenseRepository.insertExpense(expense)
                .observeOn(rxSchedulers.background())
                .flatMap {
                    expenseRepository.updateUserExpense(this, expense.expenseAmount)
                }
                .observeOn(rxSchedulers.androidUI())
                .subscribe({
                    view.showHomeScreen()
                }, {
                    Timber.e(it.localizedMessage)
                })
                .disposeBy(compositeDisposables)
        }
    }
}