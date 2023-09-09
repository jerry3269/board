package fastcampus.board.repository.querydsl;

import com.querydsl.jpa.JPQLQuery;
import fastcampus.board.domain.*;
import fastcampus.board.dto.query.ArticleSelectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ArticleHashtagRepositoryCustom {

    Page<ArticleSelectDto> findByHashtagNames(Collection<String> hashtagNames, Pageable pageable);

    Set<ArticleHashtag> findByArticleId(Long ArticleId);
}
