# OpenTelemetry Java Agent 配置指南

本文档说明如何为黑马头条微服务应用配置 OpenTelemetry Java Agent，实现链路追踪（Traces）、指标（Metrics）的自动采集与导出。

---

## 1. 下载 OTel Agent

从 OpenTelemetry Java Instrumentation 官方仓库下载 `opentelemetry-javaagent.jar`：

- 下载地址：https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases
- 选择最新稳定版本（建议 v1.33.0+）
- 将 jar 包放置在服务器的统一路径下，如 `/opt/otel/opentelemetry-javaagent.jar`

```bash
# 示例：下载最新版
wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.33.6/opentelemetry-javaagent.jar \
  -O /opt/otel/opentelemetry-javaagent.jar
```

---

## 2. JVM 启动参数

在每个服务的 JVM 启动命令中添加以下参数。各服务需要替换 `-Dotel.service.name` 和 `-Dotel.resource.attributes` 中的 `service` 值。

### leadnews-article

```
-javaagent:/opt/otel/opentelemetry-javaagent.jar
-Dotel.service.name=leadnews-article
-Dotel.resource.attributes=env=dev,service=leadnews-article
-Dotel.exporter.otlp.endpoint=http://localhost:4317
-Dotel.exporter.otlp.protocol=grpc
-Dotel.metrics.exporter=otlp
-Dotel.traces.exporter=otlp
-Dotel.logs.exporter=none
-Dotel.javaagent.debug=false
```

### leadnews-wemedia

```
-javaagent:/opt/otel/opentelemetry-javaagent.jar
-Dotel.service.name=leadnews-wemedia
-Dotel.resource.attributes=env=dev,service=leadnews-wemedia
-Dotel.exporter.otlp.endpoint=http://localhost:4317
-Dotel.exporter.otlp.protocol=grpc
-Dotel.metrics.exporter=otlp
-Dotel.traces.exporter=otlp
-Dotel.logs.exporter=none
-Dotel.javaagent.debug=false
```

### leadnews-user

```
-javaagent:/opt/otel/opentelemetry-javaagent.jar
-Dotel.service.name=leadnews-user
-Dotel.resource.attributes=env=dev,service=leadnews-user
-Dotel.exporter.otlp.endpoint=http://localhost:4317
-Dotel.exporter.otlp.protocol=grpc
-Dotel.metrics.exporter=otlp
-Dotel.traces.exporter=otlp
-Dotel.logs.exporter=none
-Dotel.javaagent.debug=false
```

### leadnews-admin

```
-javaagent:/opt/otel/opentelemetry-javaagent.jar
-Dotel.service.name=leadnews-admin
-Dotel.resource.attributes=env=dev,service=leadnews-admin
-Dotel.exporter.otlp.endpoint=http://localhost:4317
-Dotel.exporter.otlp.protocol=grpc
-Dotel.metrics.exporter=otlp
-Dotel.traces.exporter=otlp
-Dotel.logs.exporter=none
-Dotel.javaagent.debug=false
```

### leadnews-gateway

```
-javaagent:/opt/otel/opentelemetry-javaagent.jar
-Dotel.service.name=leadnews-gateway
-Dotel.resource.attributes=env=dev,service=leadnews-gateway
-Dotel.exporter.otlp.endpoint=http://localhost:4317
-Dotel.exporter.otlp.protocol=grpc
-Dotel.metrics.exporter=otlp
-Dotel.traces.exporter=otlp
-Dotel.logs.exporter=none
-Dotel.javaagent.debug=false
```

### leadnews-search

```
-javaagent:/opt/otel/opentelemetry-javaagent.jar
-Dotel.service.name=leadnews-search
-Dotel.resource.attributes=env=dev,service=leadnews-search
-Dotel.exporter.otlp.endpoint=http://localhost:4317
-Dotel.exporter.otlp.protocol=grpc
-Dotel.metrics.exporter=otlp
-Dotel.traces.exporter=otlp
-Dotel.logs.exporter=none
-Dotel.javaagent.debug=false
```

> **注意**：生产环境（prod）请将 `env=dev` 改为 `env=prod`，并将 `http://localhost:4317` 改为对应的 OTLP Collector 地址。

---

## 3. 环境变量方式（Docker 部署）

使用 Docker 部署时，推荐通过环境变量配置，避免硬编码 JVM 参数。各服务按需替换 `OTEL_SERVICE_NAME` 和 `OTEL_RESOURCE_ATTRIBUTES`。

### leadnews-article

```bash
OTEL_SERVICE_NAME=leadnews-article
OTEL_RESOURCE_ATTRIBUTES=env=dev,service=leadnews-article
OTEL_EXPORTER_OTLP_ENDPOINT=http://alloy:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_METRICS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp
OTEL_LOGS_EXPORTER=none
```

### leadnews-wemedia

```bash
OTEL_SERVICE_NAME=leadnews-wemedia
OTEL_RESOURCE_ATTRIBUTES=env=dev,service=leadnews-wemedia
OTEL_EXPORTER_OTLP_ENDPOINT=http://alloy:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_METRICS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp
OTEL_LOGS_EXPORTER=none
```

### leadnews-user

```bash
OTEL_SERVICE_NAME=leadnews-user
OTEL_RESOURCE_ATTRIBUTES=env=dev,service=leadnews-user
OTEL_EXPORTER_OTLP_ENDPOINT=http://alloy:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_METRICS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp
OTEL_LOGS_EXPORTER=none
```

### leadnews-admin

```bash
OTEL_SERVICE_NAME=leadnews-admin
OTEL_RESOURCE_ATTRIBUTES=env=dev,service=leadnews-admin
OTEL_EXPORTER_OTLP_ENDPOINT=http://alloy:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_METRICS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp
OTEL_LOGS_EXPORTER=none
```

### leadnews-gateway

```bash
OTEL_SERVICE_NAME=leadnews-gateway
OTEL_RESOURCE_ATTRIBUTES=env=dev,service=leadnews-gateway
OTEL_EXPORTER_OTLP_ENDPOINT=http://alloy:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_METRICS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp
OTEL_LOGS_EXPORTER=none
```

### leadnews-search

```bash
OTEL_SERVICE_NAME=leadnews-search
OTEL_RESOURCE_ATTRIBUTES=env=dev,service=leadnews-search
OTEL_EXPORTER_OTLP_ENDPOINT=http://alloy:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_METRICS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp
OTEL_LOGS_EXPORTER=none
```

### Docker Compose 示例

```yaml
services:
  leadnews-article:
    image: leadnews-article:latest
    environment:
      - OTEL_SERVICE_NAME=leadnews-article
      - OTEL_RESOURCE_ATTRIBUTES=env=dev,service=leadnews-article
      - OTEL_EXPORTER_OTLP_ENDPOINT=http://alloy:4317
      - OTEL_EXPORTER_OTLP_PROTOCOL=grpc
      - OTEL_METRICS_EXPORTER=otlp
      - OTEL_TRACES_EXPORTER=otlp
      - OTEL_LOGS_EXPORTER=none
      - JAVA_TOOL_OPTIONS=-javaagent:/opt/otel/opentelemetry-javaagent.jar
```

> **注意**：Docker 环境中，`OTEL_EXPORTER_OTLP_ENDPOINT` 使用容器内部的服务名（如 `alloy`），而非 `localhost`。

---

## 4. Maven 依赖：logstash-logback-encoder

为了让应用以 JSON 格式输出日志（便于 Loki 采集），在父级 `pom.xml` 或各服务 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

并在 `logback-spring.xml` 中配置 JSON 输出 Appender：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE_JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>trace_id</includeMdcKeyName>
            <includeMdcKeyName>span_id</includeMdcKeyName>
            <includeMdcKeyName>trace_flags</includeMdcKeyName>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE_JSON"/>
    </root>
</configuration>
```

> **说明**：`trace_id`、`span_id`、`trace_flags` 由 OpenTelemetry Java Agent 自动注入到 MDC 中，配合 LogstashEncoder 可将链路追踪信息关联到日志中。

---

## 5. IntelliJ IDEA 运行配置

在 IDEA 中为每个服务设置 VM Options：

1. 打开 **Run → Edit Configurations...**
2. 选择对应服务的 Spring Boot 启动配置
3. 在 **VM options** 输入框中填入该服务对应的 JVM 参数，例如 `leadnews-article`：

   ```
   -javaagent:/opt/otel/opentelemetry-javaagent.jar
   -Dotel.service.name=leadnews-article
   -Dotel.resource.attributes=env=dev,service=leadnews-article
   -Dotel.exporter.otlp.endpoint=http://localhost:4317
   -Dotel.exporter.otlp.protocol=grpc
   -Dotel.metrics.exporter=otlp
   -Dotel.traces.exporter=otlp
   -Dotel.logs.exporter=none
   -Dotel.javaagent.debug=false
   ```

4. 点击 **Apply** 保存配置

> **提示**：如果本地没有运行 OTLP Collector（Alloy / OpenTelemetry Collector），可以将 `otel.exporter.otlp.endpoint` 指向远程开发环境，或临时将 `otel.traces.exporter` 和 `otel.metrics.exporter` 设为 `none` 以避免连接错误。

---

## 配置参数说明

| 参数 | 说明 |
|------|------|
| `-javaagent` | 加载 OpenTelemetry Java Agent 的路径 |
| `otel.service.name` | 服务名称，在 Grafana / Jaeger 中显示 |
| `otel.resource.attributes` | 资源属性，`env` 标识环境，`service` 标识服务 |
| `otel.exporter.otlp.endpoint` | OTLP 数据上报端点（Alloy / Collector 地址） |
| `otel.exporter.otlp.protocol` | 传输协议，支持 `grpc` 或 `http/protobuf` |
| `otel.metrics.exporter` | 指标导出器类型，`otlp` 表示通过 OTLP 协议导出 |
| `otel.traces.exporter` | 链路导出器类型，`otlp` 表示通过 OTLP 协议导出 |
| `otel.logs.exporter` | 日志导出器类型，设为 `none`（日志由 Loki 直接采集） |
| `otel.javaagent.debug` | 是否开启 Agent 调试日志，生产环境建议 `false` |