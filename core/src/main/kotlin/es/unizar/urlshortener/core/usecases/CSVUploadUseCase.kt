@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.ByteArrayOutputStream
import es.unizar.urlshortener.core.*
import org.springframework.stereotype.Service
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import org.springframework.web.multipart.MultipartFile


interface CSVUploadUseCase {
    /**
     * Shorts all URLs in file
     *
     * @param file the CSV file with URLs to short
     */
    fun transform(file: MultipartFile) : InputStream
}

/**
 * Implementation of [CSVUploadUseCase]
 */
@Service
class CSVUploadUseCaseImpl(
    private val createShortUrlUseCase: CreateShortUrlUseCase
) : CSVUploadUseCase {
    /**
     * handles writer in transformation
     *
     * @param file the CSV file with URLs to short
     * @param writer the CSVWriter
     */
    private fun handleWriter(file: MultipartFile, writer: CSVWriter) {
        CSVReader(InputStreamReader(file.inputStream)).use { reader ->
            var line = reader.readNext()
            while (line != null) {
                line.filter { it.isNotBlank() }.forEach { cell -> handleCell(writer, cell) }
                line = reader.readNext()
            }
        }
    }
    /**
     * handles a cell of the CSVWriter
     *
     * @param writer the CSVWriter
     * @param cell the individual cell to handle
     */
    private fun handleCell(writer: CSVWriter, cell: String) {
        val urls = cell.split(",").map { it.trim() }
        urls.forEach { url ->
            val shortUrl = createShortUrlUseCase.create(url, ShortUrlProperties())
            val hash = shortUrl.hash
            writer.writeNext(
                arrayOf(
                    "https://localhost:8080/$hash",
                )
            )
        }
    }
    /**
     * Shorts all URLs in file
     *
     * @param file the CSV file with URLs to short
     */
    override fun transform(file: MultipartFile): InputStream {
        val outputStream = ByteArrayOutputStream()
        CSVWriter(OutputStreamWriter(outputStream)).use { writer -> handleWriter(file, writer) }
        return outputStream.toByteArray().inputStream()
    }
}
