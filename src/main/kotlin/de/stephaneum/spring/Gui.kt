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
        println("starting GUI")

        val frame = JFrame("Stephaneum-Server")
        frame.setSize(800, 500)
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
        textArea.isEditable = false
        frame.add(JScrollPane(textArea), BorderLayout.CENTER)

        val consoleOutput = ByteArrayOutputStream()
        System.setOut(PrintStream(consoleOutput))
        Timer(500) {
            val newText = consoleOutput.toString().replace(regexColor, "")
            if(textArea.text.length != newText.length)
                textArea.text = newText
        }.start()

        frame.isVisible = true
        frame.extendedState = frame.extendedState or JFrame.MAXIMIZED_BOTH

        // start server
        runApplication<BackendApplication>(*args)
    }
}