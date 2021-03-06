package fr.greta.java.game.domain;


import fr.greta.java.game.CustomList;
import fr.greta.java.game.domain.model.GameModel;
import fr.greta.java.game.domain.wrapper.GameModelWrapper;
import fr.greta.java.game.persistence.entity.GameEntity;
import fr.greta.java.game.persistence.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameService {

    public static final int NB_PAR_PAGE = 10;
    private GameColonne colonne;
    private boolean croissant;
    private Optional<SearchGame> search = Optional.empty();


    @Autowired
    private GameRepository repository;

    @Autowired
    private GameModelWrapper wrapperModel;

    //-----------------------------------------
    public GameService() {
        cleanColonnes();
    }

    public void cleanColonnes() {
        colonne = GameColonne.ID;
        croissant = true;
    }

    public void setColonne(String strColonne) {
        GameColonne colonne = GameColonne.valueOf(strColonne.toUpperCase());
        if (colonne == this.colonne) {
            this.croissant = !this.croissant;
        } else {
            this.colonne = colonne;
        }
    }

    public void setShearch(SearchGame search) {
        this.search = Optional.ofNullable(search);
    }

    private Sort.Direction sortDirection() {
        if (croissant) {
            return Sort.Direction.ASC;
        }
        return Sort.Direction.DESC;
    }

    //-----------------------------------------

    public CustomList<GameModel, Integer> findAllGameByPage(int page) {
        Sort sort = Sort.by(sortDirection(), colonne.colonneEntity());
        Pageable of = PageRequest.of(page, NB_PAR_PAGE, sort);
        if (search.isEmpty()) {
            return wrapperModel.fromEntities(repository.findAll(of));
        }
        Specification<GameEntity> spec = Specification.where(null);
        for (GameColonne colonne: search.get().getColonnes()) {
            spec = spec.or(like(colonne, search.get().getTexte()));
        }
        return wrapperModel.fromEntities(repository.findAll(spec, of));
    }

    private Specification<GameEntity> like(GameColonne colonne, String value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get(colonne.colonneEntity()), "%" + value + "%");
    }

    public GameModel findById(String id) {
        return repository.findById(id)
                .map(entity -> wrapperModel.fromEntity(entity))
                .orElse(null);
    }

    public GameModel save(GameModel model) {
        GameEntity entity = wrapperModel.toEntity(model);
        repository.save(entity);
        return wrapperModel.fromEntity(entity);
    }

    public void delete(GameModel model) {
        repository.delete(wrapperModel.toEntity(model));
    }
}
