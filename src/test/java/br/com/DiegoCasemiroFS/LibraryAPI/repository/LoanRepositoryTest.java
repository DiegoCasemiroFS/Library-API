package br.com.DiegoCasemiroFS.LibraryAPI.repository;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.DiegoCasemiroFS.LibraryAPI.entity.Book;
import br.com.DiegoCasemiroFS.LibraryAPI.entity.Loan;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class LoanRepositoryTest {

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    TestEntityManager testEntityManager;

    @Test
    @DisplayName("Deve verificar se existe um emprestimo não retornado para o livro passado.")
    public void existsByBookAndNotReturnedTest() {
        // Cenário
        Book book = BookRepositoryTest.createNewBook();
        testEntityManager.persist(book);

        Loan loan = Loan.builder()
                .book(book)
                .customer("Fulano")
                .loanDate(LocalDate.now())
                .build();
        testEntityManager.persist(loan);

        // Execução
        boolean exists = loanRepository.existsByBookAndNotReturned(book);

        // Validação
        Assertions.assertThat(exists).isTrue();

    }
}

