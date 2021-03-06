package com.assist_software.expenseappmvp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.assist_software.expenseappmvp.data.database.entities.Expense
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface ExpenseDao {
    @Query("UPDATE users SET userCurrentBalance = userCurrentBalance - :new_expense WHERE uid = :id")
    fun updateUserExpense(id: String, new_expense: Double): Single<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateExpense(expense: Expense): Single<Long>

    @Query("SELECT SUM(expenseAmount) FROM expenses WHERE expenseDate BETWEEN :startDate AND :endDate AND uid=:uid GROUP BY :uid")
    fun getUserExpenseByDate(startDate: Long, endDate: Long, uid: String): Maybe<Double>

    @Query("DELETE FROM expenses WHERE expenseId = :expenseId")
    fun deleteExpenseById(expenseId: Long)

    @Query("UPDATE expenses SET expenseAmount = :new_expense WHERE expenseId = :id")
    fun editExpense(id: Long, new_expense: Double): Single<Int>

    @Query("UPDATE users SET userCurrentBalance = userCurrentBalance + :old_value - :new_expense WHERE uid = :id")
    fun editUserExpense(id: String, new_expense: Double, old_value: Double): Single<Int>

    @Query("SELECT expenseImage FROM expenses WHERE expenseId = :id")
    fun getExpenseImage(id: Long): Single<ByteArray>

    @Query("SELECT expenseId, uid, expenseDate, SUM(expenseAmount)  AS expenseAmount, expenseCategory, expenseDetails, expenseImage FROM expenses WHERE uid = :id AND expenseDate BETWEEN :dateStart AND :dateEnd GROUP BY expenseCategory")
    fun getExpensesByCategoryInInterval(id: String, dateStart: Long, dateEnd: Long): MutableList<Expense>
}