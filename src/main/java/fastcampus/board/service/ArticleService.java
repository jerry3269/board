package fastcampus.board.service;

import fastcampus.board.domain.type.SearchType;
import fastcampus.board.dto.ArticleDto;
import fastcampus.board.dto.ArticleWithCommentsDto;
import fastcampus.board.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        return Page.empty();
    }

    public ArticleWithCommentsDto getArticle(Long articleId) {
        return null;
    }

    @Transactional
    public void saveArticle(ArticleDto dto) {

    }
    @Transactional
    public void updateArticle(ArticleDto dto) {

    }
    @Transactional
    public void deleteArticle(long articleId) {

    }

}
