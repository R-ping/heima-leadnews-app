-- 延时任务表（来自 leadnews_schedule 数据库）
CREATE TABLE IF NOT EXISTS `taskinfo` (
  `task_id` bigint(20) NOT NULL COMMENT '任务id',
  `execute_time` datetime(3) DEFAULT NULL COMMENT '执行时间',
  `parameters` blob COMMENT '参数',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `task_type` int(11) DEFAULT NULL COMMENT '任务类型',
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 延时任务日志表
CREATE TABLE IF NOT EXISTS `taskinfo_logs` (
  `task_id` bigint(20) NOT NULL COMMENT '任务id',
  `execute_time` datetime(3) DEFAULT NULL COMMENT '执行时间',
  `parameters` blob COMMENT '参数',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `task_type` int(11) DEFAULT NULL COMMENT '任务类型',
  `version` int(11) DEFAULT '0' COMMENT '版本号,乐观锁',
  `status` int(11) DEFAULT '0' COMMENT '状态 0=int 1=EXECUTED 2=SUCCESS 3=CANCELLED',
  `first_exec_interval` bigint(20) DEFAULT '0' COMMENT '预执行时间',
  `last_exec_interval` bigint(20) DEFAULT '0' COMMENT '执行时间',
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;