package br.com.DiegoCasemiroFS.LibraryAPI.controller;


import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.DiegoCasemiroFS.LibraryAPI.dto.BookDto;
import br.com.DiegoCasemiroFS.LibraryAPI.entity.Book;
import br.com.DiegoCasemiroFS.LibraryAPI.service.BookService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto create(@RequestBody @Valid BookDto dto) {
        Book book = modelMapper.map(dto, Book.class);
        Book bookSaved = bookService.save(book);
        BookDto bookDto = modelMapper.map(bookSaved, BookDto.class);

        return bookDto;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto getById(@PathVariable Long id) {
        return bookService.getById(id)
                .map(book -> modelMapper.map(book, BookDto.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable Long id, @RequestBody @Valid BookDto request) {
        return bookService.getById(id)
                .map(book -> {
                    book.setAuthor(request.getAuthor());
                    book.setTitle(request.getTitle());
                    book = bookService.update(book);
                    return modelMapper.map(book, BookDto.class);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book book = bookService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        bookService.delete(book);
    }

    @GetMapping
    public Page<BookDto> find(BookDto bookDto, Pageable pageRequest) {
        Book filter = modelMapper.map(bookDto, Book.class);
        Page<Book> result = bookService.find(filter, pageRequest);

        List<BookDto> list = bookService.find(filter, pageRequest)
                .stream()
                .map(entity -> modelMapper.map(entity, BookDto.class))
                .collect(Collectors.toList());

        var pageBook = new PageImpl<BookDto>(list, pageRequest, result.getTotalElements());

        return pageBook;
    }

    // TODO Pesquisa de Empréstimos
}
