-- Export etcitem data
SELECT CONCAT(item_id, ',', REPLACE(name, ',', ' '), ',', item_type, ',ETC') as output
FROM etcitem ORDER BY item_id;

-- Export weapon data
SELECT CONCAT(item_id, ',', REPLACE(name, ',', ' '), ',', type, ',WEAPON') as output
FROM weapon ORDER BY item_id;

-- Export armor data
SELECT CONCAT(item_id, ',', REPLACE(name, ',', ' '), ',', type, ',ARMOR') as output
FROM armor ORDER BY item_id;

-- Export petitem data
SELECT CONCAT(item_id, ',PET_ITEM_', item_id, ',equipment,PET') as output
FROM petitem ORDER BY item_id; 