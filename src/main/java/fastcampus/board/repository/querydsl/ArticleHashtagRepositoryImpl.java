package fastcampus.board.repository.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import fastcampus.board.domain.*;
import fastcampus.board.dto.query.ArticleSelectDto;
import fastcampus.board.dto.query.QArticleSelectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.jpa.JPAExpressions.select;

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

        JPQLQuery<ArticleSelectDto> query = from(articleHashtag)
                .innerJoin(articleHashtag.article, article)
                .innerJoin(articleHashtag.hashtag, hashtag)
                .where(hashtag.hashtagName.in(hashtagNames))
                .select(new QArticleSelectDto(
                        article.id,
                        article.userAccount,
                        article.title,
                        article.content,
                        hashtag.hashtagName,
                        article.createdAt,
                        article.createdBy,
                        article.modifiedAt,
                        article.modifiedBy
                ));

        List<ArticleSelectDto> articles = query.fetch().stream() //TODO: 페이징 처리를 하지 않기 때문에 모든데이터를 한번에 조회하는 문제발생 -> 메모리부족, 성능저하
                .sorted(getArticleComparator(pageable.getSort())) // Java에서 수동으로 정렬
                .collect(Collectors.toList());

        long count = from(articleHashtag)
                .select(articleHashtag)
                .innerJoin(articleHashtag.article, article)
                .innerJoin(articleHashtag.hashtag, hashtag)
                .where(hashtag.hashtagName.in(hashtagNames)).fetchCount();

        return new PageImpl<>(articles, pageable, count);
    }

    @Override
    public Set<ArticleHashtag> findByArticleId(Long articleId) {
        QArticle article = QArticle.article;
        QArticleHashtag articleHashtag = QArticleHashtag.articleHashtag;
        QHashtag hashtag = QHashtag.hashtag;

        List<ArticleHashtag> articleHashtags = from(articleHashtag)
                .innerJoin(articleHashtag.hashtag, hashtag).fetchJoin()
                .where(articleHashtag.article.id.eq(articleId))
                .select(articleHashtag)
                .fetch();

        return articleHashtags.stream().collect(Collectors.toUnmodifiableSet());
    }

    private Comparator<ArticleSelectDto> getArticleComparator(Sort sort) {
        // Sort 정보가 없으면 기본 Comparator 반환 (여기서는 title 기준 오름차순)
        if (sort.isUnsorted()) {
            return Comparator.comparing(ArticleSelectDto::createdAt);
        }

        // 첫 번째 Sort 정보만 사용 (다중 Sort 필요 시 로직 추가)
        Sort.Order order = sort.iterator().next();

        if ("title".equals(order.getProperty())) {
            return order.isAscending() ?
                    Comparator.comparing(ArticleSelectDto::title) :
                    Comparator.comparing(ArticleSelectDto::title).reversed();
        } else if ("content".equals(order.getProperty())) {
            return order.isAscending() ?
                    Comparator.comparing(ArticleSelectDto::content) :
                    Comparator.comparing(ArticleSelectDto::content).reversed();
        } else if ("userAccount.userId".equals(order.getProperty())) {
            return order.isAscending() ? //TODO: aritlce조회시 userAccount는 지연로딩이므로 N+1문제 발생
                    Comparator.comparing((ArticleSelectDto selectDto) -> selectDto.userAccount().getUserId()) :
                    Comparator.comparing((ArticleSelectDto selectDto) -> selectDto.userAccount().getUserId()).reversed();
        } else {
            return order.isAscending() ?
                    Comparator.comparing(ArticleSelectDto::createdAt) :
                    Comparator.comparing(ArticleSelectDto::createdAt).reversed();
        }
    }
}
