package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.HashService
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.safeCall
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import es.unizar.urlshortener.core.usecases.QRGeneratorUseCase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

const val DEFAULT_WIDTH = 350
const val DEFAULT_HEIGHT = 350

interface QRCodeControllerInterface {
    /**
     * Generates a new QRCode and shows it on screen
     *
     * @param url the url
     */
    fun generateQRCode(url: String,
                       sponsor: String? = null,
                       request: HttpServletRequest) : ResponseEntity<ByteArrayResource>
}

@RestController
class QRGeneratorControllerImpl(
    private val qrGeneratorUseCase: QRGeneratorUseCase,
    private val createShortUrlUseCase: CreateShortUrlUseCase,
    private val hashService: HashService
) : QRCodeControllerInterface {
    /**
     * Generates a new QRCode and shows it on screen
     *
     * @param url the url
     */
    @PostMapping("/api/qr", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    override fun generateQRCode(url: String,
                                sponsor: String?,
                                request: HttpServletRequest) : ResponseEntity<ByteArrayResource> {
        val image: BufferedImage?

        // Checks if there is already a shortUrl for url given
        val hash = safeCall { hashService.hasUrl(url) }
        if (createShortUrlUseCase.findByKey(hash) != null) {
            image = qrGeneratorUseCase.generate(hash, DEFAULT_WIDTH, DEFAULT_HEIGHT)
        } else {
            val shortUrl: ShortUrl = createShortUrlUseCase.create(
                url = url,
                data = ShortUrlProperties(
                    ip = request.remoteAddr,
                    sponsor = sponsor
                )
            )
            image = qrGeneratorUseCase.generate(shortUrl.hash, DEFAULT_WIDTH, DEFAULT_HEIGHT)
        }
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        val imageBytes = outputStream.toByteArray()
        val resource = ByteArrayResource(imageBytes)
        val headers = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_TYPE, "image/png")
            add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr_code.png\"")
        }
        return ResponseEntity<ByteArrayResource>(resource, headers, HttpStatus.OK)
    }
}
