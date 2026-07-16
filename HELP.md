# 操作文檔

以下命令都需要在專案根目錄底下執行，也就是包含 `pom.xml`、`docker-compose.yaml`、`dockerfile` 的目錄。

例如目前是在 `C:\DevWorkSpace\homework-backend` 底下開發，執行命令前請先確認終端機路徑位於此目錄：

```powershell
pwd
```

![查看專案根目錄](assets/homework-backend-1.jpg)

## 建立 logs 目錄

在專案根目錄底下先建立 `logs` 目錄，讓應用程式與 Docker 容器可以正常寫入日誌：

```powershell
mkdir -p logs
```

## 啟動依賴服務

執行應用程式前，需要先啟動 Docker Compose 內的 MySQL、Redis、RocketMQ 等服務：

```powershell
docker compose up -d
```

![啟動Docker Compose 服務](assets/homework-backend-2.jpg)

確認服務啟動狀態：

```powershell
docker compose ps
```

![查看 Docker Compose 服務狀態](assets/homework-backend-3.jpg)

## 打包方式

```powershell
.\mvnw clean package -DskipTests
```

![打包專案](assets/homework-backend-4.jpg)

## 運行方式 - Windows

```powershell
.\mvnw spring-boot:run
```

![運行專案](assets/homework-backend-5.jpg)

## Docker 打包方式

```powershell
docker build -t demo:latest .
```

![Docker 打包專案](assets/homework-backend-6.jpg)

## 運行方式 - Docker

```powershell
docker run --rm -p 8080:8080 \
  --network homework-backend_default \
  -v "${PWD}/logs:/var/log/app" \
  demo:latest
```

![運行 Docker 容器](assets/homework-backend-7.jpg)

## Postman 導入說明

專案根目錄已提供 Postman Collection 檔案：`homework-backend.postman_collection.json`。

導入步驟：

1. 開啟 Postman。
2. 點擊左上角 `Import`。
3. 選擇專案根目錄底下的 `homework-backend.postman_collection.json`。
4. 匯入完成後，會看到 `homework-backend` collection。
5. 請先確認應用程式已啟動，並運行在 `http://127.0.0.1:8080`，再執行 collection 內的 API。

