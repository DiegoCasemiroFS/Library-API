package br.com.DiegoCasemiroFS.LibraryAPI.controller;

import br.com.DiegoCasemiroFS.LibraryAPI.dto.LoanDto;
import br.com.DiegoCasemiroFS.LibraryAPI.dto.ReturnedLoanDto;
import br.com.DiegoCasemiroFS.LibraryAPI.entity.Book;
import br.com.DiegoCasemiroFS.LibraryAPI.entity.Loan;
import br.com.DiegoCasemiroFS.LibraryAPI.service.BookService;
import br.com.DiegoCasemiroFS.LibraryAPI.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loans")
public class LoanController {

    private final BookService bookService;
    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDto loanDTO) {
        Book book = bookService.getBookByIsbn(loanDTO.getIsbn())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));

        Loan entity = Loan.builder()
                .book(book)
                .customer(loanDTO.getCustomer())
                .loanDate(LocalDate.now())
                .build();
        entity = loanService.save(entity);

        return entity.getId();
    }

    @PatchMapping("/{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDto returnedLoanDto) {
        Loan loan = loanService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(returnedLoanDto.getReturned());
        loanService.update(loan);
    }
}
