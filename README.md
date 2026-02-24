# Java Core Custom Web Server

Custom web server xây dựng bằng Java Socket và NIO, không dùng Spring Boot. Website học Java Core — học bằng cách thực hành và giải thích lại.

## Yêu cầu

- JDK 17+

## Chạy server

**Cách 1: JAR (khuyến nghị)**

```bash
mvn package -DskipTests
java -jar target/java-core-server-1.0.0.jar
```

Port tùy chỉnh:

```bash
java -jar target/java-core-server-1.0.0.jar 9000
```

**Cách 2: exec:java**

```bash
mvn compile exec:java
```

Port tùy chỉnh:

```bash
mvn exec:java -Dexec.args="9000"
```

Mặc định server listen trên port **9091**.

## Lỗi thường gặp

- **Address already in use**: Port 9091 đang bị chiếm. Dùng port khác: `java -jar target/java-core-server-1.0.0.jar 9000`
- **exec:java không chạy**: Chạy `mvn compile` trước để copy resources vào `target/classes`

## Truy cập

- Trang chủ: http://localhost:9091/
- Topic OOP: http://localhost:9091/topics/oop
- Các topic khác (stub): http://localhost:9091/topics/collections, ...

## Cấu trúc

- `src/main/java/com/javacore/server/` — server, router, handlers
- `src/main/resources/web/` — HTML, CSS, nội dung tĩnh
