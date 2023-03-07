# TCP Веб-сервер

## Запуск

Проект использует систему сборки gradle. Запустить *сервер* из корневого каталога можно, например, с помощью команды
```
./gradlew :server:run --args='[serverArgs]'
```

Аналогично, *клиент* запускается с помощью
```
./gradlew :client:run --args='[clientArgs]'
```

(Для OS Windows, соответственно, надо использовать `.\gradlew`)

## Сервер

Сервер принимает единственный аргумент — `concurrencyLevel` из условия (его можно не передавать, значение по умолчанию — `5`), задаующийся через `--concurrencyLevel` или `-c`. Сервер всегда запускается на порте `8080`. Все файлы сервер будет брать из папки `server/data` (по умолчанию в ней лежат два файла — `sampleTextFile.txt` и `sampleImage.png`). Пример запуска:
```
./gradlew :server:run --args='-c 100'
```

## Клиент

Клиент принимает три обязательных аргумента: хост, порт и запрашиваемый файл. Пример запуска:
```
 .\gradlew :client:run --args='127.0.0.1 8080 sampleTextFile.txt'
```

В выводе клиент сначала напишет "сырой" HTML-ответ сервера, а потом его интерпретацию:
* в случае ошибки — сообщение об ошибке
* в случае успешного получения текстового файла — выведет его содержимое
* в случае успешного получения файла иного типа — сохранит его содержимое в локальный файл с соответствующим именем (этот файл будет сохранен в папке `client`), и напишет соответствующее сообщение.
