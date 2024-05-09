package br.com.DiegoCasemiroFS.LibraryAPI.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.DiegoCasemiroFS.LibraryAPI.entity.Loan;

@Service
public interface LoanService {
    
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);
    
}
