package com.aifranchise.ui.auth

import com.aifranchise.data.remote.ResultState
import com.aifranchise.data.repository.AuthRepository
import com.aifranchise.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var repository: AuthRepository

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel = LoginViewModel(repository)
    }


    @Test
    fun `login with valid credentials updates state to Success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val loginRequest = com.aifranchise.data.remote.LoginRequest(email, password)
        val userDto = com.aifranchise.data.remote.UserDto("id", "name", "owner", "outlet1")
        val mockResponse = com.aifranchise.data.remote.LoginResponse(
            token = "token", 
            user = userDto
        )
        
        `when`(repository.login(loginRequest)).thenReturn(flowOf(ResultState.Success(mockResponse)))

        // When
        viewModel.login(email, password)

        // Then
        assertEquals(ResultState.Success(mockResponse), viewModel.loginState.value)
    }

    @Test
    fun `login with empty credentials sets error state`() = runTest {
        viewModel.login("", "")
        assertTrue(viewModel.loginState.value is ResultState.Error)
        assertEquals("Email and Password required", (viewModel.loginState.value as ResultState.Error).exception.message)
    }
}
