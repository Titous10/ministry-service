package com.project.ministry_service.ministry.application;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class HierarchyJdbcService {

    private final NamedParameterJdbcTemplate jdbc;

    public HierarchyJdbcService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Insert self + ancestors for a newly created node in one shot.
     */
    @Transactional
    public void insertSelfAndParentAncestors(UUID newId, UUID parentId) {
        // insert self
        String insertSelf = "INSERT INTO ministry_hierarchy (ancestor_id, descendant_id, depth) VALUES (:id, :id, 0) ON CONFLICT DO NOTHING";
        jdbc.update(insertSelf, new MapSqlParameterSource().addValue("id", newId));

        if (parentId != null) {
            // insert parent ancestors -> newId
            String insertAncestors = """
                    INSERT INTO ministry_hierarchy (ancestor_id, descendant_id, depth)
                    SELECT ancestor_id, :newId, depth + 1
                    FROM ministry_hierarchy
                    WHERE descendant_id = :parentId
                    ON CONFLICT DO NOTHING
                    """;
            jdbc.update(insertAncestors, new MapSqlParameterSource().addValue("newId", newId).addValue("parentId", parentId));
        }
    }

    /**
     * Move subtree rooted at movedId under newParentId (nullable) in one shot:
     * deletes old ancestor links and inserts new ones.
     */
    @Transactional
    public void moveSubtree(UUID movedId, UUID newParentId) {
        // Delete old ancestor -> descendant rows where ancestor in old ancestors and descendant in subtree
        String deleteSql = """
            WITH subtree AS (
                SELECT descendant_id FROM ministry_hierarchy WHERE ancestor_id = :movedId
            ),
            old_anc AS (
                SELECT ancestor_id FROM ministry_hierarchy WHERE descendant_id = :movedId
            )
            DELETE FROM ministry_hierarchy mh
            USING old_anc oa, subtree s
            WHERE mh.ancestor_id = oa.ancestor_id AND mh.descendant_id = s.descendant_id;
            """;
        jdbc.update(deleteSql, new MapSqlParameterSource().addValue("movedId", movedId));

        // Insert new ancestor links combining new parent's ancestors and the subtree
        if (newParentId != null) {
            String insertSql = """
                WITH parent_anc AS (
                    SELECT ancestor_id, depth FROM ministry_hierarchy WHERE descendant_id = :newParentId
                    UNION ALL
                    SELECT :newParentId AS ancestor_id, 0 AS depth
                ),
                subtree AS (
                    SELECT descendant_id, depth FROM ministry_hierarchy WHERE ancestor_id = :movedId
                )
                INSERT INTO ministry_hierarchy (ancestor_id, descendant_id, depth)
                SELECT p.ancestor_id, s.descendant_id, p.depth + 1 + s.depth
                FROM parent_anc p CROSS JOIN subtree s
                ON CONFLICT (ancestor_id, descendant_id) DO NOTHING;
                """;
            jdbc.update(insertSql, new MapSqlParameterSource().addValue("newParentId", newParentId).addValue("movedId", movedId));
        } else {
            // moving to root: insert ancestors = self only
            String insertRootSql = """
                WITH subtree AS (
                    SELECT descendant_id, depth FROM ministry_hierarchy WHERE ancestor_id = :movedId
                )
                INSERT INTO ministry_hierarchy (ancestor_id, descendant_id, depth)
                SELECT :movedId AS ancestor_id, s.descendant_id, s.depth
                FROM subtree s
                ON CONFLICT (ancestor_id, descendant_id) DO NOTHING;
                """;
            jdbc.update(insertRootSql, new MapSqlParameterSource().addValue("movedId", movedId));
        }
    }

    /**
     * Full rebuild (careful - expensive): truncates and rebuilds entire transitive closure table.
     */
    @Transactional
    public void rebuildFullHierarchy() {
        String sql = """
            TRUNCATE TABLE ministry_hierarchy;
            INSERT INTO ministry_hierarchy (ancestor_id, descendant_id, depth)
            WITH RECURSIVE tree AS (
              SELECT id AS ancestor_id, id AS descendant_id, 0 AS depth FROM ministries
              UNION ALL
              SELECT t.ancestor_id, m.id AS descendant_id, t.depth + 1
              FROM tree t
              JOIN ministries m ON m.parent_id = t.descendant_id
            )
            SELECT ancestor_id, descendant_id, depth FROM tree;
            """;
        jdbc.getJdbcOperations().execute(sql);
    }

    /**
     * Optionally call the stored procedure if you created it.
     * Example call: SELECT rebuild_ministry_hierarchy(:root_id)
     */
    @Transactional
    public Integer callRebuildProcedure(UUID rootId) {
        String sql = "SELECT rebuild_ministry_hierarchy(:rootId)";
        return jdbc.queryForObject(sql, new MapSqlParameterSource().addValue("rootId", rootId), Integer.class);
    }
}
