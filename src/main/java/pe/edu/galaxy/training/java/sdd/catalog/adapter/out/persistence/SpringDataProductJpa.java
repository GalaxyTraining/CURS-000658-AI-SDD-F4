package pe.edu.galaxy.training.java.sdd.catalog.adapter.out.persistence;

import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataProductJpa extends JpaRepository<ProductJpaEntity, UUID> {

    boolean existsBySku(String sku);

    @Query("""
            select p from ProductJpaEntity p
            where (:status is null or p.status = :status)
              and (:category is null or p.category = :category)
            """)
    Page<ProductJpaEntity> search(@Param("status") ProductStatus status,
                                  @Param("category") ProductCategory category,
                                  Pageable pageable);
}
