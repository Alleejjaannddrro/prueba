package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.Redirection
import es.unizar.urlshortener.core.ShortUrl
import es.unizar.urlshortener.core.ShortUrlProperties
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class CSVUploadUseCaseTest {

    private val createShortUrlUseCase: CreateShortUrlUseCase = mock(CreateShortUrlUseCase::class.java)
    private val csvUploadUseCase: CSVUploadUseCaseImpl = CSVUploadUseCaseImpl(createShortUrlUseCase)

    @Test
    fun `transform should short URLs from CSV file`() {
        val csvContent = "http://example.com\nhttp://test.com\n"
        val inputFile: MultipartFile = mock(MultipartFile::class.java)

        `when`(inputFile.inputStream).thenReturn(ByteArrayInputStream(csvContent.toByteArray()))

        val shortUrl1 = ShortUrl("hash1", Redirection("http://shortened.com/hash1", 301))
        val shortUrl2 = ShortUrl("hash2", Redirection("http://shortened.com/hash2", 302))

        `when`(createShortUrlUseCase.create("http://example.com", ShortUrlProperties())).thenReturn(shortUrl1)
        `when`(createShortUrlUseCase.create("http://test.com", ShortUrlProperties())).thenReturn(shortUrl2)

        val result: InputStream = csvUploadUseCase.transform(inputFile)

        val resultContent = result.bufferedReader().use { it.readText() }
        val expectedContent = "\"https://localhost:8080/hash1\"\n\"https://localhost:8080/hash2\"\n"
        assertEquals(expectedContent, resultContent)
    }

    @Test
    fun `transform should handle empty file`() {
        val emptyCsvContent = ""
        val inputFile: MultipartFile = mock(MultipartFile::class.java)

        `when`(inputFile.inputStream).thenReturn(ByteArrayInputStream(emptyCsvContent.toByteArray()))

        val result: InputStream = csvUploadUseCase.transform(inputFile)

        val resultContent = result.bufferedReader().use { it.readText() }
        assertEquals("", resultContent)
    }
}
