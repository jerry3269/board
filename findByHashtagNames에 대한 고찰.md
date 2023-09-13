### findByHashtagNames 변천사

pageable : article에 대한 페이징 및 정렬 데이터를 가지고 있음.

<Br>

---

#### 1. [#86](https://github.com/jerry3269/board/issues/86) - bf5175a

* 문제 : articleHashtag에 조회쿼리를 날리기 때문에 article의 필드로 정렬할 수 없게 됨.

* 해결 : 페이징 쿼리를 날리지 않고 자바에서 수동으로 정렬 처리.

```java
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
                        article.createdAt,
                        article.createdBy,
                        article.modifiedAt,
                        article.modifiedBy
                ));

        List<ArticleSelectDto> articles = query.fetch().stream()
                .sorted(getArticleComparator(pageable.getSort())) 
                .collect(Collectors.toList());

        long count = from(articleHashtag)
                .select(articleHashtag)
                .innerJoin(articleHashtag.article, article)
                .innerJoin(articleHashtag.hashtag, hashtag)
                .where(hashtag.hashtagName.in(hashtagNames)).fetchCount();

        return new PageImpl<>(articles, pageable, count);
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
```

<br>

---

#### 2. [#87](https://github.com/jerry3269/board/issues/87) - 58d5d04

* 문제 : 
  
  1. 페이징 처리를 하지 않아서 모든 데이터를 한번에 조회하여 성능상 이슈 및 pagealbe과 조회된 articles사이의 데이터 불일치 발생.
  
  2. hashtag를 가져오지 않아서 이후에 hashtag를 가져오기 위한 N+1 문제 발생.

* 해결 : ArticleSelectDto에 hashtagName 필드를 추가하여 같이 조회함. 이후 서비스 레이어에서 해당 데이터를 바탕으로 데이터를 리팩토링함. -> 2번째 문제만 해결.

```java
select(new QArticleSelectDto(
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
```

위의 코드에서 hashtag.hashtagName만 추가 됨.

<br>

---

#### 3. [#90](https://github.com/jerry3269/board/issues/90) - 7405ea3

* 문제 : 
  
  1. 페이징 처리를 하지 않아서 모든 데이터를 한번에 조회하여 성능상 이슈 및 pagealbe과 조회된 articles사이의 데이터 불일치 발생.
  
  2. SearchType이 hashtag일 경우만 쿼리 최적화가 되고, 다른 타입이거나 기본 페이지 조회시 똑같이 N+1문제 발생.

* 해결 : 
  
  1. properties접근법을 이용하여 pageable객체를 재생성함. -> 1번 이슈 해결
  
  2. in(articleIds)로 연관된 Hashtag의 정보를 조회하는 새로운 쿼리 생성 -> 2번 이슈 해결
  
  ```java
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
  ```

2번 커밋의 단점은 페이징 처리를 하지 않아 모든 데이터를 한번에 조회하고, 검색타입이 해태그일 경우에만 검색 쿼리가 최적화 되는 문제가 있다.

<BR>

이때, **프로퍼티를** 사용하여 **article**필드의 데이터로 정렬을 할 수 있다는 걸 알게 되었고, 이를 통해 **Pageable**을 재정의 해 주었다.

정의된 **pageable**을 이용하여 페이징과 정렬 처리를 해줌으로써, 모든데이터가 아닌 필요한 데이터만 가져오고, 조회된 **articles**와 **pageable**사이의 불일치 문제를 해결하였다.

<bR>

또한, 해시태그 검색만이 아닌 다른 검색에서도 **article**과 연관된 **hashtag**를 가져오기 위해 **N+1쿼리**가 발생하는 문제가 있었는데, 이를 해결하기 위해  **articleIds**를 받아 해시태그를 조회하는 쿼리를 추가해 주었다. 
