
-- ----------------------------
-- Table structure for t_com_resources
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_com_resources";
CREATE SEQUENCE t_com_resources_id_seq;

CREATE TABLE "public"."t_com_resources" (
  "id" int4 NOT NULL DEFAULT nextval('t_com_resources_id_seq'::regclass),
  "module" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "class_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "simple_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "method_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "permissions" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "request_type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "request_url" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "api_desc" varchar(100) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "api_remark" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying,
  "login_check" int2 NOT NULL DEFAULT 0,
  "permission_check" int2 NOT NULL DEFAULT 0,
  "create_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "edit_time" timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "del_flag" int2 NOT NULL DEFAULT 0
)
;
COMMENT ON COLUMN "public"."t_com_resources"."module" IS '子系统项目模块名称, 微服务注册名称';
COMMENT ON COLUMN "public"."t_com_resources"."class_name" IS '包名.类名';
COMMENT ON COLUMN "public"."t_com_resources"."simple_name" IS '类名';
COMMENT ON COLUMN "public"."t_com_resources"."method_name" IS '方法名称';
COMMENT ON COLUMN "public"."t_com_resources"."permissions" IS '资源权限控制';
COMMENT ON COLUMN "public"."t_com_resources"."request_type" IS '请求类型 get post等';
COMMENT ON COLUMN "public"."t_com_resources"."request_url" IS '请求路径';
COMMENT ON COLUMN "public"."t_com_resources"."api_desc" IS '接口描述';
COMMENT ON COLUMN "public"."t_com_resources"."api_remark" IS '接口备注';
COMMENT ON COLUMN "public"."t_com_resources"."create_time" IS '记录创建时间';
COMMENT ON COLUMN "public"."t_com_resources"."edit_time" IS '最后修改时间';
COMMENT ON COLUMN "public"."t_com_resources"."del_flag" IS '删除标记位 0未删除 1删除';
COMMENT ON TABLE "public"."t_com_resources" IS '系统请求资源表，调用接口自动生成跟维护';

-- ----------------------------
-- Primary Key structure for table t_com_resources
-- ----------------------------
ALTER TABLE "public"."t_com_resources" ADD CONSTRAINT "t_com_resources_pkey" PRIMARY KEY ("id");


INSERT INTO t_com_resources("id", "module", "class_name", "simple_name", "method_name", "permissions", "request_type", "request_url", "api_desc", "api_remark", "login_check", "permission_check", "create_time", "edit_time", "del_flag") VALUES (1, 'micro-svc-demo', 'com.test.demo.controller.DemoController', 'DemoController', 'getUserInfo1', 'demo:getUserInfo1', '[GET]', '/demo/getUserInfo1', '获取网关传递的用户信息', '获取网关传递的用户信息', 0, 1, '2019-10-27 16:35:44.27424', '2019-10-27 16:35:44.27424', 0);

INSERT INTO t_com_resources("id", "module", "class_name", "simple_name", "method_name", "permissions", "request_type", "request_url", "api_desc", "api_remark", "login_check", "permission_check", "create_time", "edit_time", "del_flag") VALUES (2, 'micro-svc-demo1', 'com.test.demo.controller.DemoController', 'DemoController', 'getUserInfo1', 'demo:getUserInfo1', '[GET]', '/demo/getUserInfo1', '获取网关传递的用户信息', '获取网关传递的用户信息', 0, 1, '2019-10-27 16:35:44.27424', '2019-10-27 16:35:44.27424', 0);

INSERT INTO t_com_resources("id", "module", "class_name", "simple_name", "method_name", "permissions", "request_type", "request_url", "api_desc", "api_remark", "login_check", "permission_check", "create_time", "edit_time", "del_flag") VALUES (3, 'micro-svc-demo1', 'com.test.demo.controller.DemoController', 'DemoController', 'getUserInfo2', 'demo:getUserInfo2', '[GET]', '/demo/getUserInfo2', '通过shiro安全框架获取用户信息', '通过shiro安全框架获取用户信息', 0, 1, '2019-10-27 16:35:44.27424', '2019-10-27 16:35:44.27424', 0);



