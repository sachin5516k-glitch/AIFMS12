package com.aifranchise.ui.sales

import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.remote.SalesResponse
import com.aifranchise.data.repository.SalesRepository
import com.aifranchise.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SalesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var repository: SalesRepository

    private lateinit var viewModel: SalesViewModel

    @Before
    fun setup() {
        viewModel = SalesViewModel(repository)
    }

    @Test
    fun `submitSales with valid data updates state to Success`() = runTest {
        // Given
        val amount = "5000"
        val paymentMode = "Cash"
        val mockResponse = SalesResponse("id", "outlet1", 5000.0, "Cash", "url", 0, "pending")
        
        // Mocking the repository to return success flow when ANY SalesRequest is passed
        // In a real test, use ArgumentCaptor to verify the request fields
        `when`(repository.submitSales(org.mockito.ArgumentMatchers.any())).thenReturn(flowOf(ResultState.Success(mockResponse)))

        // When
        viewModel.submitSales("outlet1", amount, paymentMode, "url")

        // Then
        // Verify state is Success
        assertEquals(ResultState.Success(mockResponse), viewModel.salesState.value)
    }

    @Test
    fun `submitSales with empty amount sets error`() = runTest {
        viewModel.submitSales("outlet1", "", "Cash", "url")
        // Verify state is Error
        assert(viewModel.salesState.value is ResultState.Error)
    }
}
