-- Add the ItemHandler admin command to the commands table

INSERT INTO commands (name, access_level, class_name, help_text)
VALUES ('itemhandler', 100, 'ItemHandler', 'Controls the item handler adapter settings. Syntax: .itemhandler [route|fallback|metrics|logging|stats] [value]'); 