package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/slider")
class SliderAPI (
        private val fileService: FileService,
        private val configScheduler: ConfigScheduler,
        private val sliderRepo: SliderRepo
) {

    @GetMapping
    fun getSliders(): List<Slider> {
        return sliderRepo.findByOrderByIndex()
    }

    @PostMapping("/upload")
    fun upload(@RequestParam("file") file: MultipartFile) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "admin only")

        val fileName = file.originalFilename?.toLowerCase() ?: throw ErrorCode(400, message = "unknown filename")
        if(!fileName.endsWith(".jpg") && !fileName.endsWith("jpeg"))
            throw ErrorCode(409, "only jpg")

        val suffix = UUID.randomUUID().toString().replace('-', '_')
        val finalFileName = "slider_$suffix.jpg"

        // save to hard drive
        val path = fileService.storeFile(file.bytes, "${configScheduler.get(Element.fileLocation)}/$finalFileName") ?: throw ErrorCode(500, "storing failed")

        // save to database
        val max = sliderRepo.findByOrderByIndex().maxBy { it.index }
        sliderRepo.save(Slider(0, (max?.index ?: 0) + 1, path, null, null, SliderDirection.values().first().code))
    }

    @PostMapping("/up/{id}")
    fun moveUp(@PathVariable id: Int) {
        move(id, up = true)
    }

    @PostMapping("/down/{id}")
    fun moveDown(@PathVariable id: Int) {
        move(id, up = false)
    }

    fun move(id: Int, up: Boolean) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "admin only")

        val sliders = sliderRepo.findByOrderByIndex()
        normalizeIndices(sliders)
        val index = sliders.indexOfFirst { it.id == id }
        if(index == -1)
            throw ErrorCode(404, "slider not found")
        when {
            up && index != sliders.size - 1 -> {
                sliders[index].index++
                sliders[index+1].index--
            }
            !up && index != 0 -> {
                sliders[index].index--
                sliders[index-1].index++
            }
        }
        sliderRepo.saveAll(sliders)
    }

    @PostMapping("/direction/{id}")
    fun updateDirection(@PathVariable id: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "admin only")

        val slider = sliderRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "slider not found")
        val directions = SliderDirection.values()
        val direction = directions.indexOfFirst { it.code == slider.direction }
        if(direction == -1)
            throw ErrorCode(500, "invalid direction")

        slider.direction = directions[(direction + 1) % directions.size].description
        sliderRepo.save(slider)
    }

    @PostMapping("/delete/{id}")
    fun deleteSlider(@PathVariable id: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "admin only")

        val slider = sliderRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "slider not found")
        fileService.deleteFile(slider.path)
        sliderRepo.deleteById(id)
    }

    private fun normalizeIndices(sliders: List<Slider>) {
        sliders.forEachIndexed { index, slider -> slider.index = index }
    }
}