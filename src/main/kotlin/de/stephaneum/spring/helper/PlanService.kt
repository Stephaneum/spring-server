package de.stephaneum.spring.helper

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException

@Service
class PlanService {

    private val days = arrayOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    private val months = arrayOf("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember")

    fun resolveDate(file: File): String? {

        var result: String? = null
        try {
            val document = PDDocument.load(file)
            val pdfStripper = PDFTextStripper()
            val text = pdfStripper.getText(document)

            val splitted = text.split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (line in splitted) {
                var weekDay: String?
                var month: String?
                var day: String? = null
                var year: String? = null

                for (i in 0 until days.size) {

                    val occurenceAt = line.indexOf(days[i])
                    if (occurenceAt != -1) {
                        //Wochentag gefunden
                        weekDay = days[i]

                        for (x in 0 until months.size) {

                            val occurenceMonatAt = line.indexOf(months[x])
                            if (occurenceMonatAt != -1) {

                                //Monat gefunden

                                month = if (x >= 9) (x + 1).toString() else "0" + (x + 1)

                                val indexTagBegin = occurenceAt + days[i].length + 2
                                val indexTagEnd = occurenceMonatAt - 2

                                if (indexTagEnd > indexTagBegin && indexTagEnd < line.length)
                                    day = line.substring(indexTagBegin, indexTagEnd)

                                if (day!!.length == 1)
                                    day = "0$day"

                                val indexJahrBegin = occurenceMonatAt + months[x].length + 1
                                val indexJahrEnd = occurenceMonatAt + months[x].length + 1 + 4

                                if (indexJahrEnd > indexJahrBegin && indexJahrEnd < line.length)
                                    year = line.substring(indexJahrBegin, indexJahrEnd)

                                if (year != null) {
                                    result = "$weekDay, $day.$month.$year"
                                }

                                break
                            }
                        }
                        break
                    }
                }

                if (result != null)
                    break
            }

            document.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }
}