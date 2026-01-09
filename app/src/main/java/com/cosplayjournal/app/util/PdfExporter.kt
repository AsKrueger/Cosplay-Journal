package com.cosplayjournal.app.util

import android.content.Context
import android.os.Environment
import com.cosplayjournal.app.data.entity.Cosplay
import com.cosplayjournal.app.data.entity.HandmadePart
import com.cosplayjournal.app.data.entity.PurchasedItem
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    fun exportCosplayToPdf(
        context: Context,
        cosplay: Cosplay,
        handmadeParts: List<HandmadePart>,
        purchasedItems: List<PurchasedItem>
    ): File? {
        val fileName = "Cosplay_${cosplay.characterName.replace(" ", "_")}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            val writer = PdfWriter(FileOutputStream(file))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Title
            document.add(Paragraph("Cosplay Project Report").setBold().setFontSize(24f))
            document.add(Paragraph("Character: ${cosplay.characterName}"))
            document.add(Paragraph("Series: ${cosplay.series}"))
            document.add(Paragraph("Notes: ${cosplay.notes}"))

            document.add(Paragraph("\nHandmade Parts").setBold().setFontSize(18f))
            val handmadeTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 5f, 2f))).useAllAvailableWidth()
            handmadeTable.addHeaderCell("Part")
            handmadeTable.addHeaderCell("Materials")
            handmadeTable.addHeaderCell("Status")

            handmadeParts.forEach { part ->
                handmadeTable.addCell(part.name)
                handmadeTable.addCell(part.materials)
                handmadeTable.addCell(if (part.isFinished) "Finished" else "In Progress")
            }
            document.add(handmadeTable)

            document.add(Paragraph("\nPurchased Items").setBold().setFontSize(18f))
            val purchasedTable = Table(UnitValue.createPercentArray(floatArrayOf(3f, 4f, 3f))).useAllAvailableWidth()
            purchasedTable.addHeaderCell("Item")
            purchasedTable.addHeaderCell("Store")
            purchasedTable.addHeaderCell("Price")

            purchasedItems.forEach { item ->
                purchasedTable.addCell(item.name)
                purchasedTable.addCell(item.storeName)
                purchasedTable.addCell("$${item.price}")
            }
            document.add(purchasedTable)

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
