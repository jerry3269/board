package fastcampus.board.repository.querydsl;

import com.querydsl.jpa.JPQLQuery;
import fastcampus.board.domain.Article;
import fastcampus.board.domain.QArticle;
import fastcampus.board.domain.QArticleHashtag;
import fastcampus.board.domain.QHashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Collection;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;

public class ArticleRepositoryCustomImpl extends QuerydslRepositorySupport implements ArticleRepositoryCustom {

    public ArticleRepositoryCustomImpl() {
        super(Article.class);
    }

}
