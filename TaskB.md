## Отчет по работспособности сервиса (задание Б)

*Примечание: в соответствии с очередностями заданий, это задание выполнялось до того как были поддержаны иконки у продуктов, поэтому API, используемое здесь, несколько отличается от текущего.*

1. `GET`-запрос к корневому каталогу возвращает список всех продуктов, первый продукт добавляется автоматически:
<img width="665" alt="get_all" src="https://user-images.githubusercontent.com/65076429/221989838-a240ab1d-72ac-4d42-b5fb-be3e80a73f10.png">

2. `GET`-запрос к `/{id}` возвращает информацию об этом продукте или выдает `404` если такого продукта не существует:
<img width="659" alt="get_by_id_ok" src="https://user-images.githubusercontent.com/65076429/221990240-8a1fa838-0459-412b-b127-d18deb3b290f.png">
<img width="681" alt="get_by_id_not_found" src="https://user-images.githubusercontent.com/65076429/221990261-d544a996-45a2-4480-bddf-befe3a8f0b05.png">

3. `POST`-запрос позволяет добавить продукт и возвращает сгенерированный для него `id` (на втором скриншоте показан результат `GET`-запроса после добавления, на нем видны уже два продукта)
<img width="658" alt="post_ok" src="https://user-images.githubusercontent.com/65076429/221990657-7687d27f-0a3c-4a7e-9bd1-5f188f4c8652.png">
<img width="661" alt="get_after_post" src="https://user-images.githubusercontent.com/65076429/221990687-d0563d94-76e4-4380-80d1-420c4d11f0a7.png">

4. `PUT`-запрос позволяет модифицровать продукт (на втором скришноте показан результат последующего `GET`, из которого видно что изменение прошло успешно):
<img width="677" alt="put_ok" src="https://user-images.githubusercontent.com/65076429/221990938-4b4ed4ff-dd56-4c10-bf2f-48b3f3ea9fc1.png">
<img width="668" alt="get_after_put" src="https://user-images.githubusercontent.com/65076429/221990958-2b6340c9-3555-4b42-9ce7-54f6d1767eaf.png">

5. `DELETE`-запрос удаляет продукт (или возвращает ошибку, если продукт не найден или уже удален)
<img width="685" alt="delete_ok" src="https://user-images.githubusercontent.com/65076429/221991164-fa683922-d494-4d70-9fa3-8d115cdcac58.png">
<img width="683" alt="get_after_delete" src="https://user-images.githubusercontent.com/65076429/221991183-cec3c555-65fd-4428-9c2e-2157417e0f7c.png">
