package de.stephaneum.spring

import org.springframework.boot.runApplication
import java.awt.BorderLayout
import java.awt.Font
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.swing.*

object Gui {

    private val regexColor = Regex("\u001B\\[[;\\d]*m")

    fun open(args: Array<String>) {
        val frame = JFrame("Stephaneum-Server")
        frame.setSize(500, 400)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BorderLayout()

        val title = JLabel("Stephaneum-Server")
        title.border = BorderFactory.createCompoundBorder(title.border, BorderFactory.createEmptyBorder(5, 5, 5, 5))
        title.font = Font(Font.MONOSPACED, Font.BOLD, 20)
        frame.add(title, BorderLayout.PAGE_START)

        val textArea = JTextArea()
        textArea.border = BorderFactory.createCompoundBorder(textArea.border, BorderFactory.createEmptyBorder(5, 5, 5, 5))
        textArea.font = Font(Font.MONOSPACED, Font.PLAIN, 14)
        frame.add(JScrollPane(textArea), BorderLayout.CENTER)

        val consoleOutput = ByteArrayOutputStream()
        System.setOut(PrintStream(consoleOutput))
        Timer(1000) {
            textArea.text = consoleOutput.toString().replace(regexColor, "")
        }.start()

        frame.isVisible = true
        runApplication<BackendApplication>(*args)
    }
}