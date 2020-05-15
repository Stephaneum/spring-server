package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.ImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

data class UpdateAreaText(val id: Int?, val text: String?)
data class TimestampResponse(val timestamp: Long)

@RestController
@RequestMapping("/api/groups")
class BoardAPI (
        private val imageService: ImageService,
        private val fileService: FileService,
        private val groupRepo: GroupRepo,
        private val boardAreaRepo: BoardAreaRepo,
        private val groupBoardRepo: GroupBoardRepo
) {

    @GetMapping("/{groupID}/board")
    fun getBoard(@PathVariable groupID: Int): List<BoardArea> {
        val board = getOrCreateBoard(groupID)
        return boardAreaRepo.findByBoard(board)
    }

    @GetMapping("/{groupID}/board/timestamp")
    fun getTimestamp(@PathVariable groupID: Int): TimestampResponse {
        return TimestampResponse(getOrCreateBoard(groupID).lastUpdate.time)
    }

    @PostMapping("/{groupID}/board/add-area-text")
    fun addAreaText(@PathVariable groupID: Int, @RequestBody request: UpdateAreaText) {
        val board = getOrCreateBoard(groupID)
        val area = BoardArea(0, board, 0, 0, 0, 0, request.text, null, AreaType.TEXT)
        updateTimestamp(board)
        boardAreaRepo.save(area)
    }

    @PostMapping("/board/update-area-text")
    fun updateAreaText(@RequestBody request: UpdateAreaText) {
        val area = boardAreaRepo.findByIdOrNull(request.id ?: 0) ?: throw ErrorCode(404, "Area not found")
        area.text = request.text
        updateTimestamp(area.board)
        boardAreaRepo.save(area)
    }

    @PostMapping("/{groupID}/board/add-area-file")
    fun addAreaFile(@PathVariable groupID: Int, @RequestParam("file") file: MultipartFile) {

        val user = Session.getUser()
        var fileName = file.originalFilename ?: throw ErrorCode(400, "Unknown filename")
        var contentType = file.contentType ?: throw ErrorCode(400, "Unknown content type")
        val areaType = when {
            fileService.isImage(contentType) -> AreaType.IMAGE
            fileService.isPDF(contentType) -> AreaType.PDF
            else -> throw ErrorCode(400, "only images or pdfs are allowed")
        }

        // ensure that the image is rotated properly
        var bytes = file.bytes
        if(areaType == AreaType.IMAGE) {
            val rotatedBytes = imageService.digestImageRotation(file.bytes)
            if (rotatedBytes != null) {
                bytes = rotatedBytes
                fileName = fileService.getPathWithNewExtension(fileName, "jpg")
                contentType = Files.probeContentType(Paths.get(fileName))
            }
        }

        val result = fileService.storeFileStephaneum(user, fileName, contentType, bytes, "Tafel", groupID, FileService.StoreMode.GROUP, lockedFolder = true)

        if(result !is File)
            throw ErrorCode(500, "File could not be saved")

        val board = getOrCreateBoard(groupID)
        val area = BoardArea(0, board, 0, 0, 0, 0, null, result, areaType)
        updateTimestamp(board)
        boardAreaRepo.save(area)
    }

    @PostMapping("/board/delete-area/{areaID}")
    fun deleteArea(@PathVariable areaID: Int) {
        val user = Session.getUser()
        val area = boardAreaRepo.findByIdOrNull(areaID) ?: throw ErrorCode(404, "Area not found")
        val file = area.file
        if(file != null) {
            fileService.deleteFileStephaneum(user, file) // the area will be deleted also
        } else {
            boardAreaRepo.delete(area)
        }
        updateTimestamp(area.board)
    }

    private fun getOrCreateBoard(groupID: Int): GroupBoard {
        val group = groupRepo.findByIdOrNull(groupID) ?: throw ErrorCode(404, "Group not found")
        return groupBoardRepo.findByGroup(group) ?: groupBoardRepo.save(GroupBoard(0, group))
    }

    private fun updateTimestamp(board: GroupBoard) {
        board.lastUpdate = now()
        groupBoardRepo.save(board)
    }
}