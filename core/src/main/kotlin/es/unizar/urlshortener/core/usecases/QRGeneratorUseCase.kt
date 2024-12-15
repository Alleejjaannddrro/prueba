@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

const val DEFAULT_MAXIMUM_SIZE = 100L

/**
 * Given an url returns a QR code with the url shortened.
 */
interface QRGeneratorUseCase {
    /**
     * Generates a new QRCode
     *
     * @param hash content of QR
     * @param width width of QR (default 350px)
     * @param height height of QR (default 350px)
     *
     * @return QRCode generated
     */
    fun generate(hash: String, width: Int? = 350, height: Int? = 350): BufferedImage
}

/**
 * Implementation of [QRGeneratorUseCase]
 */
@Service
class QRGeneratorUseCaseImpl: QRGeneratorUseCase {
    // Caffeine cache instance (hash as key)
    val qrCache: Cache<String, BufferedImage> = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)   // Expire entries after 1 hour
        .maximumSize(DEFAULT_MAXIMUM_SIZE)              // Max size
        .build()

    /**
     * Checks if QR is in cache and retrieves it. If not, calls [createQRCode]
     * and saves new QR in cache (if there is space)
     *
     * @param hash content of QR
     * @param width width of QR (default 350px)
     * @param height height of QR (default 350px)
     *
     * @return QRCode generated
     */
    override fun generate(hash: String, width: Int?, height: Int?): BufferedImage {
        return qrCache.get(hash) {
            createQRCode(hash, width!!, height!!)
        }
    }
    /**
     * Generates a new QRCode
     *
     * @param hash content of QR
     * @param width width of QR (default 350px)
     * @param height height of QR (default 350px)
     *
     * @return QRCode generated
     */
    private fun createQRCode(hash: String, width: Int?, height: Int?): BufferedImage {
        val bitMatrix = QRCodeWriter().encode(
            "https://localhost:8080/$hash",
            BarcodeFormat.QR_CODE,
            width!!,
            height!!
        )
        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }
}
