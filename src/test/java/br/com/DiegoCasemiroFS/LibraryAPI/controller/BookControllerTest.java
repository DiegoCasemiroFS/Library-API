package br.com.DiegoCasemiroFS.LibraryAPI.controller;

import java.util.Arrays;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.DiegoCasemiroFS.LibraryAPI.dto.BookDto;
import br.com.DiegoCasemiroFS.LibraryAPI.entity.Book;
import br.com.DiegoCasemiroFS.LibraryAPI.exception.BusinessException;
import br.com.DiegoCasemiroFS.LibraryAPI.service.BookService;

@AutoConfigureMockMvc
@ActiveProfiles("test")
// @ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = BookController.class)
public class BookControllerTest {

	static String BOOK_API = "/api/books";

	@Autowired
	MockMvc mvc;

	@MockBean
	BookService bookService;

	@Test
	@DisplayName("Deve criar um livro com sucesso")
	public void createBookTest() throws Exception {
		// Cenário
		BookDto dto = createBookDto();

		Book savedBook = Book.builder()
				.id(1L)
				.title("Titulo")
				.author("Autor")
				.isbn("12345678")
				.build();

		BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);

		String json = new ObjectMapper().writeValueAsString(dto);

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		// Validação
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isCreated())
				// .andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("id").value(1L))
				.andExpect(MockMvcResultMatchers.jsonPath("title").value(dto.getTitle()))
				.andExpect(MockMvcResultMatchers.jsonPath("author").value(dto.getAuthor()))
				.andExpect(MockMvcResultMatchers.jsonPath("isbn").value(dto.getIsbn()));
	}

	@Test
	@DisplayName("Deve lançar um erro de validaçao quando nao houver dados suficientes para a criaçao do livro")
	public void createInvalidBookTest() throws Exception {
		// Cenário
		String json = new ObjectMapper().writeValueAsString(new BookDto());

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		// Validação
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(3)));
	}

	@Test
	@DisplayName("Deve lançar um erro ao tentar cadastrar um livro com isbn já utilizado por outro")
	public void createBookWithDuplicatedIsbn() throws Exception {
		// Cenário
		BookDto bookDto = createBookDto();

		String json = new ObjectMapper().writeValueAsString(bookDto);

		String errorMessage = "Isbn já cadastrado.";

		BDDMockito.given(bookService.save(Mockito.any(Book.class)))
				.willThrow(new BusinessException(errorMessage));

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		// Validação
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value(errorMessage));

	}

	@Test
	@DisplayName("Deve obter informações de um livro")
	public void getBookDetails() throws Exception {
		// Cenário
		Long id = 1l;
		Book book = Book.builder()
				.id(id)
				.title("Titulo")
				.author("Autor")
				.isbn("123456789")
				.build();

		BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);

		// Validação
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
				.andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("title").value(book.getTitle()))
				.andExpect(MockMvcResultMatchers.jsonPath("author").value(book.getAuthor()))
				.andExpect(MockMvcResultMatchers.jsonPath("isbn").value(book.getIsbn()));
	}

	@Test
	@DisplayName("Deve retornar Resource Not Found quando o livro procurado não existir")
	public void bookNotFound() throws Exception {
		// Cenario
		BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + 1L))
				.accept(MediaType.APPLICATION_JSON);

		// Validação
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBook() throws Exception {
		// Cenário
		BDDMockito.given(bookService.getById(Mockito.anyLong()))
				.willReturn(Optional.of(Book.builder().id(1L).build()));

		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1L));

		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}

	@Test
	@DisplayName("Deve retornar Resource Not Found quando não encontrar um livro para deletar")
	public void deleteInexistentBook() throws Exception {
		BDDMockito.given(bookService.getById(Mockito.anyLong()))
				.willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1L));

		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBook() throws Exception {
		Long id = 1L;
		String json = new ObjectMapper().writeValueAsString(createBookDto());

		Book updatingBook = Book.builder()
				.id(1L)
				.title("some title")
				.author("some author")
				.isbn("321")
				.build();

		BDDMockito.given(bookService.getById(id))
				.willReturn(Optional.of(updatingBook));

		Book updatedBook = Book.builder()
				.id(id)
				.title("Titulo")
				.author("Autor")
				.isbn("123456789")
				.build();

		BDDMockito.given(bookService.update(updatingBook))
				.willReturn(updatedBook);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + id))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("title").value(createBookDto().getTitle()))
				.andExpect(MockMvcResultMatchers.jsonPath("author").value(createBookDto().getAuthor()))
				.andExpect(MockMvcResultMatchers.jsonPath("isbn").value(123456789));
	}

	@Test
	@DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
	public void updateBookInexistent() throws Exception {
		String json = new ObjectMapper().writeValueAsString(createBookDto());

		BDDMockito.given(bookService.getById(Mockito.anyLong()))
				.willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1L))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	@DisplayName("Deve filtrar livros")
	public void findBooksTest() throws Exception {
		// Cenário
		Long id = 1L;
		Book book = Book.builder()
				.id(id)
				.title(createBookDto().getTitle())
				.author(createBookDto().getAuthor())
				.isbn(createBookDto().getIsbn())
				.build();

		BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

		// Execução
		String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		// Validação
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("pageable.pageSize").value(100))
				.andExpect(MockMvcResultMatchers.jsonPath("pageable.pageNumber").value(0));
	}

	private BookDto createBookDto() {
		return BookDto.builder()
				.title("Titulo")
				.author("Autor")
				.isbn("12345678")
				.build();
	}

}
