package com.assist_software.expenseappmvp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.assist_software.expenseappmvp.data.database.entities.Expense

@Dao
interface ExpenseDao {
    @Query("UPDATE users SET userCurrentBalance = userCurrentBalance - :new_expense WHERE uid = :id")
    fun updateUserExpense(id: String, new_expense: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateExpense(expense: Expense)
}