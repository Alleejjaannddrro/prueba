package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.CSVUploadUseCase
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

interface CSVUploadController {
    /**
     * Enable users to upload a CSV of URLs for shortening
     * and return a CSV of shortened URLs
     */
    fun upload(file: MultipartFile): ResponseEntity<InputStreamResource>
}

@RestController
class CSVUploadControllerImpl(
    private val csvUploadUseCase : CSVUploadUseCase
) : CSVUploadController {
    /**
     * Enable users to upload a CSV of URLs for shortening
     * and return a CSV of shortened URLs
     */
    @PostMapping("/api/csv", consumes = ["multipart/form-data"], produces = ["text/csv"])
    override fun upload(@RequestPart("file") file: MultipartFile): ResponseEntity<InputStreamResource> {
        val shortUrlCsv = csvUploadUseCase.transform(file)
        val headers = HttpHeaders().apply {
            add("Content-Disposition", "attachment; filename=shortened_urls.csv")
        }
        return ResponseEntity
            .ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(InputStreamResource(shortUrlCsv))
    }
}
