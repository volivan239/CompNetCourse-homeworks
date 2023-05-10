# Практика 11. Сетевой уровень
## 2. Дистанционно-векторная маршрутизация

Алгоритм реализован в модуле `distance-vector`, выполнены все три задания. Работоспособность можно проверить с помощью тестов, запускаемых командой
```
./gradlew distance-vector:test
```

Результаты тестов можно будет после этого посмотреть в файле `distance-vector/build/reports/tests/test/index.html`

## 3. Использование протокола IPv6
### Сервер
Сервер запускается командой
```
./gradlew echo-server:run
```

В [конфиг-файле](echo-server/src/main/resources/config.json) можно опционально указать хост и порт на котором будет запущен сервер (значения по умолчанию -- `::1` и `8082`)

### Клиент
Клиент запускается командой
```
./gradlew echo-client:run --args="<host> <port> [message]"
```
Здесь `host` и `port` -- адрес и порт сервера, соответственно, а `message` -- сообщение, передаваемое на сервер (по умолчанию отправляется строка "Echo hello"). Пример запуска клиента и сервера:
![image](https://github.com/volivan239/CompNetCourse-homeworks/assets/65076429/da5b511f-2f4f-4f7d-8c0a-46db4b4de779)
![image](https://github.com/volivan239/CompNetCourse-homeworks/assets/65076429/d9e3dac9-2597-4c43-b99c-e50101d13a70)
