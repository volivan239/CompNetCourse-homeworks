# Практика 2. Rest Service

Помимо самого сервиса, в репозитории находится [отчет](TaskB.md), демонстрирующий его работу, и [решения](problems.pdf) задач из второй части.

## Установка и запуск

Сервис использует систему сборки `gradle`. После скачивания репозитория запуск производится из консоли командами

```
./gradlew run
```

или (вариант для OS Windows)

```
.\gradlew.bat run
```

После этого программа запустит сервер (по умолчанию доступный по `http://127.0.0.1:8080`), к которому уже можно обращаться.

## Поддерживаемые операции

* GET-запрос к корню (пример: `GET http://127.0.0.1:8080`) вернет список всех имеющихся продуктов в формате JSON (по умолчанию в список добавляется один элемент). Для каждого продукта указаны поля `id` (идентификатор, по которому в дальнейшем можно обращаться к продукту), `name` (название продукта) и `desc` (описание).
* GET-запрос по конкретному `id` (пример: `GET http://127.0.0.1:8080/4bce8d2a-0060-4a7e-ba42-fe8e983fc25f`) вернет JSON с информацией только об этом продукте
* GET-запрос к `/{id}/image` (пример: `GET http://127.0.0.1:8080/4bce8d2a-0060-4a7e-ba42-fe8e983fc25f/image`) вернет png-файл с иконкой продукта 
* Можно добавлять продукты через POST-запрос. Он должен иметь тело формата JSON, в котором будут указаны поля `name`, `desc` и `imgURL` — название, описание и URL иконки в формате png (это поле может иметь значение **null**). В качестве ответа сервис вернет сгенерированный `id` для нового продукта. Пример такого запроса:
```
POST http://127.0.0.1:8080
{"name":"Sample product", "desc":"Sample desc", "imgURL": "http://mysite.mydomain/myimage.png"}
```
* Аналогично можно модифицировать имеющиеся продукты с помощью `PUT`-запроса. Тело должно иметь такой же формат, что и `POST`, но в URL необходимо указать `id` продукта как это сделано в `GET`-запросах
* Можно удалить продукт с помощью запроса `DELETE`, для этого в `URL` нужно передать `id` удаляемого продукта (пример: `DELETE http://127.0.0.1:8080/4bce8d2a-0060-4a7e-ba42-fe8e983fc25f`)
