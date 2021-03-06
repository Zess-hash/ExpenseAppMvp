package com.assist_software.expenseappmvp.screens.mainScreen.fragments.expense.dialog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.assist_software.expenseappmvp.R
import com.assist_software.expenseappmvp.screens.addActionScreen.AddActionActivity
import com.assist_software.expenseappmvp.screens.addActionScreen.enum.CategoryEnum
import com.assist_software.expenseappmvp.screens.mainScreen.fragments.expense.adapter.FragmentDialogListener
import com.assist_software.expenseappmvp.screens.mainScreen.fragments.expense.adapter.models.Transaction
import com.assist_software.expenseappmvp.utils.TimeUtils
import com.google.gson.Gson
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.category_item.view.*
import kotlinx.android.synthetic.main.dialog_transaction_info.view.*


class ExpenseDialog(
    private var transaction: Transaction,
    private var listener: PublishSubject<Transaction>
    //private val listener: FragmentDialogListener
) : DialogFragment() {
    private lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.dialog_transaction_info, container, false)

        initComponents(transaction)
        initButtons()
        return layout
    }

    private fun initComponents(transaction: Transaction) {
        layout.category_name.text = transaction.category
        when (transaction.category) {
            CategoryEnum.INCOME.getName(layout.context).toLowerCase().capitalize() -> {
                layout.category_image.setImageResource(CategoryEnum.INCOME.getIcon(layout.context))
            }
            CategoryEnum.FOOD.getName(layout.context).toLowerCase().capitalize() -> {
                layout.category_image.setImageResource(CategoryEnum.FOOD.getIcon(layout.context))
            }
            CategoryEnum.CAR.getName(layout.context).toLowerCase().capitalize() -> {
                layout.category_image.setImageResource(CategoryEnum.CAR.getIcon(layout.context))
            }
            CategoryEnum.CLOTHES.getName(layout.context).toLowerCase().capitalize() -> {
                layout.category_image.setImageResource(CategoryEnum.CLOTHES.getIcon(layout.context))
            }
            CategoryEnum.SAVINGS.getName(layout.context).toLowerCase().capitalize() -> {
                layout.category_image.setImageResource(CategoryEnum.SAVINGS.getIcon(layout.context))
            }
            CategoryEnum.HEALTH.getName(layout.context).toLowerCase().capitalize() -> {
                layout.category_image.setImageResource(CategoryEnum.HEALTH.getIcon(layout.context))
            }
            CategoryEnum.BEAUTY.getName(layout.context).toLowerCase().capitalize() -> {
                layout.category_image.setImageResource(CategoryEnum.BEAUTY.getIcon(layout.context))
            }
            CategoryEnum.TRAVEL.getName(layout.context).toLowerCase().capitalize() -> {
                layout.category_image.setImageResource(CategoryEnum.TRAVEL.getIcon(layout.context))
            }
        }
        layout.dialog_date_editText.setText(TimeUtils.gmtToLocalTime(transaction.date))
        layout.dialog_amount_editText.setText(transaction.amount.toString())
        layout.dialog_details_editText.setText(transaction.details)
        if (transaction.imageDetails.isNotEmpty()) {
            layout.dialog_details_ImageView.setImageBitmap(byteArrayToImage(transaction.imageDetails))
        }
    }

    private fun initButtons() {
        layout.close_dialog_btn.setOnClickListener { dialog?.dismiss() }
        layout.dialog_delete_textView.setOnClickListener(deleteOnClickListener)
        layout.dialog_edit_textView.setOnClickListener(editOnClickListener)
    }

    private fun byteArrayToImage(bytArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytArray, 0, bytArray.size)
    }

    private val deleteOnClickListener = View.OnClickListener {
        //listener.onDeleteClick(transaction)
        listener.onNext(transaction)
        dialog?.dismiss()
    }

    private val editOnClickListener = View.OnClickListener {
        val intent = Intent(context, AddActionActivity::class.java).apply {
            putExtra("transaction",
                Gson().toJson(transaction.apply { imageDetails = ByteArray(0) }))
        }
        startActivity(intent)
        dialog?.dismiss()
    }
}