# MySawit-AUTH

## Database Schema
https://app.eraser.io/workspace/UIuTCmf4r8F0oQXKQoRi

## Api Doc
https://docs.google.com/document/d/10HpIqgPsum000fG7jnK9GIEm32EbXZiKK6fvPFwZXpU/edit?usp=sharing

<details>
    <summary><h2>Monitoring</h2></summary>

![grafana](assets/grafana_1.png)
![grafana](assets/grafana_2.png)

Monitoring pada service ini dilakukan dengan stack berikut:

1. **Prometheus** untuk scrape metrik aplikasi (`job="auth-app"`) dari endpoint actuator/micrometer.
2. **Grafana** untuk visualisasi metrik operasional lewat dashboard **Auth Service Overview** (`monitoring/grafana/dashboards/auth-overview.json`).

Metrik yang dimonitor pada dashboard:

- **Service health & traffic**
  - Service Up
  - Request Rate (req/s)
  - Error Rate (5xx %)
  - Uptime
  - HTTP Request Rate by Status

- **Latency**
  - P95 Latency
  - P99 Latency
  - HTTP Latency p95/p99

- **JVM & process**
  - JVM Heap/Non-Heap Memory
  - JVM GC Pause (sum rate)
  - JVM Threads by State
  - Process CPU Usage
  - Open File Descriptors

- **Database connection pool (HikariCP)**
  - Hikari Connections (total, active, idle, pending)
  - Hikari Pool Usage %

Justifikasi kenapa metrik tersebut harus dimonitor:

- **Service health & traffic**
  - Untuk memastikan service benar-benar available (`Service Up`, `Uptime`).
  - Untuk melihat beban request real-time dan mendeteksi lonjakan trafik (`Request Rate`, `HTTP Request Rate by Status`).
  - Untuk mendeteksi indikasi gangguan secepat mungkin melalui kenaikan error 5xx (`Error Rate`).

- **Latency**
  - Untuk memastikan waktu respons tetap memenuhi target SLA/SLO.
  - P95/P99 dipakai karena lebih representatif terhadap pengalaman mayoritas user dibanding rata-rata, sekaligus menangkap kasus tail latency.

- **JVM & process**
  - Untuk mendeteksi pressure resource aplikasi Java (memori, GC, thread) sebelum berdampak ke timeout/error.
  - Untuk melihat bottleneck di level proses OS (CPU usage dan open file descriptors) yang bisa menyebabkan degradasi performa atau crash.

- **Database connection pool (HikariCP)**
  - Karena hampir semua operasi auth bergantung pada DB, exhaustion connection pool akan langsung menaikkan latency dan error.
  - Metrik active/idle/pending dan pool usage membantu menentukan apakah ukuran pool sudah tepat atau perlu tuning.

</details>
