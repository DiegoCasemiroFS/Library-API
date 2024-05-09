package br.com.DiegoCasemiroFS.LibraryAPI.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.DiegoCasemiroFS.LibraryAPI.dto.LoanDto;
import br.com.DiegoCasemiroFS.LibraryAPI.dto.ReturnedLoanDto;
import br.com.DiegoCasemiroFS.LibraryAPI.entity.Book;
import br.com.DiegoCasemiroFS.LibraryAPI.entity.Loan;
import br.com.DiegoCasemiroFS.LibraryAPI.exception.BusinessException;
import br.com.DiegoCasemiroFS.LibraryAPI.service.BookService;
import br.com.DiegoCasemiroFS.LibraryAPI.service.LoanService;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {

	static String LOAN_API = "/api/loans";
	@Autowired
	MockMvc mockMvc;
	@MockBean
	BookService bookService;
	@MockBean
	LoanService loanService;

	@Test
	@DisplayName("Deve realizar um empréstimo")
	public void createLoanTest() throws Exception {
		// Cenário
		LoanDto loanDto = LoanDto.builder()
				.isbn("123456789")
				.customer("Fulano").build();

		String json = new ObjectMapper().writeValueAsString(loanDto);

		Book book = Book.builder().id(1L).isbn("123456789").build();
		BDDMockito.given(bookService.getBookByIsbn("123456789")).willReturn(Optional.of(book));

		Loan loan = Loan.builder().id(1L).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
		BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		// Validação
		mockMvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.content().string("1"));
	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer o empréstimo de um livro inexistente")
	public void invalidIsbnCreateLoanTest() throws Exception {
		// Cenário
		LoanDto loanDto = LoanDto.builder()
				.isbn("123456789")
				.customer("Fulano")
				.build();

		String json = new ObjectMapper().writeValueAsString(loanDto);

		BDDMockito.given(bookService.getBookByIsbn("123456789")).willReturn(Optional.empty());

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		// Validação
		mockMvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("errors[0]")
						.value("Book not found for passed isbn"));
	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer o empréstimo de um livro já emprestado")
	public void loanedBookErrorOnCreateLoanTest() throws Exception {
		// Cenário
		LoanDto loanDto = LoanDto.builder().isbn("123456789").customer("Fulano").build();
		Book book = Book.builder().id(1L).isbn("123456789").build();

		String json = new ObjectMapper().writeValueAsString(loanDto);

		BDDMockito.given(bookService.getBookByIsbn("123456789")).willReturn(Optional.of(book));
		BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
				.willThrow(new BusinessException("Book already loaned"));

		// E#xecução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		// Validação
		mockMvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value("Book already loaned"));
	}

	@Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest() throws Exception {
        // Cenário
		Loan loan = Loan.builder().id(1L).build();
		ReturnedLoanDto returnedLoanDTO = ReturnedLoanDto.builder().returned(true).build();
		String json = new ObjectMapper().writeValueAsString(returnedLoanDTO);

		// Execução
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

		// Validação
        mockMvc.perform(MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

	@Test
    @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente")
    public void returnInexistentBookTest() throws Exception {
        ReturnedLoanDto returnedLoanDto = ReturnedLoanDto.builder().returned(true).build();
        String json = new ObjectMapper().writeValueAsString(returnedLoanDto);

        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}

