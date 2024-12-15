package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.usecases.CSVUploadUseCase
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.ByteArrayInputStream

@WebMvcTest
@ContextConfiguration(
    classes = [
        CSVUploadControllerImpl::class
    ]
)
class CSVUploadControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var csvUploadUseCase: CSVUploadUseCase

    @Test
    fun `should return shortened URLs CSV when a valid file is uploaded`() {
        // Arrange
        val originalCsv = "https://example.com\nhttps://another.com"
        val shortenedCsvContent = "http://localhost:8080/585592c8\nhttp://localhost:8080/cbbd73da"
        val mockFile = MockMultipartFile(
            "file",
            "urls.csv",
            MediaType.TEXT_PLAIN_VALUE,
            originalCsv.toByteArray()
        )
        val shortenedCsvStream = ByteArrayInputStream(shortenedCsvContent.toByteArray())

        Mockito.`when`(csvUploadUseCase.transform(mockFile)).thenReturn(shortenedCsvStream)

        // Act & Assert
        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/csv")
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.parseMediaType("text/csv"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType("text/csv"))
            .andExpect(MockMvcResultMatchers.header().string("Content-Disposition",
                "attachment; filename=shortened_urls.csv"))
            .andExpect(MockMvcResultMatchers.content().string(shortenedCsvContent))

        // Verify that the use case was called once
        Mockito.verify(csvUploadUseCase, Mockito.times(1)).transform(mockFile)
    }

    @Test
    fun `should return 400 Bad Request when no file is provided`() {
        // Act & Assert
        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/api/csv")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.parseMediaType("text/csv"))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
