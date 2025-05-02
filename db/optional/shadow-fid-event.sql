-- npc
INSERT INTO `npc` (npcid, name, nameid, note, impl, gfxid, lvl, hp, mp, ac, str, con, dex, wis, intel, mr, exp, lawful, size, weakAttr, ranged, tamable, passispeed, atkspeed, alt_atk_speed, atk_magic_speed, sub_magic_speed, undead, poison_atk, paralysis_atk, agro, agrososc, agrocoi, family, agrofamily, agrogfxid1, agrogfxid2, picupitem, digestitem, bravespeed, hprinterval, hpr, mprinterval, mpr, teleport, randomlevel, randomhp, randommp, randomac, randomexp, randomlawful, damage_reduction, hard, doppel, IsTU, IsErase, bowActId, karma, transform_id, transform_gfxid, light_size, amount_fixed, change_head, cant_resurrect, is_equality_drop, boss) 
VALUES (99000,'Lastavard Blacksmith','Lastavard Blacksmith','Shadow Fid event','L1Merchant',1768,0,0,0,0,0,0,0,0,0,0,0,0,'',0,0,0,0,0,0,0,0,0,0,0,0,0,0,'',0,-1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,0,14,0,1,0,0,0,0);

INSERT INTO `npcaction` VALUES(99000,'shadowevent1','shadowevent1','','');

-- spawn the blacksmiths in key locations
INSERT INTO `spawnlist_npc` (`location`, `count`, `npc_templateid`, `locx`, `locy`, `heading`, `mapid`) 
VALUES('Lastavard Blacksmith',1,99000,33436,32802,6,4),  -- Giran
('Lastavard Blacksmith',1,99000,34059,32279,6,4),        -- Oren
('Lastavard Blacksmith',1,99000,33087,33390,6,4);        -- SKT

-- add illusion enchant weapon scrolls to the shops that sell normal enchant weapon scrolls
INSERT INTO `shop` (`npc_id`, `item_id`, `selling_price`, `order_id`) VALUES
(70033, 40128, 10000, 999),
(70048, 40128, 10000, 999),
(70057, 40128, 10000, 999),
(70063, 40128, 10000, 999),
(70067, 40128, 10000, 999),
(70082, 40128, 10000, 999),
(70092, 40128, 10000, 999),
(70093, 40128, 10000, 999),
(81028, 40128, 10000, 999);

-- update those who already buy it to also sell it
UPDATE `shop` SET `selling_price` = 10000 WHERE item_id = 40128 AND `npc_id` = 70063;

-- Add Shadow Fid weapons to the Lastavard Blacksmith's shop
INSERT INTO `shop` (`npc_id`, `item_id`, `order_id`) VALUES
(99000, 550012, 0),  -- Shadow Fid Dagger
(99000, 550061, 1),  -- Shadow Fid 2H Sword
(99000, 550086, 2),  -- Shadow Fid Edoryu
(99000, 550134, 3),  -- Shadow Fid Staff
(99000, 550160, 4),  -- Shadow Fid Claw
(99000, 550284, 5),  -- Shadow Fid Bow
(99000, 550285, 6),  -- Shadow Fid Chain Sword
(99000, 550286, 7);  -- Shadow Fid Kiringku

-- Event cleanup scripts (commented out by default)
/*
-- Remove the scrolls from shops after event ends
DELETE FROM `shop` WHERE `npc_id` IN(70033,70048,70057,70067,70082,70092,70093,81028) AND `item_id` = 40128 AND `selling_price` = 10000;

-- update the shop that previously bought it to not sell it anymore
UPDATE `shop` SET `selling_price` = -1 WHERE item_id = 40128 AND `npc_id` = 70063;

-- delete all scrolls and weapons from characters, storage, etc.
DELETE FROM `character_items` WHERE `item_id` IN(40128,550012,550061,550086,550134,550160,550284,550285,550286);
DELETE FROM `character_warehouse` WHERE `item_id` IN(40128,550012,550061,550086,550134,550160,550284,550285,550286);
DELETE FROM `character_elf_warehouse` WHERE `item_id` IN(40128,550012,550061,550086,550134,550160,550284,550285,550286);
DELETE FROM `clan_warehouse` WHERE `item_id` IN(40128,550012,550061,550086,550134,550160,550284,550285,550286);

-- remove the NPCs from the spawnlist
DELETE FROM `spawnlist_npc` WHERE `npc_templateid` = 99000;
*/