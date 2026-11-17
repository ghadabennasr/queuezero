package com.queuezero.backend.location;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceLocationRepository extends JpaRepository<ServiceLocation, Long> {

    // categoryId = 0 means "any category"; empty strings mean "no filter"
    @Query("""
            select l from ServiceLocation l join fetch l.category c
            where l.active = true
              and (:categoryId = 0L or c.id = :categoryId)
              and (:governorate = '' or lower(l.governorate) = lower(:governorate))
              and lower(l.name) like lower(concat('%', :q, '%'))
            order by l.name
            """)
    List<ServiceLocation> search(@Param("categoryId") long categoryId,
                                 @Param("governorate") String governorate,
                                 @Param("q") String q);

    @Query("select l from ServiceLocation l join fetch l.category where l.id = :id and l.active = true")
    Optional<ServiceLocation> findActiveById(@Param("id") Long id);
}
