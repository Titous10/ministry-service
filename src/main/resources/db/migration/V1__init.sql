-- ministries
CREATE TABLE IF NOT EXISTS ministries
(
    id               uuid PRIMARY KEY,
    name             varchar(255) NOT NULL UNIQUE,
    type             varchar(50),
    established_date date,
    parent_id        uuid,
    term_start       date,
    term_end         date,
    criteria         jsonb,
    active           boolean          DEFAULT true
);

-- ministry_members
CREATE TABLE IF NOT EXISTS ministry_members
(
    id            uuid PRIMARY KEY,
    ministry_id   uuid NOT NULL,
    member_id     uuid NOT NULL,
    role          varchar(50),
    committee     boolean          DEFAULT false,
    assigned_date date             DEFAULT CURRENT_DATE,
    active        boolean          DEFAULT true,
    CONSTRAINT uq_ministry_member UNIQUE (ministry_id, member_id)
);

-- hierarchy table (transitive closure)
CREATE TABLE IF NOT EXISTS ministry_hierarchy
(
    id            serial PRIMARY KEY,
    ancestor_id   uuid    NOT NULL,
    descendant_id uuid    NOT NULL,
    depth         integer NOT NULL,
    CONSTRAINT uq_mh UNIQUE (ancestor_id, descendant_id)
);

CREATE INDEX IF NOT EXISTS idx_mh_ancestor ON ministry_hierarchy (ancestor_id);
CREATE INDEX IF NOT EXISTS idx_mh_descendant ON ministry_hierarchy (descendant_id);