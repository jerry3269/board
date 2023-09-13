package fastcampus.board.repository.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import fastcampus.board.domain.*;
import fastcampus.board.dto.query.ArticleHashtagDto;
import fastcampus.board.dto.query.ArticleSelectDto;
import fastcampus.board.dto.query.QArticleHashtagDto;
import fastcampus.board.dto.query.QArticleSelectDto;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.*;
import java.util.stream.Collectors;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.springframework.data.domain.Sort.*;

public class ArticleHashtagRepositoryImpl extends QuerydslRepositorySupport implements ArticleHashtagRepositoryCustom {
    /**
     * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
     *
     * @param domainClass must not be {@literal null}.
     */
    public ArticleHashtagRepositoryImpl() {
        super(ArticleHashtag.class);
    }

    @Override
    public Page<ArticleSelectDto> findByHashtagNames(Collection<String> hashtagNames, Pageable pageable) {

        QArticle article = QArticle.article;
        QArticleHashtag articleHashtag = QArticleHashtag.articleHashtag;
        QHashtag hashtag = QHashtag.hashtag;

        Pageable renewalPageable = getRenewalPageable(pageable);

        JPQLQuery<ArticleSelectDto> query = getQuerydsl().createQuery()
                .select(new QArticleSelectDto(
                        article.id,
                        article.userAccount,
                        article.title,
                        article.content,
                        article.createdAt,
                        article.createdBy,
                        article.modifiedAt,
                        article.modifiedBy
                )).distinct()
                .from(articleHashtag)
                .innerJoin(articleHashtag.article, article)
                .innerJoin(articleHashtag.hashtag, hashtag)
                .where(hashtag.hashtagName.in(hashtagNames));

        List<ArticleSelectDto> fetch = getQuerydsl().applyPagination(renewalPageable, query).fetch();

        long count = getQuerydsl().createQuery()
                .select(article.id).distinct()
                .from(articleHashtag)
                .innerJoin(articleHashtag.article, article)
                .innerJoin(articleHashtag.hashtag, hashtag)
                .where(hashtag.hashtagName.in(hashtagNames))
                .fetchCount();

        return new PageImpl<>(fetch, pageable, count);
    }

    private static Pageable getRenewalPageable(Pageable pageable) {
        List<Order> orders = new ArrayList<>();
        pageable.getSort().stream()
                .forEach(order -> orders.add(new Order(order.getDirection(), "article." + order.getProperty())));
        Sort newSort = Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), newSort);
    }

    @Override
    public Set<ArticleHashtag> findByArticleId(Long articleId) {
        QArticle article = QArticle.article;
        QArticleHashtag articleHashtag = QArticleHashtag.articleHashtag;
        QHashtag hashtag = QHashtag.hashtag;

        List<ArticleHashtag> articleHashtags = getQuerydsl().createQuery()
                .select(articleHashtag)
                .from(articleHashtag)
                .innerJoin(articleHashtag.hashtag, hashtag).fetchJoin()
                .where(articleHashtag.article.id.eq(articleId))
                .fetch();

        return articleHashtags.stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<ArticleHashtagDto> findDtoByArticleIds(Collection<Long> articleIds) {
        QArticle article = QArticle.article;
        QArticleHashtag articleHashtag = QArticleHashtag.articleHashtag;
        QHashtag hashtag = QHashtag.hashtag;

        return getQuerydsl().createQuery()
                .select(new QArticleHashtagDto(
                        article.id,
                        hashtag.hashtagName
                ))
                .from(articleHashtag)
                .innerJoin(articleHashtag.article, article)
                .innerJoin(articleHashtag.hashtag, hashtag)
                .where(article.id.in(articleIds))
                .fetch()
                .stream().collect(Collectors.toUnmodifiableSet());
    }
}
