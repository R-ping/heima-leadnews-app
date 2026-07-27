<!-- code-review-graph MCP tools -->
## MCP Tools: code-review-graph

**IMPORTANT: This project has a knowledge graph. ALWAYS use the
code-review-graph MCP tools BEFORE using Grep/Glob/Read to explore
the codebase.** The graph is faster, cheaper (fewer tokens), and gives
you structural context (callers, dependents, test coverage) that file
scanning cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes_tool` or `query_graph_tool` instead of Grep
- **Understanding impact**: `get_impact_radius_tool` instead of manually tracing imports
- **Code review**: `detect_changes_tool` + `get_review_context_tool` instead of reading entire files
- **Finding relationships**: `query_graph_tool` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview_tool` + `list_communities_tool`

Fall back to Grep/Glob/Read **only** when the graph doesn't cover what you need.

### Key Tools

| Tool | Use when |
| ------ | ---------- |
| `detect_changes_tool` | Reviewing code changes — gives risk-scored analysis |
| `get_review_context_tool` | Need source snippets for review — token-efficient |
| `get_impact_radius_tool` | Understanding blast radius of a change |
| `get_affected_flows_tool` | Finding which execution paths are impacted |
| `query_graph_tool` | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes_tool` | Finding functions/classes by name or keyword |
| `get_architecture_overview_tool` | Understanding high-level codebase structure |
| `refactor_tool` | Planning renames, finding dead code |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes_tool` for code review.
3. Use `get_affected_flows_tool` to understand impact.
4. Use `query_graph_tool` pattern="tests_for" to check coverage.

---

# 全局固定规则（必须严格遵守）

## 一、Git 版本管理规则

1. **新功能/模块必开新分支（强制）**
   - 每当开始一个新的功能模块、需求迭代或 Bugfix 时，必须从主分支切出新分支。
   - 分支命名规范：`feature/xxxxx`（新功能）、`fix/xxxxx`（修复问题）、`refactor/xxxxx`（重构）。
   - 禁止直接在主分支上修改代码。

2. **任务完成后的自动提交流程（强制）**
   - 当一个完整任务完成后，必须主动执行：
     1. 检查当前变更文件（`git status`）。
     2. 暂存所有变更（`git add`，需排除 `.gitignore` 中的文件）。
     3. 生成符合规范的 Commit Message 并提交。
   - 如果代码存在报错或未通过本地编译校验，禁止提交。

3. **Commit Message 规范（约定式提交）**
   - 必须遵循语义化格式：`<type>(<scope>): <subject>`
   - 常用 type：`feat`（新功能）、`fix`（修复）、`docs`（文档）、`style`（格式）、`refactor`（重构）、`perf`（性能优化）
   - 示例：`feat(user): 新增用户登录接口` 或 `fix(cart): 修复购物车计算总价错误`

## 二、开发行为与流程规则

1. **先读后写（理解上下文）**
   - 在开始编码前，必须先阅读项目现有代码目录结构。严禁在未理解现有技术栈的情况下随意引入新依赖。

2. **增量式开发（小步快跑）**
   - 不要一次性生成数百行复杂逻辑。优先构建骨架，确认方向正确后，再逐步填充实现细节。

3. **依赖引入需谨慎**
   - 如需引入第三方包，必须检查其维护状态和 License 兼容性，并告知用户确认后再执行安装命令。

## 三、代码质量标准

1. **自测与错误处理**
   - 生成的代码必须具备基本的异常捕获和边界处理（空指针、网络超时、数组越界等）。
   - 对外接口必须包含输入参数的校验逻辑。

2. **清理无用代码**
   - 任务完成后，必须主动删除调试用的 `console.log`、`print`、暂存的无用变量以及仅用于测试的硬编码数据。

3. **类型与文档定义**
   - 严禁使用裸 `Object`，必须定义明确的类型。
   - 复杂的公共函数必须添加标准的 JSDoc 或 DocString 注释。

## 四、交互与汇报规则

1. **变更即记录（CHANGELOG）**
   - 每次完成一个可用的功能节点后，除了 Git 提交，请在 `/docs` 目录下更新或生成 `CHANGELOG.md`。

2. **遇到模糊需求必须先确认**
   - 如果需求表述模糊，严禁擅自定义业务逻辑。必须列出疑问点，向用户确认后再动工。

3. **任务结束汇报格式**
   - 每次任务完成后，回复末尾必须包含：
     - **完成内容**：一句话总结。
     - **变更文件列表**：列出被修改/新增的文件。
     - **下一步建议**：指出潜在的风险或建议测试的重点。

## 五、安全红线（绝对禁止）

1. **严禁硬编码敏感信息**
   - 代码中绝对禁止出现明文密码、AccessKey/SecretKey、Token、数据库连接串。必须使用环境变量引用。
   - 务必确保 `.env` 已在 `.gitignore` 中，防止误提交。
