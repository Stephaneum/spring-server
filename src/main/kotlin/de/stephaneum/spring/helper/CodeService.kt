package de.stephaneum.spring.helper

import de.stephaneum.spring.database.Code
import de.stephaneum.spring.database.CodeRepo
import org.springframework.stereotype.Service

@Service
class CodeService (
        private val codeRepo: CodeRepo
) {

    private val codePool = listOf('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z')
    private val codeLength = 10

    @ExperimentalStdlibApi
    fun generateCodes(role: Int, count: Int): List<Code> {
        val codes = codeRepo.findAll().map { code -> code.code }.toMutableList()
        val adding = List(count) {
            var currCode: String
            do {
                currCode = getRandomCode()
            } while(codes.any { code -> code == currCode })
            codes.add(currCode)
            Code(0, currCode, role, false)
        }
        return codeRepo.saveAll(adding).toList()
    }

    @ExperimentalStdlibApi
    fun generateCode(role: Int, existing: List<Code>? = null): Code {
        val codes = existing ?: codeRepo.findAll()
        var next: String
        do {
            next = getRandomCode()
        } while(codes.any {it.code == next})

        return codeRepo.save(Code(0, next, role, false))
    }

    @ExperimentalStdlibApi
    private fun getRandomCode(): String {
        return (CharArray(codeLength) { codePool.random() }).concatToString()
    }
}