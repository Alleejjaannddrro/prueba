package es.unizar.urlshortener.core.usecases

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class QRGeneratorUseCaseTest {

    @Test
    fun `should generate QR code for valid URL`() {
        val url = "http://example.com/"

        val qrCodeImage = QRGeneratorUseCaseImpl().generate(url)

        assertNotNull(qrCodeImage)
        assertEquals(350, qrCodeImage.width)
        assertEquals(350, qrCodeImage.height)
    }
}
