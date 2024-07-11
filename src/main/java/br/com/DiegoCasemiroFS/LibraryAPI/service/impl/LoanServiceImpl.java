package br.com.DiegoCasemiroFS.LibraryAPI.service.impl;
import java.util.Optional;

import br.com.DiegoCasemiroFS.LibraryAPI.entity.Loan;
import br.com.DiegoCasemiroFS.LibraryAPI.exception.BusinessException;
import br.com.DiegoCasemiroFS.LibraryAPI.repository.LoanRepository;
import br.com.DiegoCasemiroFS.LibraryAPI.service.LoanService;
import org.springframework.stereotype.Service;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository loanRepository;

    public LoanServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Loan save(Loan loan) {
        if( loanRepository.existsByBookAndNotReturned(loan.getBook()) ){
            throw new BusinessException("Book already loaned");
        }
        
        return loanRepository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return loanRepository.save(loan);
    }
}