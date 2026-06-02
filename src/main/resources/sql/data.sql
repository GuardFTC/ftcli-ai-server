-- 日期工具：获取当前时间
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('getNowTime', '获取当前时间，返回格式为yyyy-MM-dd HH:mm:ss', 'date');

-- 日期工具：计算距离当天结束的剩余时间
INSERT OR IGNORE INTO tool_spec (name, description, type)
VALUES ('getRemainingTimeToDayEnd', '计算当前时间距离当天结束还有多少时间', 'date');

-- getRemainingTimeToDayEnd工具参数：当前时间
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'getRemainingTimeToDayEnd'),
        'nowDateStr', '当前时间,格式为yyyy-MM-dd HH:mm:ss', 1, 'STRING', NULL);

-- getRemainingTimeToDayEnd工具参数：时间单位
INSERT OR IGNORE INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values)
VALUES ((SELECT id FROM tool_spec WHERE name = 'getRemainingTimeToDayEnd'),
        'unit', '时间单位', 0, 'ENUMS', 'HOUR,MINUTE,SECOND');
