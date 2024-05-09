package br.com.DiegoCasemiroFS.LibraryAPI.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.DiegoCasemiroFS.LibraryAPI.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);
}