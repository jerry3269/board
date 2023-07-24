package fastcampus.board.service;

import fastcampus.board.dto.ArticleCommentDto;
import fastcampus.board.repository.ArticleCommentRepository;
import fastcampus.board.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleCommentService {

    private final ArticleRepository articleRepository;
    private final ArticleCommentRepository articleCommentRepository;

    public List<ArticleCommentDto> searchArticleComments(Long articleId) {
        return List.of();
    }

    @Transactional
    public void saveArticleComment(ArticleCommentDto dto) {

    }

    @Transactional
    public void updateArticleComment(ArticleCommentDto dto) {

    }

    @Transactional
    public void deleteArticleComment(Long articleCommentId) {

    }
}
