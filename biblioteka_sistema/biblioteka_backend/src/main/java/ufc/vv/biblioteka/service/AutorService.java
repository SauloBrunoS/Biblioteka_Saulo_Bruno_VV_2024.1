package ufc.vv.biblioteka.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.repository.AutorRepository;

@Service
public class AutorService {

    private final AutorRepository autorRepository;

    public AutorService(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    public Autor createAutor(Autor autor) {
        validarAutor(autor);

        if (autorRepository.existsByNomeCompleto(autor.getNomeCompleto())) {
            throw new DuplicateKeyException("Um autor com este nome já está cadastrado.");
        }

        return autorRepository.save(autor);
    }

    public Autor updateAutor(int id, Autor updatedAutor) {
        Autor existingAutor = autorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Autor não encontrado"));

        validarAutor(updatedAutor);

        existingAutor.setDataNascimento(updatedAutor.getDataNascimento());
        existingAutor.setNacionalidade(updatedAutor.getNacionalidade());
        existingAutor.setNomeCompleto(updatedAutor.getNomeCompleto());

        return autorRepository.save(existingAutor);
    }

    public void deleteAutorById(int id) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Autor não encontrado"));

        // Verifica se o autor tem livros associados
        if (autor.getLivros() != null && !autor.getLivros().isEmpty()) {
            throw new DataIntegrityViolationException("Não é possível excluir o autor porque ele tem livros associados.");
        }

        autorRepository.delete(autor);
    }

    private void validarAutor(Autor autor) {
        if (autor == null) {
            throw new IllegalArgumentException("Autor não pode ser nulo.");
        }
        if (autor.getNomeCompleto() == null || autor.getNomeCompleto().isEmpty() ||
                autor.getDataNascimento() == null ||
                // talvez não precise de empty
                autor.getNacionalidade() == null || autor.getNacionalidade().toString().isEmpty()) {
            throw new IllegalArgumentException("Todos os campos obrigatórios devem ser preenchidos.");
        }
    }

}
