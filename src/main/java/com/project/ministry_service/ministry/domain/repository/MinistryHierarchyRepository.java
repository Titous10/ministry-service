package com.project.ministry_service.ministry.domain.repository;

import com.project.ministry_service.ministry.domain.model.MinistryHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MinistryHierarchyRepository extends JpaRepository<MinistryHierarchy, Long> {
    List<MinistryHierarchy> findAllByAncestorId(UUID ancestorId);
    List<MinistryHierarchy> findAllByDescendantId(UUID descendantId);

    record DescentDepth(UUID descent, int depth) {}

    @Query(value = """
    SELECT mh.descendant_id AS descent,
           MAX(mh.depth) AS depth,
           m.name AS name
    FROM ministry_hierarchy mh
    JOIN ministries m ON m.id = mh.descendant_id
    WHERE mh.descendant_id IN (:descendantIds)
    GROUP BY mh.descendant_id, m.name
    HAVING MAX(mh.depth) = (
        SELECT MIN(sub.max_depth)
        FROM (
            SELECT MAX(mh2.depth) AS max_depth
            FROM ministry_hierarchy mh2
            WHERE mh2.descendant_id IN (:descendantIds)
            GROUP BY mh2.descendant_id
        ) AS sub
    )
    """, nativeQuery = true)
    List<Object[]> findDescentWithMinOfMaxDepth(@Param("descendantIds") Set<UUID> descendantIds);


    void deleteAllByDescendantId(UUID descendantId);
}