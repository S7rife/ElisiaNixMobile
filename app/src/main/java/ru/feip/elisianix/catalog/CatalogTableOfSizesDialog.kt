package ru.feip.elisianix.catalog

import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import ru.feip.elisianix.R
import ru.feip.elisianix.common.BaseBottomDialog
import ru.feip.elisianix.databinding.DialogTableOfSizesBinding
import ru.feip.elisianix.extensions.buildTableOfSizes
import ru.feip.elisianix.extensions.colorEnd


class CatalogTableOfSizesDialog :
    BaseBottomDialog<DialogTableOfSizesBinding>(R.layout.dialog_table_of_sizes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val grey = resources.getColor(R.color.black10, context?.theme)
        val transparent = resources.getColor(R.color.transparent, context?.theme)

        binding.apply {
            dialogCloseBtn.setOnClickListener { findNavController().popBackStack() }
            dialogLabel.text = getString(R.string.table_of_sizes)

            val header = mutableListOf(
                getString(R.string.rus).uppercase(), getString(R.string.bust_girth),
                getString(R.string.waist_girth), getString(R.string.hip_girth),
            )
            val table = buildTableOfSizes()

            addRow(header, grey, true)
            table.forEach { addRow(it, transparent) }
            addFooter()
        }
    }

    private fun addRow(lst: MutableList<String>, backColor: Int, header: Boolean = false) {
        val grayMid = resources.getColor(R.color.black45, context?.theme)
        val tableArea: LinearLayout = binding.tableRoot
        val row: View = layoutInflater.inflate(R.layout.table_of_sizes_row, tableArea, false)
        row.setBackgroundColor(backColor)

        row.findViewById<TextView>(R.id.text1).text = lst[0]
        val t2 = row.findViewById<TextView>(R.id.text2)
        val t3 = row.findViewById<TextView>(R.id.text3)
        val t4 = row.findViewById<TextView>(R.id.text4)
        row.findViewById<View>(R.id.line).isVisible = !header

        var end = 0
        if (header) {
            for (i in 1..3) lst[i] = lst[i] + "\n${getString(R.string.cm)}"
            val sp = TypedValue.COMPLEX_UNIT_SP
            end = 2
            t2.setTextSize(sp, 13f)
            t3.setTextSize(sp, 13f)
            t4.setTextSize(sp, 13f)
        }
        t2.colorEnd(end, grayMid, lst[1])
        t3.colorEnd(end, grayMid, lst[2])
        t4.colorEnd(end, grayMid, lst[3])

        tableArea.addView(row)
    }

    private fun addFooter() {
        val tableArea: LinearLayout = binding.tableRoot
        val root: View = layoutInflater.inflate(R.layout.dialog_product_footer, tableArea, false)
        root.findViewById<TextView>(R.id.signUpFitting).paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tableArea.addView(root)
    }
}