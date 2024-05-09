package br.com.DiegoCasemiroFS.LibraryAPI.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import br.com.DiegoCasemiroFS.LibraryAPI.entity.Book;

@DataJpaTest
@ActiveProfiles("test")
// @ExtendWith(SpringExtension.class)
public class BookRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnTrueWhenIsbnExistsTest() {
        // Cenário
        String isbn = "123456789";

        Book book = createNewBook();

        testEntityManager.persist(book);

        // Execução
        boolean exists = bookRepository.existsByIsbn(isbn);

        // Validação
        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um livro na base com o isbn informado")
    public void returnFalseWhenIsbnDoesnotExistsTest() {
        String isbn = "123456789";

        boolean exists = bookRepository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void findByIdTest() {
        Book book = createNewBook();
        testEntityManager.persist(book);

        Optional<Book> foundBook = bookRepository.findById(book.getId());

        Assertions.assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        Book book = createNewBook();

        Book savedBook = bookRepository.save(book);

        Assertions.assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() {
        Book book = createNewBook();

        testEntityManager.persist(book);

        Book foundBook = testEntityManager.find(Book.class, book.getId());

        bookRepository.delete(foundBook);
        Book deletedBook = testEntityManager.find(Book.class, book.getId());

        Assertions.assertThat(deletedBook).isNull();
    }

    public static Book createNewBook() {
        return Book.builder()
                .title("Titulo")
                .author("Autor")
                .isbn("123456789")
                .build();
    }

}