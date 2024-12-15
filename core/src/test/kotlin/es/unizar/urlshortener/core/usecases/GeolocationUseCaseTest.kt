package es.unizar.urlshortener.core.usecases

import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeolocationUseCaseTest {

    private val geolocationUseCase = spy(GeolocationUseCase())

    @Test
    fun `getGeolocation should return geolocation data for a valid IP`() {
        val ip = "134.201.250.155"
        val expectedGeolocation = Geolocation(
            latitude = 34.04563903808594,
            longitude = -118.24163818359375,
            city = "Los Angeles",
            country = "United States"
        )

        doReturn(expectedGeolocation).`when`(geolocationUseCase).fetchGeolocationFromApi(ip)

        val result = geolocationUseCase.getGeolocation(ip)

        assertEquals(expectedGeolocation, result)

        verify(geolocationUseCase, times(1)).fetchGeolocationFromApi(ip)

        val cachedResult = geolocationUseCase.getGeolocation(ip)
        assertEquals(expectedGeolocation, cachedResult)

        verify(geolocationUseCase, times(1)).fetchGeolocationFromApi(ip)
    }

    @Test
    fun `getGeolocation should throw exception for an invalid IP`() {
        val invalidIp = "999.999.999.999"

        assertFailsWith<IllegalArgumentException>("Invalid IP address: $invalidIp") {
            geolocationUseCase.getGeolocation(invalidIp)
        }

        verify(geolocationUseCase, never()).fetchGeolocationFromApi(invalidIp)
    }

    @Test
    fun `getGeolocation should cache results for repeated IPs`() {
        val ip = "8.8.8.8"
        val expectedGeolocation = Geolocation(
            latitude = 37.3860,
            longitude = -122.0838,
            city = "Mountain View",
            country = "United States"
        )

        doReturn(expectedGeolocation).`when`(geolocationUseCase).fetchGeolocationFromApi(ip)

        val result1 = geolocationUseCase.getGeolocation(ip)
        assertEquals(expectedGeolocation, result1)

        val result2 = geolocationUseCase.getGeolocation(ip)
        assertEquals(expectedGeolocation, result2)

        verify(geolocationUseCase, times(1)).fetchGeolocationFromApi(ip)
    }
}
