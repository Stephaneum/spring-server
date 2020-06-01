package de.stephaneum.spring.rest

import de.stephaneum.spring.backup.BackupService
import de.stephaneum.spring.backup.ModuleType
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.security.CryptoService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

object InitRequest {
    data class Paths(val fileLocation: String, val backupLocation: String)
    data class NewInstance(val fileLocation: String, val backupLocation: String, val firstName: String, val lastName: String, val email: String, val password: String, val gender: Int)
}

@RestController
@RequestMapping("/api/init")
class InitAPI (
        private val globalStateService: GlobalStateService,
        private val codeService: CodeService,
        private val cryptoService: CryptoService,
        private val backupService: BackupService,
        private val fileService: FileService,
        private val storageCalculator: StorageCalculator,
        private val userRepo: UserRepo,
        private val menuRepo: MenuRepo,
        private val configScheduler: ConfigScheduler
) {

    private var fileLocation: String? = null
    private var backupLocation: String? = null

    @PostMapping("/paths")
    fun setPaths(@RequestBody request: InitRequest.Paths) {
        fileLocation = request.fileLocation
        backupLocation = request.backupLocation
    }

    @PostMapping("/load-backup")
    fun loadBackup(@RequestParam("file") file: MultipartFile) {

        if(globalStateService.state != GlobalState.NEED_INIT)
            throw ErrorCode(409, "already initialized")

        val currFileLocation = fileLocation ?: throw ErrorCode(412, "file location not set")
        val currBackupLocation = backupLocation ?: throw ErrorCode(412, "backup location not set")
        val fileName = file.originalFilename ?: throw ErrorCode(417, "unknown file name")

        if(!fileName.toLowerCase().endsWith(".zip"))
            throw ErrorCode(418, "only zip files")

        configScheduler.initialize(currFileLocation, currBackupLocation, 0)
        fileService.storeFile(file.bytes, "$currBackupLocation/${ModuleType.HOMEPAGE.code}/$fileName") ?: throw ErrorCode(500, "could not save file")
        backupService.restore(ModuleType.HOMEPAGE, fileName) // global state will be updated there
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    @PostMapping("/new-instance")
    fun newInstance(@RequestBody request: InitRequest.NewInstance) {

        if(globalStateService.state != GlobalState.NEED_INIT)
            throw ErrorCode(409, "already initialized")

        val user = User(
                code = codeService.generateCode(ROLE_ADMIN),
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                password = cryptoService.hashPassword(request.password),
                gender = request.gender,
                storage = storageCalculator.convertToBytes(1, SizeType.GB).toInt()
        )

        userRepo.save(user)
        val menu = menuRepo.save(Menu(name = "Home", priority = 10))
        configScheduler.initialize(request.fileLocation, request.backupLocation, menu.id)
        globalStateService.state = GlobalState.OK
    }
}