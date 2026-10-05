
## سؤال ۱

**سؤال:**
در پروژه‌ای با بار پردازشی بالا، چگونه می‌توانید عملکرد Thread Pool در Java را بهینه و کنترل کنید؟

**راهنما:**
به تأثیر تعداد هسته‌های پردازنده و تفاوت وظایف محاسباتی و ورودی/خروجی توجه کنید.

**سطح:** Senior

### پاسخ

برای بهینه‌سازی Thread Pool ابتدا باید مشخص کنیم وظایف ما از چه نوعی هستند.

اگر وظایف **محاسباتی** باشند، یعنی بیشتر CPU را درگیر کنند، تعداد Threadها معمولاً باید نزدیک به تعداد هسته‌های CPU باشد. ایجاد Threadهای زیاد در این حالت باعث افزایش Context Switching و در نتیجه کاهش کارایی می‌شود.

برای مثال:

```java
int threads = Runtime.getRuntime().availableProcessors();

ExecutorService executor =
        Executors.newFixedThreadPool(threads);
```

اما اگر وظایف بیشتر **ورودی و خروجی** باشند، مثل دسترسی به Database، ارسال درخواست HTTP یا خواندن فایل، می‌توان تعداد Threadها را بیشتر از تعداد هسته‌های CPU در نظر گرفت؛ چون Thread بخش زیادی از زمان خود را در انتظار I/O است.

برای کنترل دقیق‌تر Thread Pool می‌توان از `ThreadPoolExecutor` استفاده کرد و مواردی مثل اندازه Pool، ظرفیت Queue و رفتار هنگام پر شدن Queue را مشخص کرد.

همچنین باید معیارهایی مثل مصرف CPU، اندازه Queue، تعداد Threadهای فعال، Throughput و Latency را مانیتور کنیم و اندازه Pool را بر اساس Benchmark واقعی تنظیم کنیم.

**نکته مهم:**
تعداد مناسب Thread فقط به تعداد هسته‌های CPU بستگی ندارد و نوع کار و رفتار واقعی سیستم نیز باید در نظر گرفته شود.

---

# سؤال ۲

**سؤال:**
در Java چه زمانی استفاده از Reflection توصیه می‌شود و چه معایبی دارد؟

**راهنما:**
به کاربردهای پویا و تأثیر آن بر عملکرد و امنیت توجه کنید.

**سطح:** Senior

### پاسخ

Reflection زمانی مناسب است که برنامه نیاز داشته باشد اطلاعات مربوط به کلاس‌ها، متدها یا فیلدها را در **زمان اجرا** بررسی یا استفاده کند.

از کاربردهای رایج آن می‌توان به موارد زیر اشاره کرد:

* Frameworkهایی مانند Spring
* سیستم‌های Dependency Injection
* ORM
* Serialization
* سیستم‌های Plugin

برای مثال می‌توان در زمان اجرا یک کلاس را پیدا و نمونه‌ای از آن ایجاد کرد:

```java
Class<?> clazz =
        Class.forName("com.example.MyService");

Object object =
        clazz.getDeclaredConstructor().newInstance();
```

اما Reflection معایبی هم دارد.

اول اینکه معمولاً نسبت به فراخوانی مستقیم، سربار بیشتری دارد و می‌تواند روی Performance تأثیر بگذارد.

دوم اینکه Type Safety کاهش پیدا می‌کند و بعضی خطاها به جای زمان Compile، در زمان Runtime مشخص می‌شوند.

همچنین استفاده زیاد از Reflection باعث می‌شود کد سخت‌تر قابل فهم و Refactor باشد.

از نظر امنیتی نیز باید دقت کرد، چون Reflection می‌تواند امکان دسترسی به اعضای داخلی کلاس‌ها را فراهم کند.

**نتیجه:**
Reflection زمانی مناسب است که رفتار Dynamic واقعاً موردنیاز باشد. برای منطق عادی برنامه، استفاده از کد معمولی و Type-safe معمولاً انتخاب مناسب‌تری است.

---

# سؤال ۳

**سؤال:**
در چه شرایطی استفاده از Java Memory Model اهمیت حیاتی پیدا می‌کند و چگونه می‌توان مشکلات مرتبط با آن را حل کرد؟

**راهنما:**
به تأثیر اجرای همزمان و اشتراک داده‌ها در محیط چندریسمانی فکر کنید.

**سطح:** Senior

### پاسخ

Java Memory Model زمانی اهمیت زیادی پیدا می‌کند که چند Thread به داده‌های مشترک دسترسی داشته باشند و بعضی از آن‌ها این داده‌ها را تغییر دهند.

در این شرایط سه مشکل اصلی وجود دارد:

**۱. Visibility**

ممکن است یک Thread مقدار یک متغیر را تغییر دهد، اما Thread دیگر این تغییر را به‌موقع مشاهده نکند.

برای بعضی سناریوها می‌توان از `volatile` استفاده کرد:

```java
volatile boolean running;
```

**۲. Atomicity**

بعضی عملیات‌ها مثل:

```java
counter++;
```

اتمیک نیستند.

برای چنین شرایطی می‌توان از کلاس‌های Atomic استفاده کرد:

```java
AtomicInteger counter =
        new AtomicInteger();

counter.incrementAndGet();
```

**۳. Ordering**

ترتیب اجرای عملیات بین Threadها همیشه دقیقاً مطابق ترتیب ظاهری دستورات برنامه نیست.

برای کنترل این موارد می‌توان از ابزارهایی مانند موارد زیر استفاده کرد:

* `synchronized`
* `volatile`
* `Lock`
* کلاس‌های `Atomic`
* Collectionهای Thread-safe
* Immutable Objectها

همچنین مفهوم مهم **Happens-Before** مشخص می‌کند که چه زمانی تغییرات انجام‌شده توسط یک Thread برای Thread دیگر قابل مشاهده هستند.

**نتیجه:**
JMM در برنامه‌های چندریسمانی برای کنترل Visibility، Atomicity و Ordering اهمیت دارد و باید با استفاده صحیح از Synchronization و ابزارهای Concurrent از Race Condition و مشکلات حافظه جلوگیری کرد.

---

# سؤال ۴

**سؤال:**
در طراحی معماری یک سیستم توزیع‌شده با حجم بالای داده، چه الگوهایی را برای ارتقای مقیاس‌پذیری و تحمل‌پذیری خطا پیشنهاد می‌دهید؟

**راهنما:**
به تفکیک مسئولیت‌ها و مدیریت خطا در محیط توزیع‌شده توجه کنید.

**سطح:** Senior

### پاسخ

برای افزایش مقیاس‌پذیری، ابتدا باید مسئولیت‌های مختلف سیستم را از یکدیگر جدا کنیم تا هر بخش بتواند مستقل از بخش‌های دیگر توسعه و Scale شود.

در این معماری می‌توان از روش‌های زیر استفاده کرد:

**مقیاس‌پذیری**

* اجرای چند Instance از هر سرویس
* استفاده از Load Balancer
* استفاده از Cache برای کاهش فشار روی Database
* استفاده از Message Broker مانند Kafka برای پردازش غیرهمزمان
* استفاده از Database Replication
* در حجم بسیار زیاد، استفاده از Partitioning یا Sharding

**تحمل‌پذیری خطا**

در سیستم توزیع‌شده باید فرض کنیم سرویس‌ها یا ارتباطات ممکن است دچار خطا شوند.

برای مدیریت این شرایط می‌توان از موارد زیر استفاده کرد:

* Timeout
* Retry همراه با Backoff
* Circuit Breaker
* Bulkhead
* Idempotency

همچنین وجود Logging، Monitoring و Distributed Tracing برای پیدا کردن خطا و Bottleneck در سیستم ضروری است.

**نتیجه:**
برای Scalability باید امکان Scale کردن مستقل اجزای سیستم را فراهم کنیم و برای Fault Tolerance نیز باید Failure را به‌عنوان یک اتفاق طبیعی در معماری در نظر گرفته و از الگوهایی مانند Timeout، Retry و Circuit Breaker استفاده کنیم.

---

# سؤال ۵

**سؤال:**
برای بهبود کارایی یک سرویس Java که با تأخیر بالا در پاسخ‌دهی مواجه است، چه روش‌هایی را در سطح معماری و کد پیشنهاد می‌کنید؟

**راهنما:**
به تفکیک راهکارهای سطح معماری و کد توجه کنید.

**سطح:** Senior

### پاسخ

قبل از هرگونه تغییر باید مشخص کنیم علت اصلی تأخیر کجاست. برای این کار می‌توان از Monitoring، Profiling و Distributed Tracing استفاده کرد.

سپس راهکارها را در دو سطح بررسی می‌کنیم.

### سطح معماری

در این سطح می‌توان از روش‌های زیر استفاده کرد:

* استفاده از Cache برای داده‌های پرتکرار
* بهینه‌سازی Queryهای Database و Indexها
* استفاده از Connection Pool
* انتقال عملیات غیرضروری به پردازش غیرهمزمان
* کاهش تعداد درخواست‌های بین سرویس‌ها
* استفاده از Load Balancing
* اجرای چند Instance از سرویس
* استفاده از Message Broker برای کارهای غیرهمزمان

### سطح کد

در سطح کد نیز می‌توان:

* Algorithm و Data Structure مناسب‌تری انتخاب کرد
* Queryهای غیرضروری را حذف کرد
* مشکل N+1 Query را برطرف کرد
* ایجاد Objectهای غیرضروری را کاهش داد
* Thread Pool را متناسب با نوع کار تنظیم کرد
* از Batch Processing برای عملیات متعدد استفاده کرد
* از Blocking غیرضروری در Threadها جلوگیری کرد
* با Profiler بخش‌های پرمصرف CPU و Memory را پیدا کرد

در نهایت باید قبل و بعد از تغییرات، معیارهایی مانند **Latency، Throughput، مصرف CPU و Memory** را اندازه‌گیری کنیم.

**نتیجه:**
در یک سرویس با Latency بالا، نباید صرفاً با حدس کد را تغییر دهیم. ابتدا Bottleneck را با ابزارهای اندازه‌گیری پیدا می‌کنیم و سپس در سطح مناسب، یعنی معماری، Database یا کد، آن را بهینه می‌کنیم.
# سؤال ۶

**سؤال:**
در یک پروژه Enterprise، چگونه می‌توانید قابلیت Maintainability سیستم را افزایش دهید؟

**راهنما:**
به تأثیر معماری، اصول شی‌گرایی و تست‌پذیری فکر کنید.

**سطح:** Senior

### پاسخ

برای افزایش `Maintainability` باید سیستم را طوری طراحی کنیم که تغییر دادن، تست کردن و توسعه دادن آن ساده باشد.

در سطح Architecture، باید مسئولیت‌ها را به‌درستی جدا کنیم و از `Separation of Concerns` و `Loose Coupling` استفاده کنیم. وابستگی بین Componentها باید حداقل باشد و هر Component مسئولیت مشخصی داشته باشد.

در سطح Object-Oriented Design نیز رعایت اصول `SOLID`، استفاده مناسب از `Abstraction` و `Dependency Injection` باعث می‌شود تغییرات یک بخش، تأثیر زیادی روی بخش‌های دیگر نداشته باشد.

از طرف دیگر، `Testability` اهمیت زیادی دارد. Componentها باید تا حد امکان مستقل و قابل تست باشند و برای آن‌ها `Unit Test`، `Integration Test` و در صورت نیاز `End-to-End Test` داشته باشیم.

همچنین `Clean Code`، Naming مناسب، مستندات مربوط به تصمیمات مهم معماری و جلوگیری از پیچیدگی غیرضروری، هزینه نگهداری سیستم را کاهش می‌دهد.

**نتیجه:**
`Maintainability` حاصل ترکیب Architecture مناسب، `Loose Coupling`، رعایت `SOLID`، `Dependency Injection`، `Clean Code` و تست‌پذیری مناسب است.

---

# سؤال ۷

**سؤال:**
چه ابزارهایی برای مانیتورینگ سرویس‌های Java در محیط Production پیشنهاد می‌کنید و چرا؟

**راهنما:**
به نیازهای جمع‌آوری داده، تحلیل و هشداردهی توجه کنید.

**سطح:** Senior

### پاسخ

برای Monitoring یک سرویس Java در Production بهتر است چند نوع داده را جمع‌آوری کنیم: `Metrics`، `Logs` و `Traces`.

برای `Metrics` می‌توان از `Micrometer` در سطح Application و `Prometheus` برای جمع‌آوری و ذخیره Metrics استفاده کرد. برای Visualization نیز `Grafana` گزینه رایجی است.

برای `Logs` می‌توان از ساختارهایی مانند `ELK Stack` یا `OpenSearch` استفاده کرد تا Logs از چند سرویس جمع‌آوری و قابل جست‌وجو باشند.

برای `Distributed Tracing` نیز `OpenTelemetry` و ابزارهایی مانند `Jaeger` یا `Tempo` قابل استفاده هستند.

در خود Java نیز ابزارهایی مانند `JFR`، `JMX` و `VisualVM` برای بررسی JVM، Memory، CPU، Threadها و سایر رفتارهای Runtime مفید هستند.

در کنار جمع‌آوری داده، باید `Alerting` نیز داشته باشیم؛ مثلاً برای افزایش `Error Rate`، `Latency`، `CPU Usage`، `Memory Usage` یا `GC Pause`.

**نتیجه:**
یک Monitoring مناسب فقط جمع‌آوری Metrics نیست؛ باید `Metrics + Logs + Traces + Alerting` را در کنار هم داشته باشیم تا هم وضعیت سیستم را ببینیم و هم بتوانیم علت مشکلات Production را پیدا کنیم.

---

# سؤال ۸

**سؤال:**
در مواجهه با Memory Leak در یک سرویس Java، مراحل شناسایی و رفع آن را شرح دهید.

**راهنما:**
به ابزارهای پروفایل و بررسی Heap توجه داشته باشید.

**سطح:** Senior

### پاسخ

ابتدا باید مشخص کنیم واقعاً با `Memory Leak` مواجه هستیم یا فقط `High Memory Usage` طبیعی داریم.

با Monitoring می‌توان روند `Heap Usage`، `GC Activity` و `Old Generation` را بررسی کرد. اگر بعد از چندین `GC`، مقدار زیادی Object همچنان در Heap باقی بماند و مصرف Memory به‌صورت مداوم افزایش پیدا کند، احتمال Memory Leak وجود دارد.

در مرحله بعد می‌توان با ابزارهایی مثل `JFR`، `JVisualVM`، `Eclipse MAT` یا `jcmd`، Heap و رفتار JVM را بررسی کرد.

معمولاً یک یا چند `Heap Dump` در زمان‌های مختلف گرفته می‌شود و در Heap Dump موارد زیر بررسی می‌شوند:

* تعداد Objectها
* مقدار Memory مصرفی
* `GC Roots`
* Referenceهای بین Objectها
* Objectهایی که غیرضروری در حافظه نگه داشته شده‌اند

برای مثال ممکن است یک `static Collection`، Cache بدون محدودیت، Listener ثبت‌شده یا ThreadLocal باعث شود Objectها دیگر قابل Garbage Collection نباشند.

بعد از پیدا کردن Reference مشکل‌ساز، باید Lifecycle آن اصلاح شود؛ مثلاً Cache محدود شود، Object از Collection حذف شود یا Resource به‌درستی Release شود.

در نهایت باید Fix را در محیط تست بررسی و با Load Test و Monitoring تأیید کنیم که Heap بعد از `GC` به وضعیت پایدار برمی‌گردد.

**نتیجه:**
فرآیند کلی عبارت است از:

`Monitor → Identify → Heap Dump → Analyze GC Roots → Fix References → Load Test → Verify`

---

# سؤال ۹

**سؤال:**
چگونه می‌توان با استفاده از مانیتورینگ، مشکلات Bottleneck را در یک سیستم Java شناسایی و تحلیل کرد؟

**راهنما:**
به اهمیت جمع‌آوری داده‌های عملکردی و تحلیل آن‌ها توجه کنید.

**سطح:** Senior

### پاسخ

برای پیدا کردن `Bottleneck` ابتدا باید Metrics مناسب را از تمام لایه‌های سیستم جمع‌آوری کنیم.

در سطح Application باید مواردی مانند `Request Rate`، `Error Rate`، `Latency` و `Throughput` را بررسی کنیم. بهتر است Latency را با Percentileهایی مانند `p95` و `p99` بررسی کنیم، نه فقط Average.

در سطح JVM موارد زیر مهم هستند:

* CPU Usage
* Heap Usage
* GC Frequency
* GC Pause Time
* Thread Count
* Thread Pool Queue
* Thread Contention

در Database نیز باید Queryهای کند، Connection Pool و تعداد Connectionهای فعال را بررسی کنیم.

اگر سیستم Distributed باشد، `Distributed Tracing` کمک می‌کند مشخص کنیم یک Request بیشتر زمان خود را در کدام Service، Database Query یا External API صرف کرده است.

برای مثال اگر `p99 Latency` افزایش پیدا کرده و همزمان Queue مربوط به Thread Pool نیز در حال افزایش باشد، می‌توان بررسی کرد آیا Pool کوچک است یا Threadها در یک عملیات Blocking منتظر مانده‌اند.

بنابراین Monitoring فقط برای مشاهده وضعیت سیستم نیست؛ باید از داده‌های آن برای پیدا کردن **Root Cause** استفاده کنیم.

**نتیجه:**
برای پیدا کردن Bottleneck باید Metrics را از Application، JVM، Database و Infrastructure جمع‌آوری کرده و با استفاده از Metrics، Profiling و Tracing مسیر ایجاد Latency را تا رسیدن به Root Cause دنبال کنیم.

---

# سؤال ۱۰

**سؤال:**
در پروژه‌های بزرگ، چگونه استراتژی تست خودکار را برای کاهش هزینه نگهداری و افزایش کیفیت توسعه می‌دهید؟

**راهنما:**
به سطوح مختلف تست و نقش آن‌ها در چرخه توسعه توجه کنید.

**سطح:** Senior

### پاسخ

در پروژه‌های بزرگ بهتر است یک `Test Pyramid` مناسب داشته باشیم و بیشترین تعداد تست‌ها در سطح پایین‌تر قرار بگیرند.

### Unit Test

برای تست منطق Business و Componentهای کوچک استفاده می‌شود. این تست‌ها باید سریع، مستقل و زیاد باشند.

### Integration Test

برای بررسی Integration بین Componentها مانند Database، Kafka، REST API و سایر Infrastructureها استفاده می‌شود.

### End-to-End Test

برای بررسی یک Flow کامل از ابتدا تا انتها استفاده می‌شود. این تست‌ها معمولاً کندتر و پیچیده‌تر هستند، بنابراین نباید تمام رفتار سیستم را فقط با E2E Test پوشش دهیم.

همچنین تست‌ها باید در `CI/CD Pipeline` اجرا شوند تا هر تغییر قبل از Merge یا Release بررسی شود.

برای کاهش هزینه نگهداری نیز باید تست‌ها:

* مستقل باشند
* Deterministic باشند
* وابستگی غیرضروری به Environment نداشته باشند
* Test Data قابل کنترل داشته باشند
* از Duplicate Test جلوگیری کنند

در کنار Functional Testing، در سیستم‌های مهم می‌توان `Contract Test`، `Performance Test` و `Security Test` را نیز اضافه کرد.

**نتیجه:**
یک استراتژی مناسب شامل `Unit Test` برای سرعت و پوشش بالا، `Integration Test` برای بررسی Integrationها و تعداد کنترل‌شده‌ای `E2E Test` برای بررسی Critical Flowهاست. اجرای خودکار آن‌ها در `CI/CD` باعث می‌شود خطاها زودتر شناسایی شوند و هزینه تغییرات کاهش پیدا کن
# سؤال ۱۱

**سؤال:**
در تست اتوماتیک یک سیستم توزیع‌شده، چه چالش‌هایی وجود دارد و چگونه باید آن‌ها را مدیریت کرد؟

**راهنما:**
به تفاوت تست سیستم‌های توزیع‌شده با سیستم‌های ساده توجه کنید.

**سطح:** Senior

### پاسخ

در سیستم‌های Distributed، تست پیچیده‌تر از یک سیستم ساده است، چون چند Service، Database، Message Broker و Network درگیر هستند و رفتار سیستم فقط به کد یک Service وابسته نیست.

مهم‌ترین چالش‌ها عبارت‌اند از:

* `Network Failure` و Latency
* اجرای غیرهمزمان و `Eventual Consistency`
* وابستگی بین Serviceها
* `Race Condition` و مشکلات مربوط به Concurrency
* سختی ایجاد یک Test Environment مشابه Production
* غیرقابل پیش‌بینی بودن بعضی تست‌ها و ایجاد `Flaky Test`

برای مدیریت این موارد باید تست‌ها را در چند سطح طراحی کنیم. با `Unit Test` منطق هر Service را مستقل تست می‌کنیم و با `Integration Test` ارتباط با Database، Kafka یا سایر Dependencyها را بررسی می‌کنیم.

برای ارتباط بین Serviceها می‌توان از `Contract Test` استفاده کرد تا تغییرات یک Service باعث شکستن Consumerها نشود.

همچنین باید Failureهای واقعی Distributed System مثل Timeout، Network Failure و Duplicate Message را نیز در تست‌ها شبیه‌سازی کنیم.

**نتیجه:**
در Distributed System فقط `Happy Path` کافی نیست. باید علاوه بر Business Logic، Failure، Concurrency، Asynchronous Processing و ارتباط بین Serviceها نیز تست شوند.

---

# سؤال ۱۲

**سؤال:**
در انتخاب ابزار تست اتوماتیک برای پروژه‌های Java، چه معیارهایی را باید مدنظر قرار داد؟

**راهنما:**
به تأثیر ابزار روی فرآیند توسعه و نگهداری تست‌ها فکر کنید.

**سطح:** Senior

### پاسخ

انتخاب Test Tool نباید فقط بر اساس محبوبیت آن انجام شود. مهم است که ابزار با نیازهای پروژه و فرآیند توسعه هماهنگ باشد.

معیارهای مهم عبارت‌اند از:

* پشتیبانی مناسب از Java و Frameworkهای مورد استفاده
* سادگی استفاده و `Learning Curve`
* سرعت اجرای تست‌ها
* قابلیت Integration با `CI/CD`
* قابلیت ایجاد و مدیریت Test Data
* پشتیبانی از Mocking و Integration Testing
* قابلیت Parallel Test Execution
* کیفیت گزارش‌ها و Debugging
* Stability و میزان ایجاد `Flaky Test`
* هزینه و License
* Community و Maintenance پروژه

همچنین باید هزینه نگهداری تست‌ها را در بلندمدت در نظر گرفت. ابزاری که در ابتدا استفاده از آن ساده است ولی تست‌های آن به‌سرعت شکننده و سخت برای Maintenance می‌شوند، انتخاب مناسبی برای یک پروژه Enterprise نیست.

**نتیجه:**
معیار اصلی انتخاب ابزار باید ترکیبی از `Developer Experience`، سرعت، قابلیت Integration، Stability و هزینه Maintenance باشد؛ نه صرفاً امکانات اولیه ابزار.

---

# سؤال ۱۳

**سؤال:**
در طراحی یک RESTful API با حجم بالای درخواست، چه اقداماتی برای افزایش کارایی و امنیت پیشنهاد می‌کنید؟

**راهنما:**
به تأثیر حجم درخواست‌ها و مخاطرات امنیتی توجه کنید.

**سطح:** Senior

### پاسخ

برای Performance باید ابتدا API را تا حد امکان `Stateless` طراحی کنیم تا بتوان چند Instance از آن اجرا کرد و با `Load Balancer` ترافیک را بین آن‌ها تقسیم کرد.

برای کاهش Load نیز می‌توان از `Caching`، مناسب‌سازی Database Queryها، Indexing و `Pagination` استفاده کرد. همچنین Response باید فقط شامل داده‌های موردنیاز باشد و در صورت امکان از `Compression` استفاده شود.

برای مدیریت حجم بالای درخواست‌ها باید `Rate Limiting` و در صورت نیاز `Backpressure` در نظر گرفته شود تا سیستم در زمان Peak Load دچار Overload نشود.

از نظر Security نیز موارد زیر ضروری هستند:

* `Authentication` و `Authorization`
* استفاده از HTTPS
* Validation ورودی‌ها
* جلوگیری از Injection
* محدود کردن اندازه Request
* جلوگیری از افشای اطلاعات حساس در Error Response
* مدیریت صحیح CORS در صورت نیاز

همچنین باید API را با Metricsهایی مانند `Request Rate`، `Error Rate` و `p95/p99 Latency` مانیتور کنیم.

**نتیجه:**
برای یک REST API پرترافیک باید همزمان `Scalability`، `Performance` و `Security` را در نظر بگیریم؛ یعنی Horizontal Scaling و Caching در کنار Rate Limiting، Authentication، Authorization و Input Validation.

---

# سؤال ۱۴

**سؤال:**
در چه شرایطی استفاده از SOAP نسبت به REST در پروژه‌های Java ترجیح داده می‌شود؟

**راهنما:**
به تفاوت‌های ساختاری و کاربردی این دو پروتکل دقت کنید.

**سطح:** Senior

### پاسخ

انتخاب بین SOAP و REST به نیازهای سیستم بستگی دارد.

`SOAP` یک Protocol مبتنی بر XML است و استانداردهای مشخصی برای قابلیت‌هایی مانند `WS-Security`، `WS-ReliableMessaging` و `WS-AtomicTransaction` دارد.

بنابراین در سیستم‌های Enterprise که به Contract رسمی، Security استاندارد و قابلیت‌های WS-* نیاز دارند، SOAP می‌تواند مناسب باشد. برای مثال در بعضی سیستم‌های بانکی و Integrationهای قدیمی Enterprise هنوز SOAP استفاده می‌شود.

در مقابل، `REST` معمولاً ساده‌تر و سبک‌تر است و به‌خوبی با HTTP و فرمت‌هایی مانند JSON کار می‌کند. برای Web APIها و سرویس‌هایی که نیاز به سادگی، Performance مناسب و Integration آسان دارند، REST انتخاب رایجی است.

یک تفاوت مهم دیگر این است که SOAP معمولاً Contract مشخصی با `WSDL` دارد، در حالی که REST محدود به یک Contract رسمی مشابه WSDL نیست.

**نتیجه:**
اگر نیاز اصلی سیستم به استانداردهای رسمی WS-*، Contract قوی و قابلیت‌هایی مانند WS-Security باشد، SOAP می‌تواند انتخاب مناسبی باشد. در APIهای معمول Web و Microserviceها، REST معمولاً ساده‌تر و سبک‌تر است.

---

# سؤال ۱۵

**سؤال:**
برای مدیریت خطا در سرویس‌های مبتنی بر REST، چه راهکارهایی پیشنهاد می‌شود؟

**راهنما:**
به اهمیت شفافیت و استاندارد بودن پاسخ خطا توجه کنید.

**سطح:** Senior

### پاسخ

در REST API باید Error Responseها استاندارد، قابل پیش‌بینی و برای Client قابل استفاده باشند.

ابتدا باید از HTTP Status Code مناسب استفاده کنیم. برای مثال:

```text
400 → Bad Request
401 → Unauthorized
403 → Forbidden
404 → Not Found
409 → Conflict
422 → Validation Error
500 → Internal Server Error
```

بهتر است ساختار Error Response در کل API یکسان باشد. برای مثال:

```json
{
  "code": "USER_NOT_FOUND",
  "message": "User does not exist",
  "traceId": "abc-123"
}
```

`message` نباید اطلاعات حساس یا جزئیات داخلی سیستم را افشا کند.

در سمت Server نیز Exceptionها باید به شکل مرکزی مدیریت شوند. در Spring Boot می‌توان از `@RestControllerAdvice` و `@ExceptionHandler` برای این کار استفاده کرد.

همچنین باید `Correlation ID` یا `Trace ID` در Response و Logs وجود داشته باشد تا بتوان یک خطا را در سیستم Distributed دنبال کرد.

برای خطاهای موقت مانند ارتباط با Service دیگر نیز می‌توان از `Timeout`، `Retry` و `Circuit Breaker` استفاده کرد؛ البته Retry باید با احتیاط و فقط برای عملیات مناسب انجام شود.

**نتیجه:**
Error Handling مناسب یعنی `Consistent Error Response + Correct HTTP Status Code + Centralized Exception Handling + Logging/Tracing` و در سیستم‌های Distributed، مدیریت صحیح Failureهای موقت نیز ضروری است.
د.


# سؤال ۱۶

**سؤال:**
در پروژه‌ای با استفاده از Spring Boot، چگونه می‌توانید Dependency Injection را بهینه و قابل تست نگه دارید؟

**راهنما:**
به تأثیر نوع Injection و تست‌پذیری کلاس‌ها توجه کنید.

**سطح:** Senior

### پاسخ

در Spring Boot بهتر است از **Constructor Injection** استفاده کنیم، چون Dependencyهای کلاس به‌صورت صریح مشخص می‌شوند و کلاس بدون داشتن Dependencyهای لازم نمی‌تواند ساخته شود.

مثلاً:

```java
@Service
public class OrderService {

    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

این روش نسبت به `Field Injection` مثل `@Autowired` روی Field، `Testability` بهتری دارد؛ چون در Unit Test می‌توان Dependency را مستقیماً به Constructor ارسال کرد و نیازی به Spring Context نیست.

همچنین Dependencyها باید تا حد امکان به Interface وابسته باشند و با `Dependency Inversion` از Coupling غیرضروری جلوگیری کنیم.

از طرف دیگر، نباید همه چیز را Bean کنیم یا Dependencyهای غیرضروری ایجاد کنیم، چون باعث پیچیده شدن Dependency Graph می‌شود.

**نتیجه:**
در Spring Boot، `Constructor Injection` به دلیل Explicit بودن Dependencyها، Immutable بودن Referenceها و Testability بهتر، انتخاب مناسبی است. همچنین استفاده از Interface و `Dependency Inversion` باعث کاهش Coupling و ساده‌تر شدن تست‌ها می‌شود.

---

# سؤال ۱۷

**سؤال:**
برای مدیریت Exceptionها در Spring چه راهکارهایی وجود دارد که هم قابل نگهداری و هم توسعه‌پذیر باشد؟

**راهنما:**
به مدیریت متمرکز و جداسازی منطق خطا فکر کنید.

**سطح:** Senior

### پاسخ

در Spring بهتر است Exception Handling به‌صورت **متمرکز** انجام شود و منطق مدیریت خطا از Business Logic جدا باشد.

در Spring Boot می‌توان از `@RestControllerAdvice` و `@ExceptionHandler` استفاده کرد تا Exceptionهای مختلف در یک محل مدیریت شوند.

مثلاً:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(
            OrderNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ORDER_NOT_FOUND"));
    }
}
```

همچنین بهتر است Exceptionهای مختلف بر اساس نوع خطا تفکیک شوند؛ مثلاً `Validation Exception`، `Business Exception` و `Technical Exception`.

`Error Response` نیز باید ساختار استانداردی داشته باشد و شامل اطلاعاتی مثل Error Code و Trace ID باشد، ولی نباید جزئیات حساس داخلی سیستم را به Client برگرداند.

**نتیجه:**
با استفاده از `Global Exception Handling`، تعریف Exceptionهای مشخص و استانداردسازی `Error Response`، منطق خطا از Business Logic جدا می‌شود و اضافه کردن Exceptionهای جدید ساده‌تر و قابل نگهداری‌تر خواهد بود.

---

# سؤال ۱۸

**سؤال:**
در پروژه‌های Enterprise، چه زمانی استفاده از Spring AOP منطقی و بهینه است؟

**راهنما:**
به مفهوم جداسازی Concernها و کاهش تکرار توجه کنید.

**سطح:** Senior

### پاسخ

`Spring AOP` زمانی مناسب است که یک Concern در بخش‌های مختلف سیستم تکرار می‌شود ولی جزو Business Logic اصلی آن‌ها نیست.

موارد رایج استفاده شامل:

* Logging
* Transaction Management
* Security
* Auditing
* Metrics
* Performance Monitoring

برای مثال اگر بخواهیم زمان اجرای متدهای مختلف را اندازه‌گیری کنیم، به‌جای قرار دادن کد Timing در تمام متدها، می‌توانیم یک Aspect ایجاد کنیم.

```java
@Around("@annotation(Monitored)")
public Object monitor(ProceedingJoinPoint joinPoint)
        throws Throwable {

    long start = System.currentTimeMillis();

    try {
        return joinPoint.proceed();
    } finally {
        // record execution time
    }
}
```

مزیت اصلی این کار `Separation of Concerns` و کاهش `Code Duplication` است.

البته نباید از AOP برای Business Logic پیچیده استفاده کرد، چون رفتار سیستم را از محل اصلی کد دور می‌کند و ممکن است Debugging و Maintenance را سخت‌تر کند.

**نتیجه:**
AOP برای `Cross-Cutting Concerns` مناسب است، نه برای Business Logic اصلی. استفاده صحیح از آن باعث کاهش Duplication و Separation بهتر مسئولیت‌ها می‌شود.

---

# سؤال ۱۹

**سؤال:**
در چه شرایطی استفاده از Singleton Pattern می‌تواند مشکلات جدی در پروژه ایجاد کند و راه‌حل چیست؟

**راهنما:**
به تأثیر Singleton در تست‌پذیری و همزمانی توجه کنید.

**سطح:** Senior

### پاسخ

`Singleton` زمانی مشکل‌ساز می‌شود که یک Object دارای **Mutable State** باشد و بخش‌های مختلف سیستم به همان Instance مشترک دسترسی داشته باشند.

در این حالت در محیط چندریسمانی ممکن است `Race Condition` و مشکلات مربوط به `Thread Safety` ایجاد شود.

همچنین Singleton یک Global State ایجاد می‌کند که باعث افزایش Coupling و کاهش `Testability` می‌شود. برای مثال در Unit Test ممکن است State تست قبلی روی تست بعدی تأثیر بگذارد.

راه‌حل این است که تا حد امکان Objectها را `Stateless` یا Immutable طراحی کنیم و Dependencyها را از طریق `Dependency Injection` دریافت کنیم.

در Spring نیز معمولاً Beanها به‌صورت Singleton Scope هستند، اما این به معنی آن نیست که Bean Singleton باید State مشترک و Mutable داشته باشد. Serviceهای Singleton بهتر است تا حد امکان Stateless باشند.

اگر State واقعاً لازم باشد، باید Thread Safety و Lifecycle آن به‌صورت مشخص مدیریت شود.

**نتیجه:**
مشکل اصلی Singleton صرفاً Singleton بودن نیست؛ ترکیب آن با `Global Mutable State` باعث مشکلاتی مانند Thread Safety، Coupling و Testability ضعیف می‌شود. استفاده از `Dependency Injection` و طراحی Stateless یا Immutable راهکار مناسب‌تری است.

---

# سؤال ۲۰

**سؤال:**
در پروژه‌ای با نیاز به توسعه‌پذیری بالا، استفاده از Strategy Pattern چه مزایایی دارد؟

**راهنما:**
به مفهوم جداسازی رفتارها و سهولت افزودن قابلیت جدید توجه کنید.

**سطح:** Senior

### پاسخ

`Strategy Pattern` زمانی مفید است که برای یک عملیات، چند الگوریتم یا رفتار مختلف داشته باشیم و بخواهیم این رفتارها را از منطق اصلی جدا کنیم.

برای مثال فرض کنیم روش‌های مختلف محاسبه هزینه ارسال داریم:

```java
public interface ShippingStrategy {
    BigDecimal calculate(Order order);
}
```

سپس هر رفتار در یک Class جدا قرار می‌گیرد:

```java
public class NormalShipping implements ShippingStrategy {
    // ...
}

public class ExpressShipping implements ShippingStrategy {
    // ...
}
```

کلاس اصلی به Interface وابسته است، نه به Implementation خاص:

```java
public class ShippingService {

    private final ShippingStrategy strategy;

    public ShippingService(ShippingStrategy strategy) {
        this.strategy = strategy;
    }
}
```

مزیت اصلی این Pattern، `Separation of Behavior` و کاهش `Conditional Logic`هایی مثل `if/else` یا `switch`های بزرگ است.

همچنین اضافه کردن Strategy جدید معمولاً بدون تغییر در منطق موجود انجام می‌شود و در نتیجه `Open/Closed Principle` بهتر رعایت می‌شود.

هر Strategy نیز می‌تواند به‌صورت مستقل Unit Test شود.

**نتیجه:**
`Strategy Pattern` رفتارهای مختلف را از منطق اصلی جدا می‌کند، باعث کاهش Conditional Logic می‌شود و اضافه کردن رفتار جدید را ساده‌تر می‌کند. همچنین Testability و Maintainability سیستم را افزایش می‌دهد.
# سؤال 21

**سؤال:** در چه شرایطی استفاده از Observer Pattern در پروژه‌های جاوا توصیه می‌شود؟

**راهنما:** به نیاز به اطلاع‌رسانی همزمان به چند بخش فکر کنید.

**سطح:** Senior

### پاسخ

`Observer Pattern` زمانی مناسب است که تغییر وضعیت یک Object باید باعث اطلاع‌رسانی به چند Object دیگر شود، بدون اینکه Object اصلی به آن‌ها وابستگی مستقیم داشته باشد.

در این Pattern یک `Subject` وضعیت یا Event را منتشر می‌کند و چند `Observer` به آن Subscribe می‌شوند.

مثلاً در یک سیستم سفارش، بعد از ثبت Order ممکن است چند بخش نیاز به واکنش داشته باشند:

* ارسال Notification
* ثبت Audit Log
* به‌روزرسانی Inventory
* ارسال Event به سیستم‌های دیگر

به‌جای اینکه `OrderService` مستقیماً همه این سرویس‌ها را صدا بزند، می‌توان Event منتشر کرد و Observerها آن را دریافت کنند.

نمونه ساده:

```java
public interface OrderObserver {
    void onOrderCreated(Order order);
}
```

و:

```java
public class NotificationObserver implements OrderObserver {

    @Override
    public void onOrderCreated(Order order) {
        // send notification
    }
}
```

مزیت اصلی این Pattern، **Loose Coupling** و جدا کردن Publisher از Consumerها است.

در Java/Spring معمولاً همین مفهوم با `ApplicationEventPublisher` و `@EventListener` یا در معماری‌های بزرگ‌تر با Message Brokerهایی مثل Kafka پیاده‌سازی می‌شود.

نکته مهم این است که اگر تعداد Observerها زیاد باشد یا پردازش آن‌ها سنگین باشد، اجرای synchronous می‌تواند latency را افزایش دهد. در چنین شرایطی می‌توان از asynchronous processing استفاده کرد.

**نتیجه:**
`Observer Pattern` برای زمانی مناسب است که یک تغییر یا Event باید به چند Consumer اطلاع داده شود و می‌خواهیم Publisher بدون وابستگی مستقیم به Consumerها باقی بماند.

---

# سؤال 22

**سؤال:** در مدیریت منابع و بهینه‌سازی مصرف حافظه در جاوا، چه تکنیک‌هایی را به کار می‌برید؟

**راهنما:** به اهمیت آزادسازی منابع و مدیریت چرخه عمر آن‌ها توجه کنید.

**سطح:** Senior

### پاسخ

در Java، مدیریت Memory تا حد زیادی توسط `Garbage Collector` انجام می‌شود، اما منابعی مثل `Database Connection`، `File` و `Socket` باید به‌صورت صحیح مدیریت و آزاد شوند.

مهم‌ترین تکنیک‌ها:

1. **استفاده از try-with-resources**

برای منابعی که `AutoCloseable` هستند:

```java
try (InputStream input = Files.newInputStream(path)) {
    // use resource
}
```

با خروج از block، resource به‌صورت خودکار `close` می‌شود.

2. **مدیریت صحیح Connection Pool**

مثلاً در Database نباید Connectionها را بدون `close` رها کرد؛ معمولاً `close()` در Pool باعث بازگشت Connection به Pool می‌شود.

3. **جلوگیری از نگه‌داشتن Referenceهای غیرضروری**

برای مثال:

```java
static List<Object> cache = new ArrayList<>();
```

اگر این collection بدون محدودیت رشد کند، Objectها همچنان از طریق `GC Roots` قابل دسترسی هستند و Garbage Collector نمی‌تواند آن‌ها را جمع کند.

4. **مدیریت Cache**

Cache باید معمولاً دارای محدودیت‌هایی مثل:

* Maximum Size
* TTL
* Eviction Policy

باشد.

5. **جلوگیری از Object Allocation غیرضروری**

در مسیرهای حساس از نظر Performance، ایجاد مداوم Objectهای غیرضروری می‌تواند باعث افزایش فشار روی GC شود.

6. **Monitoring و Profiling**

برای مشکلات واقعی Memory باید Heap، GC و Object Allocation را بررسی کرد. ابزارهایی مثل `JFR`، `JDK Mission Control` و `Heap Dump` برای تحلیل مفید هستند.

نکته مهم این است که در Java معمولاً نباید با `System.gc()` سعی کنیم Memory را به‌صورت دستی مدیریت کنیم. مسئله اصلی مدیریت صحیح Referenceها و Lifecycle منابع است.

**نتیجه:**
مدیریت Memory فقط به Garbage Collector محدود نیست؛ باید Lifecycle منابع، `try-with-resources`، Cache، Object Allocation و Referenceهای غیرضروری را کنترل و با ابزارهای Profiling بررسی کرد.

---

# سؤال 23

**سؤال:** در پروژه‌های بزرگ، چگونه می‌توان از ویژگی‌های جدید جاوا (مانند Stream API یا Optional) برای بهبود خوانایی و کارایی کد استفاده کرد؟

**راهنما:** به تاثیر سبک برنامه‌نویسی تابعی و مدیریت Null اشاره کنید.

**سطح:** Senior

### پاسخ

ویژگی‌هایی مانند `Stream API` و `Optional` می‌توانند کد را خواناتر و Declarative کنند، اما نباید صرفاً به دلیل جدید بودن از آن‌ها استفاده کرد.

### Stream API

`Stream` برای پردازش مجموعه‌ای از داده‌ها به‌صورت Declarative مناسب است.

مثلاً:

```java
List<String> activeNames = users.stream()
        .filter(User::isActive)
        .map(User::getName)
        .toList();
```

مزیت این روش این است که منطق `filter` و `map` واضح‌تر از حلقه‌های تو در تو بیان می‌شود.

همچنین عملیات‌هایی مانند `filter`، `map` و `reduce` امکان استفاده از سبک Functional Programming را فراهم می‌کنند.

اما `Stream` الزاماً سریع‌تر از `for` loop نیست. برای Collectionهای کوچک یا منطق پیچیده، loop ساده ممکن است خواناتر یا حتی کاراتر باشد.

همچنین استفاده بی‌دلیل از `parallelStream()` می‌تواند به دلیل overhead و استفاده از `ForkJoinPool` باعث افت Performance شود.

### Optional

`Optional` برای نمایش واضح احتمال نبودن یک مقدار، مخصوصاً در `return type`، مناسب است:

```java
Optional<User> findById(Long id)
```

و:

```java
return userRepository.findById(id)
        .map(User::getName)
        .orElse("Unknown");
```

این کار می‌تواند بسیاری از `null` checkهای تکراری را کاهش دهد.

اما `Optional` معمولاً برای همه‌جا مناسب نیست. استفاده افراطی در `Entity`، `DTO`، Fieldها یا Method Parameterها می‌تواند کد را پیچیده‌تر کند.

همچنین `Optional.get()` بدون بررسی وجود مقدار، عملاً می‌تواند همان مشکل Runtime Exception را ایجاد کند:

```java
optional.get();
```

بهتر است از `map`، `flatMap`، `orElse`، `orElseGet` یا `orElseThrow` متناسب با سناریو استفاده شود.

**نتیجه:**
`Stream API` برای پردازش Declarative و خواناتر Collectionها و `Optional` برای بیان و مدیریت بهتر نبودن مقدار مناسب هستند؛ اما استفاده از آن‌ها باید بر اساس Readability، Performance و Context باشد، نه صرفاً استفاده از Featureهای جدید Java.

---

# سؤال 24

**سؤال:** چه تفاوت‌هایی بین HashMap و ConcurrentHashMap وجود دارد و در چه شرایطی باید از هرکدام استفاده کرد؟

**راهنما:** به تفاوت پیاده‌سازی و کاربرد در محیط‌های مختلف توجه کنید.

**سطح:** Senior

### پاسخ

تفاوت اصلی این است که `HashMap` برای استفاده معمولی و `ConcurrentHashMap` برای دسترسی concurrent توسط چند Thread طراحی شده است.

### HashMap

`HashMap` Thread-safe نیست.

اگر چند Thread به یک `HashMap` دسترسی داشته باشند و حداقل یکی از آن‌ها Map را تغییر دهد، بدون synchronization مناسب، رفتار برنامه می‌تواند غیرقابل‌پیش‌بینی باشد.

در Single-thread یا زمانی که Map به‌صورت مناسب خارج از دسترس concurrent قرار دارد، `HashMap` انتخاب مناسبی است.

### ConcurrentHashMap

`ConcurrentHashMap` برای دسترسی همزمان چند Thread طراحی شده و امکان `get`، `put` و سایر عملیات را با concurrency مناسب فراهم می‌کند.

مثلاً:

```java
ConcurrentHashMap<String, Integer> counters =
        new ConcurrentHashMap<>();

counters.merge("java", 1, Integer::sum);
```

این نوع عملیات می‌تواند بدون اینکه خودمان کل Map را synchronize کنیم، به‌صورت thread-safe انجام شود.

یکی از نکات مهم این است که `ConcurrentHashMap` اجازه `null` به‌عنوان `key` یا `value` را نمی‌دهد.

همچنین باید توجه داشت که thread-safe بودن خود Map به این معنی نیست که **تمام عملیات ترکیبی روی آن به‌صورت خودکار atomic هستند**. برای چنین سناریوهایی باید از APIهای atomic مانند `compute`، `merge` یا synchronization مناسب استفاده کرد.

مثلاً این الگو ممکن است از نظر منطقی مشکل داشته باشد:

```java
if (!map.containsKey(key)) {
    map.put(key, value);
}
```

بهتر است از operationهای atomic خود `ConcurrentHashMap` استفاده شود:

```java
map.putIfAbsent(key, value);
```

**نتیجه:**
`HashMap` برای استفاده غیرهمزمان مناسب است و overhead کمتری دارد. `ConcurrentHashMap` زمانی استفاده می‌شود که چند Thread باید به‌صورت concurrent به Map دسترسی داشته باشند. برای عملیات چندمرحله‌ای نیز باید از APIهای atomic یا synchronization مناسب استفاده کرد.

---

# سؤال 25

**سؤال:** در طراحی کلاس‌های شی‌گرا، چگونه می‌توان اصل Open/Closed را به طور مؤثر اجرا کرد؟

**راهنما:** به مفهوم توسعه بدون تغییر کد موجود توجه کنید.

**سطح:** Senior

### پاسخ

اصل `Open/Closed Principle` می‌گوید یک کلاس یا Module باید:

> **Open for Extension, Closed for Modification**

باشد.

یعنی بتوانیم رفتار جدیدی به سیستم اضافه کنیم، بدون اینکه برای هر قابلیت جدید مجبور شویم کد موجود و تست‌شده را تغییر دهیم.

یکی از روش‌های رایج، استفاده از `Abstraction` و `Polymorphism` است.

مثلاً فرض کنیم سیستم روش‌های مختلف محاسبه هزینه ارسال دارد:

```java
public interface ShippingStrategy {
    BigDecimal calculate(Order order);
}
```

حالا می‌توانیم رفتارهای مختلف ایجاد کنیم:

```java
public class NormalShipping implements ShippingStrategy {
    // ...
}

public class ExpressShipping implements ShippingStrategy {
    // ...
}
```

سرویس اصلی به Interface وابسته است:

```java
public class ShippingService {

    private final ShippingStrategy strategy;

    public ShippingService(ShippingStrategy strategy) {
        this.strategy = strategy;
    }
}
```

اگر بعداً `InternationalShipping` اضافه شود، می‌توانیم Implementation جدیدی ایجاد کنیم بدون اینکه منطق `NormalShipping` یا `ExpressShipping` را تغییر دهیم.

این اصل معمولاً با مفاهیمی مثل:

* `Interface`
* `Abstraction`
* `Polymorphism`
* `Dependency Injection`
* `Strategy Pattern`

پیاده‌سازی می‌شود.

در مقابل، چنین کدی نشانه طراحی کمتر قابل توسعه است:

```java
if (type == NORMAL) {
    // ...
} else if (type == EXPRESS) {
    // ...
} else if (type == INTERNATIONAL) {
    // ...
}
```

هر بار که نوع جدیدی اضافه شود، باید همین کلاس را تغییر دهیم و احتمالاً دوباره تست کنیم.

البته OCP به این معنی نیست که **هیچ‌وقت نباید کد موجود را تغییر دهیم**. هدف این است که نقاطی که احتمال تغییر و Extension در آن‌ها بالاست، با Abstraction مناسب طراحی شوند؛ نه اینکه برای هر تغییر ساده چندین Interface و Class اضافی ایجاد کنیم.

**نتیجه:**
برای اجرای OCP باید رفتارهای قابل تغییر را پشت `Abstraction` قرار دهیم و با `Polymorphism` و `Dependency Injection` امکان اضافه‌کردن Implementation جدید را بدون تغییر منطق موجود فراهم کنیم.


# سؤال 26

**سؤال:** در چه شرایطی ترکیب (Composition) نسبت به وراثت (Inheritance) ارجحیت دارد؟

**راهنما:** به مشکلات وراثت چندسطحی و نیاز به تغییر رفتار فکر کنید.

**سطح:** Senior

### پاسخ

`Composition` زمانی نسبت به `Inheritance` مناسب‌تر است که بخواهیم رفتار یک کلاس را **قابل تغییر و قابل ترکیب** نگه داریم و نخواهیم به یک hierarchy پیچیده وابسته شویم.

در `Inheritance`، کلاس فرزند به ساختار و رفتار کلاس والد وابسته می‌شود:

```java
class Car extends Vehicle {
}
```

اگر hierarchy چندسطحی شود، تغییر در Parent می‌تواند روی چندین Child اثر بگذارد و فهم و نگهداری سیستم سخت‌تر شود.

در `Composition`، به‌جای اینکه رفتار را از Parent به ارث ببریم، Object موردنیاز را داخل کلاس قرار می‌دهیم:

```java
class Car {

    private final Engine engine;

    Car(Engine engine) {
        this.engine = engine;
    }
}
```

یکی از مزیت‌های مهم Composition این است که می‌توانیم Implementation را تغییر دهیم:

```java
class Car {
    private Engine engine;

    void setEngine(Engine engine) {
        this.engine = engine;
    }
}
```

مثلاً می‌توان `GasolineEngine` یا `ElectricEngine` را بدون تغییر hierarchy اصلی استفاده کرد.

Composition معمولاً در شرایط زیر ترجیح داده می‌شود:

* رفتار باید در Runtime قابل تغییر باشد
* چند رفتار مختلف باید با هم ترکیب شوند
* Inheritance hierarchy در حال پیچیده شدن است
* رابطه واقعی `is-a` بین دو کلاس وجود ندارد
* می‌خواهیم Coupling کمتری داشته باشیم
* می‌خواهیم Unit Testing ساده‌تر باشد

البته Inheritance زمانی مناسب است که رابطه واقعی و پایدار `is-a` داشته باشیم و Polymorphism از طریق Parent abstraction واقعاً مورد نیاز باشد.

**نتیجه:**
`Composition` معمولاً زمانی ارجح است که تغییرپذیری رفتار و کاهش Coupling مهم باشد. در مقابل، `Inheritance` برای روابط واقعی و پایدار `is-a` مناسب‌تر است. به همین دلیل در طراحی شی‌گرا معمولاً اصل **Favor Composition over Inheritance** مطرح می‌شود.

---

# سؤال 27

**سؤال:** در پروژه‌های بزرگ، چگونه می‌توان وابستگی بین کلاس‌ها را کاهش داد؟

**راهنما:** به تاثیر انتزاع و تزریق وابستگی در کاهش Coupling توجه کنید.

**سطح:** Senior

### پاسخ

برای کاهش وابستگی بین کلاس‌ها باید `Coupling` را کاهش داده و هر کلاس را به **Abstraction** مناسب وابسته کنیم، نه به Implementationهای concrete.

یکی از روش‌های اصلی استفاده از `Interface` است:

```java
public interface PaymentService {
    void pay(Order order);
}
```

کلاس مصرف‌کننده:

```java
public class OrderService {

    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

در این حالت `OrderService` به `PaymentService` وابسته است، نه به مثلاً `BankPaymentService`.

این موضوع همراه با `Dependency Injection` باعث می‌شود Implementation را راحت‌تر تغییر دهیم و در Test نیز Mock یا Fake مناسب تزریق کنیم.

روش‌های مهم دیگر:

* `Dependency Inversion Principle`
* استفاده مناسب از `Interface` و `Abstraction`
* `Constructor Injection`
* `Separation of Concerns`
* جلوگیری از وابستگی مستقیم بین Layerها
* کاهش وابستگی‌های غیرضروری
* استفاده از Eventها برای بعضی ارتباطات غیرمستقیم

نکته مهم این است که **هر Interface الزاماً باعث کاهش Coupling نمی‌شود**. اگر Abstraction صرفاً برای پوشاندن یک Implementation ایجاد شده باشد ولی وابستگی مفهومی همچنان وجود داشته باشد، ارزش زیادی ایجاد نمی‌کند.

**نتیجه:**
برای کاهش Coupling، کلاس‌ها باید تا حد امکان به Abstraction وابسته باشند و وابستگی‌هایشان از طریق `Dependency Injection` تأمین شود. در کنار آن، `Separation of Concerns` و `Dependency Inversion` نقش مهمی دارند.

---

# سؤال 28

**سؤال:** در مواجهه با داده‌های حجیم، چگونه الگوریتم جستجو را بهینه می‌کنید؟

**راهنما:** به تاثیر مرتب‌سازی و انتخاب الگوریتم مناسب توجه کنید.

**سطح:** Senior

### پاسخ

اول باید مشخص کنیم داده‌ها **مرتب هستند یا خیر** و آیا جستجو فقط یک‌بار انجام می‌شود یا قرار است تعداد زیادی Search روی همان داده‌ها انجام شود.

اگر داده‌ها مرتب باشند، می‌توان از `Binary Search` استفاده کرد:

```text
Linear Search  → O(n)
Binary Search  → O(log n)
```

در `Binary Search` در هر مرحله تقریباً نیمی از فضای Search حذف می‌شود.

مثلاً:

```java
int index = Collections.binarySearch(numbers, target);
```

اما اگر داده‌ها مرتب نباشند، `Binary Search` قابل استفاده نیست و مرتب‌سازی اولیه هزینه دارد:

```text
Sorting → O(n log n)
Binary Search → O(log n) per search
```

بنابراین اگر فقط یک Search داشته باشیم، Sorting کردن کل داده ممکن است اصلاً ارزش نداشته باشد.

اگر Search بر اساس `key` زیاد انجام می‌شود، می‌توان از Data Structure مناسب مثل `HashMap` استفاده کرد:

```java
Map<Long, User> usersById = new HashMap<>();
```

در حالت معمول، Lookup در `HashMap` به‌طور میانگین `O(1)` است.

بنابراین انتخاب الگوریتم به شرایط بستگی دارد:

| شرایط                        | راهکار                       |
| ---------------------------- | ---------------------------- |
| داده unsorted و Search محدود | `Linear Search`              |
| داده sorted                  | `Binary Search`              |
| Lookupهای زیاد بر اساس Key   | `HashMap`                    |
| Range Query روی داده مرتب    | ساختارهای مرتب مثل `TreeMap` |
| داده بسیار حجیم در Database  | `Index` و Query Optimization |

در سیستم‌های واقعی باید علاوه بر `Time Complexity`، مواردی مثل `Memory Usage`، هزینه Sorting، نوع Query و محل داده را هم در نظر گرفت.

**نتیجه:**
برای بهینه‌سازی Search ابتدا باید الگوی دسترسی به داده مشخص شود. `Binary Search` برای داده مرتب، `HashMap` برای Key-based Lookup و در Database معمولاً `Index` راهکارهای مهم هستند. انتخاب نهایی باید بر اساس Complexity و الگوی واقعی استفاده باشد.

---

# سؤال 29

**سؤال:** در چه شرایطی استفاده از الگوریتم‌های موازی (Parallel Algorithms) در جاوا توصیه می‌شود؟

**راهنما:** به قابلیت تقسیم وظایف و منابع سخت‌افزاری توجه کنید.

**سطح:** Senior

### پاسخ

`Parallel Algorithm` زمانی مناسب است که یک مسئله بتواند به چند Task مستقل تقسیم شود و سیستم نیز منابع CPU کافی برای اجرای همزمان آن‌ها داشته باشد.

مثلاً اگر یک مجموعه بزرگ از داده‌ها داشته باشیم و پردازش هر Element مستقل باشد، می‌توان پردازش را بین چند Thread تقسیم کرد.

در Java یکی از راه‌های ساده استفاده از `parallelStream()` است:

```java
numbers.parallelStream()
        .map(this::calculate)
        .toList();
```

اما Parallel همیشه سریع‌تر نیست.

هزینه‌هایی مانند:

* ایجاد و مدیریت Taskها
* `Context Switching`
* Synchronization
* تقسیم و ترکیب داده‌ها
* Memory overhead

وجود دارد.

اگر Dataset کوچک باشد یا عملیات هر Element بسیار سریع باشد، این overhead ممکن است بیشتر از سود Parallelism باشد.

همچنین `parallelStream()` برای عملیات `I/O-bound` معمولاً انتخاب مناسبی نیست؛ چون `ForkJoinPool` برای CPU-oriented parallelism طراحی شده و Blocking I/O می‌تواند Workerها را معطل کند.

برای کارهای CPU-bound، مستقل و سنگین، روی سیستم چند هسته‌ای، Parallelism می‌تواند مفید باشد.

قبل و بعد از Parallelization نیز باید با Benchmark واقعی اندازه‌گیری کنیم:

```text
Sequential
    ↓
Measure
    ↓
Parallel
    ↓
Measure again
```

**نتیجه:**
Parallel Algorithms زمانی مناسب‌اند که Taskها مستقل و قابل تقسیم باشند، workload به‌اندازه کافی بزرگ باشد و CPU resources کافی وجود داشته باشد. `parallelStream()` نباید صرفاً به دلیل داشتن CPU چند هسته‌ای استفاده شود؛ باید با Benchmark اثبات شود که واقعاً Performance بهتر شده است.

---

# سؤال 30

**سؤال:** در بهینه‌سازی الگوریتم‌های مرتب‌سازی، چه پارامترهایی را باید در نظر گرفت؟

**راهنما:** به تاثیر نوع داده و حجم ورودی روی انتخاب الگوریتم دقت کنید.

**سطح:** Senior

### پاسخ

انتخاب الگوریتم Sorting فقط بر اساس سرعت اسمی آن نیست و باید چند عامل را همزمان در نظر گرفت.

مهم‌ترین پارامترها:

### 1. حجم داده

برای Datasetهای بزرگ، `Time Complexity` اهمیت زیادی دارد.

مثلاً:

```text
Bubble Sort → O(n²)
Merge Sort  → O(n log n)
Quick Sort  → O(n log n) average
```

بنابراین برای داده‌های بزرگ معمولاً الگوریتم‌های `O(n log n)` مناسب‌تر هستند.

### 2. نوع داده و Distribution

خصوصیات داده می‌تواند روی Performance اثر بگذارد. مثلاً بعضی الگوریتم‌ها روی داده‌هایی که تقریباً مرتب هستند عملکرد متفاوتی دارند.

### 3. Memory

بعضی الگوریتم‌ها `In-place` هستند و Memory اضافی بسیار کمی نیاز دارند، در حالی که مثلاً `Merge Sort` معمولاً به فضای اضافی نیاز دارد.

### 4. Stability

اگر دو Element مقدار Sort یکسان داشته باشند، در `Stable Sort` ترتیب قبلی آن‌ها حفظ می‌شود.

این ویژگی زمانی مهم است که چند مرحله Sorting داشته باشیم.

### 5. تعداد دفعات Sorting

اگر داده مرتب شده و قرار است چندین بار Search انجام شود، هزینه اولیه Sorting ممکن است کاملاً توجیه‌پذیر باشد.

### 6. نوع Storage

برای داده‌های بسیار بزرگ که در Memory جا نمی‌شوند، ممکن است به `External Sorting` نیاز داشته باشیم.

### 7. Parallelism

برای Datasetهای بسیار بزرگ روی سیستم‌های چند هسته‌ای، در شرایط مناسب می‌توان از Parallel Sorting استفاده کرد.

در Java نیز بهتر است در بسیاری از موارد به‌جای پیاده‌سازی الگوریتم از صفر، از APIهای استاندارد مثل:

```java
Arrays.sort(array);
Collections.sort(list);
```

یا `List.sort()` استفاده کنیم؛ چون Implementationهای استاندارد Java برای انواع مختلف داده بهینه شده‌اند.

**نتیجه:**
در انتخاب Sorting Algorithm باید `Input Size`، `Time Complexity`، `Memory Usage`، `Stability`، وضعیت داده، تعداد دفعات Sorting و امکان Parallelization را در نظر گرفت. انتخاب درست به Context مسئله وابسته است، نه فقط به Big-O.


# سؤال 31

سؤال: در پروژه‌ای که نیاز به جستجوی سریع داده‌ها دارد، چه ساختار داده‌ای را پیشنهاد می‌دهید و چرا؟

راهنما: به نوع جستجو و نیاز به ترتیب یا سرعت توجه کنید.

سطح: Senior

### پاسخ

انتخاب Data Structure مناسب به نوع جستجو، حجم داده، نیاز به مرتب‌سازی و الگوی دسترسی بستگی دارد. یک ساختار داده برای تمام سناریوها بهترین نیست.

ساختارهای مهم عبارت‌اند از:

* HashMap: برای جستجو بر اساس Key با پیچیدگی زمانی میانگین `O(1)`.

* TreeMap: برای نگهداری داده‌ها به‌صورت مرتب و انجام `Range Query` با پیچیدگی معمول `O(log n)`.

* ArrayList: برای دسترسی مستقیم با Index در `O(1)`؛ اما جستجوی یک Element بر اساس مقدار معمولاً `O(n)` است.

* Binary Search: برای جستجو در داده‌های مرتب با پیچیدگی `O(log n)`.

* HashSet: برای بررسی وجود یک Element با پیچیدگی زمانی میانگین `O(1)`.

برای مثال، اگر بخواهیم کاربر را بر اساس ID پیدا کنیم، `HashMap` انتخاب مناسبی است:

Java

```
Map<Long, User> usersById = new HashMap<>();

usersById.put(user.getId(), user);

User user = usersById.get(userId);
```

اما اگر به داده‌های مرتب‌شده بر اساس Key و جستجوی بازه‌ای نیاز داشته باشیم، `TreeMap` مناسب‌تر است.

نکته مهم این است که پیچیدگی زمانی به‌تنهایی کافی نیست. مصرف حافظه، هزینه درج و حذف، تعداد جستجوها و نیاز به Thread Safety نیز باید بررسی شوند.

همچنین پیچیدگی `O(1)` در `HashMap` میانگین است، نه تضمین مطلق برای همه شرایط.

نتیجه: برای جستجو بر اساس Key معمولاً `HashMap`، برای داده‌های مرتب و `Range Query`، `TreeMap` و برای جستجو در داده مرتب، `Binary Search` انتخاب مناسبی است. انتخاب نهایی به الگوی دسترسی و نیازهای سیستم بستگی دارد.

# سؤال 32

سؤال: در چه شرایطی استفاده از LinkedList نسبت به ArrayList بهینه‌تر است؟

راهنما: به تفاوت عملکرد در عملیات مختلف توجه کنید.

سطح: Senior

### پاسخ

تفاوت اصلی `ArrayList` و `LinkedList` در نحوه ذخیره‌سازی داده‌ها است.

* `ArrayList` از یک آرایه داخلی استفاده می‌کند.

* `LinkedList` از ساختار `Doubly Linked List` استفاده می‌کند که هر Node به Node قبلی و بعدی اشاره می‌کند.

تفاوت عملکرد آن‌ها:

* دسترسی با Index: در `ArrayList` برابر `O(1)` و در `LinkedList` برابر `O(n)` است.

* جستجوی Element: در هر دو معمولاً `O(n)` است.

* افزودن به انتهای لیست: در `ArrayList` به‌طور میانگین `O(1)` و در `LinkedList` برابر `O(1)` است.

* درج یا حذف در موقعیت مشخص: در `ArrayList` معمولاً به دلیل جابه‌جایی عناصر `O(n)` است؛ در `LinkedList` اگر Node موردنظر را از قبل داشته باشیم، `O(1)` خواهد بود.

نکته مهم این است که درج یا حذف در `LinkedList` فقط زمانی `O(1)` است که موقعیت یا Node موردنظر را از قبل داشته باشیم. پیدا کردن آن موقعیت همچنان می‌تواند `O(n)` هزینه داشته باشد.

برای مثال، اگر بخواهیم به عنصر شماره ۵۰۰۰ دسترسی پیدا کنیم، `ArrayList` مستقیماً از طریق Index به آن دسترسی دارد، اما `LinkedList` باید از Nodeها عبور کند.

از طرف دیگر، `ArrayList` به دلیل Locality بهتر داده‌ها در حافظه و استفاده کمتر از Objectهای جداگانه، در بسیاری از عملیات واقعی عملکرد مناسبی دارد.

نتیجه: `LinkedList` زمانی می‌تواند مناسب باشد که درج و حذف مکرر در ابتدا یا انتهای لیست داشته باشیم یا Node موردنظر از قبل در اختیارمان باشد. برای دسترسی تصادفی، پیمایش متداول و بسیاری از کاربردهای عمومی، معمولاً `ArrayList` انتخاب مناسب‌تری است.

# سؤال 33

سؤال: در مدیریت حافظه و Garbage Collection، چه ساختارهایی می‌توانند باعث Memory Leak شوند؟

راهنما: به تاثیر مراجع و مدیریت دستی منابع فکر کنید.

سطح: Senior

### پاسخ

در Java، `Garbage Collector` اشیایی را جمع‌آوری می‌کند که دیگر از طریق Referenceهای قابل‌دسترسی، به‌ویژه از `GC Roots`، قابل دسترسی نباشند.

بنابراین `Memory Leak` زمانی رخ می‌دهد که Objectهایی که دیگر به آن‌ها نیاز نداریم، همچنان Reference قابل‌دسترسی داشته باشند و در نتیجه توسط GC جمع‌آوری نشوند.

ساختارها و الگوهای رایج عبارت‌اند از:

۱. Collectionهای بدون محدودیت

Java

```
private static final List<Object> CACHE = new ArrayList<>();

public void process(Object object) {
    CACHE.add(object);
}
```

اگر Objectها دائماً اضافه شوند و هیچ‌وقت حذف نشوند، حافظه مصرفی افزایش پیدا می‌کند.

۲. Static Map و Cache نامحدود

نگه‌داشتن داده‌ها در `static Map` باعث می‌شود تا زمانی که Reference باقی است، Objectها قابل جمع‌آوری نباشند.

راهکار: استفاده از `TTL`، محدودیت اندازه و `Eviction Policy` متناسب با نیاز.

۳. Listenerهایی که حذف نمی‌شوند

اگر Listener ثبت شود ولی پس از پایان Lifecycle مربوطه از Publisher حذف نشود، ممکن است Referenceهای غیرضروری حفظ شوند.

۴. ThreadLocal

در Threadهای طولانی‌عمر، مثل Threadهای یک `Thread Pool`، استفاده نادرست از `ThreadLocal` می‌تواند باعث باقی ماندن داده‌های غیرضروری شود.

۵. نگه‌داشتن Reference در Objectهای طولانی‌عمر

نگه‌داشتن Reference به یک Graph بزرگ از Objectها، حتی زمانی که دیگر به آن‌ها نیاز نداریم، می‌تواند مصرف حافظه را افزایش دهد.

برای شناسایی این مشکلات می‌توان از `JFR`، `Heap Dump` و ابزارهایی مثل `JDK Mission Control` یا `Eclipse MAT` استفاده کرد. بررسی `GC Roots`، تعداد Objectها و `Retained Heap` به پیدا کردن Referenceهای مشکل‌ساز کمک می‌کند.

همچنین باید توجه داشت که منابعی مانند File و Database Connection فقط مسئله Memory نیستند و باید Lifecycle آن‌ها را با `try-with-resources` یا روش مناسب مدیریت کرد.

نتیجه: Memory Leak در Java معمولاً به دلیل نگه‌داشتن Referenceهای غیرضروری رخ می‌دهد؛ مانند Collectionهای نامحدود، Cacheها، Listenerها و `ThreadLocal`. راهکار، مدیریت صحیح Lifecycle و تحلیل Heap برای یافتن Referenceهای غیرضروری است.

# سؤال 34

سؤال: در معماری Microservices، برای ارتباط بین سرویس‌ها چه زمانی استفاده از Messaging مناسب‌تر از REST است؟

راهنما: به تفاوت ارتباط همزمان و غیرهمزمان توجه کنید.

سطح: Senior

### پاسخ

انتخاب بین `REST` و `Messaging` به میزان نیاز به پاسخ فوری، استقلال سرویس‌ها، حجم پیام‌ها و تحمل‌پذیری خطا بستگی دارد.

در ارتباط `REST` معمولاً یک سرویس درخواست می‌فرستد و برای دریافت پاسخ منتظر می‌ماند. در `Messaging`، سرویس تولیدکننده پیام را منتشر می‌کند و Consumer می‌تواند آن را در زمان دیگری پردازش کند.

### چه زمانی Messaging مناسب‌تر است؟

۱. پردازش غیرهمزمان (Asynchronous Processing)

مثلاً پس از ثبت سفارش، ارسال پیامک یا ایمیل می‌تواند بدون منتظر نگه‌داشتن درخواست کاربر انجام شود.

۲. کاهش وابستگی زمانی بین سرویس‌ها

اگر Consumer موقتاً در دسترس نباشد، پیام می‌تواند در Broker باقی بماند و بعداً پردازش شود؛ البته این رفتار به تنظیمات و قابلیت‌های Broker بستگی دارد.

۳. مدیریت حجم بالای پیام‌ها

با استفاده از Queue یا Partition می‌توان پردازش را بین چند Consumer تقسیم کرد.

۴. ارتباط Event-driven

مثلاً یک سرویس `OrderCreated` را منتشر می‌کند و سرویس‌های Inventory، Notification و Analytics بر اساس نیاز خود آن را مصرف می‌کنند.

### چه زمانی REST مناسب‌تر است؟

* وقتی پاسخ فوری لازم است.

* وقتی Client به نتیجه عملیات نیاز دارد.

* وقتی ارتباط ساده Request/Response کافی است.

* وقتی نمی‌خواهیم پیچیدگی Broker و پردازش غیرهمزمان را اضافه کنیم.

Messaging نیز پیچیدگی‌های خاص خود را دارد؛ از جمله `Eventual Consistency`، مدیریت پیام تکراری، ترتیب پیام‌ها و Retry.

برای مثال، اگر Consumer یک پیام را بیش از یک بار دریافت کند، باید در صورت نیاز از `Idempotency` استفاده شود تا اثر تکرار پردازش کنترل شود.

نتیجه: `REST` برای ارتباط مستقیم و Request/Response مناسب است؛ `Messaging` زمانی ترجیح دارد که پردازش غیرهمزمان، کاهش وابستگی زمانی، جذب بار و ارتباط Event-driven اهمیت داشته باشند. انتخاب باید بر اساس نیاز سیستم باشد، نه صرفاً استفاده از Microservices.

# سؤال 35

سؤال: در انتخاب ابزار Messaging (مانند Kafka یا RabbitMQ)، چه پارامترهایی را باید مدنظر قرار داد؟

راهنما: به تفاوت نیازهای پروژه و ویژگی‌های ابزارها توجه کنید.

سطح: Senior

### پاسخ

برای انتخاب ابزار Messaging باید نیازهای معماری و الگوی مصرف پیام‌ها را بررسی کنیم. `Kafka` و `RabbitMQ` هر دو ابزارهای Messaging هستند، اما مدل کاری و کاربردهای رایج آن‌ها متفاوت است.

۱. نوع Messaging

اگر هدف، انتشار و نگهداری Eventها و امکان مصرف مجدد آن‌ها توسط Consumerهای مختلف باشد، `Kafka` گزینه مناسبی است.

اگر هدف، ارسال پیام به Queue، توزیع کار بین Workerها، Routing و مدیریت پیام‌ها بر اساس الگوهای مختلف باشد، `RabbitMQ` می‌تواند انتخاب مناسبی باشد.

۲. Throughput و حجم داده

باید بررسی کنیم سیستم چه تعداد پیام در ثانیه تولید می‌کند، اندازه پیام‌ها چقدر است و چه میزان تأخیر قابل قبول است.

Kafka برای پردازش جریان داده با حجم بالا و نگهداری Eventها طراحی شده است؛ البته عملکرد واقعی به تعداد Partitionها، سخت‌افزار، تنظیمات و الگوی مصرف بستگی دارد.

۳. ترتیب پیام‌ها

در Kafka ترتیب پیام‌ها در محدوده یک `Partition` حفظ می‌شود، نه به‌صورت سراسری بین تمام Partitionها.

در RabbitMQ نیز ترتیب دریافت و پردازش می‌تواند تحت تأثیر چند Consumer، Redelivery و تنظیمات Queue قرار بگیرد.

۴. نگهداری و Replay پیام‌ها

در Kafka پیام‌ها بر اساس سیاست Retention نگهداری می‌شوند و Consumer می‌تواند Offset خود را تغییر دهد و پیام‌های قبلی را دوباره بخواند.

در RabbitMQ معمولاً پیام پس از تأیید مصرف (`Acknowledgement`) و مطابق تنظیمات Queue از صف حذف می‌شود.

۵. Reliability و Delivery Guarantees

باید مواردی مانند این‌ها بررسی شوند:

* `Acknowledgement`

* `Retry`

* `Dead Letter Queue`

* `Message Durability`

* `Idempotency`

* مدیریت پیام‌های تکراری

هیچ‌کدام از این ابزارها به‌تنهایی تضمین نمی‌کنند که منطق تجاری یک پیام دقیقاً یک بار اجرا شود؛ این موضوع به طراحی Consumer، تراکنش‌ها و نحوه مدیریت خطا نیز وابسته است.

۶. پیچیدگی عملیاتی و هزینه

مواردی مثل Deployment، Monitoring، Scaling، Backup، دانش تیم و هزینه نگهداری نیز مهم هستند.

به‌طور کلی، `Kafka` معمولاً برای Event Streaming و پردازش جریان داده مناسب است؛ در مقابل، `RabbitMQ` برای Queue-based Messaging، توزیع کار و Routing انعطاف‌پذیر کاربرد زیادی دارد.

نتیجه: انتخاب Messaging Tool باید بر اساس نوع ارتباط، Throughput، نیاز به Replay، ترتیب پیام‌ها، Delivery Guarantees، Routing، مقیاس‌پذیری و پیچیدگی عملیاتی انجام شود. `Kafka` معمولاً برای Event Streaming و `RabbitMQ` برای Queue-based Messaging و Routing مناسب است؛ اما انتخاب نهایی به نیاز واقعی سیستم بستگی دارد.



# سؤال 36

سؤال: در توسعه سرویس‌های ابری، چه چالش‌هایی در مدیریت منابع و مقیاس‌پذیری وجود دارد و چگونه باید آن‌ها را حل کرد؟

راهنما: به نیاز به مقیاس‌پذیری و کنترل هزینه توجه کنید.

سطح: Senior

### پاسخ

در توسعه سرویس‌های ابری، چالش اصلی این است که منابع محاسباتی، حافظه، شبکه و Storage را متناسب با بار سیستم مدیریت کنیم، بدون اینکه هزینه افزایش پیدا کند یا Performance کاهش یابد.

مهم‌ترین چالش‌ها عبارت‌اند از:

۱. Scalability

با افزایش تعداد درخواست‌ها، ممکن است CPU، Memory یا Database به Bottleneck تبدیل شوند.

* `Horizontal Scaling`: اضافه کردن Instanceهای جدید.

* `Vertical Scaling`: افزایش منابع یک Instance.

* استفاده از `Load Balancer` برای توزیع درخواست‌ها.

* استفاده از `Auto Scaling` بر اساس Metrics و سیاست‌های تعریف‌شده.

۲. مدیریت هزینه (Cost Optimization)

استفاده بیش‌ازحد از منابع باعث افزایش هزینه می‌شود. باید Metrics مصرف منابع بررسی شوند و اندازه Instanceها، تعداد Replicaها و مدت استفاده از منابع متناسب با نیاز واقعی تنظیم شوند.

۳. Resource Limits

محدودیت CPU، Memory و Connectionها می‌تواند باعث افزایش Latency یا حتی Crash شدن سرویس شود. باید Resource Usage، Thread Pool، Connection Pool و رفتار GC مانیتور شوند.

۴. High Availability و Fault Tolerance

استفاده از چند Instance، Health Check، Load Balancing، Backup و توزیع مناسب منابع در Failure Domainهای مستقل، احتمال قطعی سرویس را کاهش می‌دهد.

۵. Observability

با استفاده از Metrics، Logs و Distributed Tracing می‌توان Bottleneckها و مشکلات منابع را شناسایی کرد.

نتیجه: برای مدیریت منابع ابری باید از `Auto Scaling`، مانیتورینگ، محدودیت‌گذاری مناسب منابع، توزیع بار و بهینه‌سازی هزینه استفاده کرد. تصمیم‌ها باید بر اساس Metrics واقعی و الگوی مصرف سیستم گرفته شوند.

# سؤال 37

سؤال: در چه شرایطی استفاده از سرویس‌های Managed Cloud نسبت به راه‌حل‌های On-Premise ارجحیت دارد؟

راهنما: به تفاوت هزینه و سرعت راه‌اندازی توجه کنید.

سطح: Senior

### پاسخ

در `Managed Cloud Services`، بخش قابل‌توجهی از مدیریت زیرساخت و عملیات سرویس بر عهده ارائه‌دهنده Cloud است؛ اما در `On-Premise`، سازمان مسئولیت بیشتری در زمینه تهیه، نگهداری و مدیریت زیرساخت دارد.

### چه زمانی Managed Cloud مناسب‌تر است؟

۱. راه‌اندازی سریع‌تر

برای استفاده از Database، Message Broker، Cache یا Object Storage، معمولاً نیازی به راه‌اندازی و مدیریت تمام زیرساخت از ابتدا نیست.

۲. کاهش بار عملیاتی

بخشی از مسئولیت‌هایی مانند Patch Management، Backup، Monitoring زیرساخت و بعضی عملیات نگهداری می‌تواند بر عهده Provider باشد؛ البته این موارد به سرویس انتخاب‌شده بستگی دارند.

۳. مقیاس‌پذیری

افزایش ظرفیت منابع در بسیاری از سرویس‌های Managed ساده‌تر است و نیاز به خرید و نصب سخت‌افزار ندارد.

۴. هزینه اولیه کمتر

معمولاً نیازی به سرمایه‌گذاری اولیه قابل‌توجه برای خرید تجهیزات نیست، اما هزینه بلندمدت به میزان مصرف، قیمت‌گذاری و نیازهای سیستم بستگی دارد.

### چه زمانی On-Premise مناسب‌تر است؟

* زمانی که محدودیت‌های قانونی یا سازمانی درباره محل نگهداری داده‌ها وجود دارد.

* وقتی کنترل کامل‌تری بر زیرساخت و محیط اجرا لازم است.

* وقتی سازمان زیرساخت و تیم عملیاتی مناسب دارد.

* زمانی که Workload ثابت و هزینه کل مالکیت زیرساخت توجیه‌پذیر است.

* وقتی وابستگی به Provider یا محدودیت‌های شبکه و اتصال قابل قبول نیست.

نکته مهم این است که Managed Cloud به معنی حذف تمام مسئولیت‌های امنیتی نیست. معمولاً مسئولیت‌ها بین Provider و سازمان تقسیم می‌شوند و باید مدل `Shared Responsibility` بررسی شود.

نتیجه: `Managed Cloud` برای راه‌اندازی سریع، کاهش بار عملیاتی و افزایش انعطاف‌پذیری مناسب است. `On-Premise` زمانی می‌تواند ترجیح داشته باشد که کنترل زیرساخت، الزامات خاص سازمانی یا اقتصاد بلندمدت اهمیت بیشتری داشته باشند. انتخاب باید بر اساس هزینه کل، امنیت، الزامات قانونی و توان عملیاتی تیم انجام شود.

# سؤال 38

سؤال: در استفاده از Docker برای سرویس‌های جاوا، چه مزایا و چالش‌هایی وجود دارد؟

راهنما: به تاثیر ایزوله‌سازی و نیاز به مدیریت منابع توجه کنید.

سطح: Senior

### پاسخ

`Docker` امکان بسته‌بندی برنامه Java و وابستگی‌های Runtime آن را در یک `Container Image` فراهم می‌کند تا برنامه در محیط‌های مختلف با ساختار اجرایی سازگارتری اجرا شود.

### مزایا

۱. محیط اجرای یکسان

برنامه، Runtime و وابستگی‌های موردنیاز در Image تعریف می‌شوند و تفاوت محیط توسعه و Production کاهش پیدا می‌کند.

۲. ایزوله‌سازی

Containerها محیط اجرای جداگانه‌ای دارند و مدیریت سرویس‌ها و وابستگی‌های آن‌ها ساده‌تر می‌شود.

۳. استقرار و Scaling

اجرای چند Instance از سرویس و ادغام آن با CI/CD و ابزارهای Orchestration مانند Kubernetes آسان‌تر می‌شود.

۴. مدیریت وابستگی‌ها

نسخه Java و تنظیمات Runtime را می‌توان در Image مشخص کرد و از وابستگی به نصب‌های دستی روی سرور جلوگیری کرد.

### چالش‌ها

۱. مدیریت Memory و CPU

Container محدودیت منابع دارد. اگر Heap جاوا بیش‌ازحد بزرگ تنظیم شود، ممکن است مصرف حافظه کل Process از محدودیت Container عبور کند.

در Javaهای جدید، JVM تا حد زیادی از محدودیت‌های Container آگاه است؛ بااین‌حال باید Heap، Threadها، Direct Memory و سایر مصرف‌کنندگان حافظه بررسی شوند.

۲. Image Size

استفاده از Imageهای سنگین، وابستگی‌های غیرضروری و نگهداری Build Tools در Image نهایی، حجم Image را افزایش می‌دهد.

۳. امنیت

باید از Imageهای معتبر، نسخه‌های به‌روز، حداقل دسترسی و اجرای برنامه با کاربر غیر Root استفاده کرد.

۴. Logging و Monitoring

لاگ‌ها، Metrics و Health Checkها باید طوری طراحی شوند که وضعیت سرویس در محیط Container قابل بررسی باشد.

نتیجه: Docker باعث قابل‌حمل‌تر شدن برنامه، ایزوله‌سازی و ساده‌تر شدن Deployment می‌شود؛ اما مدیریت منابع، امنیت Image، Logging و تنظیمات JVM باید به‌درستی انجام شوند.

# سؤال 39

سؤال: در پروژه‌های بزرگ، چگونه می‌توان بهینه‌ترین ساختار Dockerfile را برای برنامه‌های جاوا نوشت؟

راهنما: به تاثیر حجم ایمیج و سرعت Build توجه کنید.

سطح: Senior

### پاسخ

یک Dockerfile مناسب برای برنامه Java باید Image نهایی را کوچک‌تر و امن‌تر کند، زمان Build را کاهش دهد و اجرای برنامه را در Production قابل‌اعتماد نگه دارد.

### ۱. استفاده از Multi-stage Build

در مرحله اول، برنامه Build می‌شود و در مرحله نهایی فقط Artifact و Runtime موردنیاز قرار می‌گیرند.

dockerfile

```
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/app.jar app.jar

USER 10001

ENTRYPOINT ["java", "-jar", "app.jar"]
```

در این مثال، Maven و Source Code وارد Image نهایی نمی‌شوند و فقط JAR و Java Runtime باقی می‌مانند.

### ۲. بهینه‌سازی Build Cache

فایل‌های وابستگی مانند `pom.xml` باید قبل از Source Code کپی شوند. به این ترتیب، اگر فقط کد تغییر کند و Dependencyها ثابت باشند، Docker می‌تواند از Cache لایه‌های قبلی استفاده کند.

### ۳. انتخاب Base Image مناسب

استفاده از Image متناسب با نسخه Java و نیازهای برنامه، حجم و سطح حمله را کاهش می‌دهد. باید به سازگاری Native Libraryها و نیازهای Runtime نیز توجه کرد.

### ۴. امنیت

* اجرای برنامه با `Non-root User`

* استفاده از Imageهای معتبر و به‌روز

* جلوگیری از قرار دادن Password و Secret در Image

* بررسی آسیب‌پذیری Dependencyها و Image

### ۵. تنظیم JVM و منابع

تنظیم Heap و سایر پارامترهای JVM باید با توجه به محدودیت Memory و CPU انجام شود. تنظیمات ثابت و بیش‌ازحد بزرگ می‌توانند باعث مصرف غیرضروری منابع یا Crash شوند.

### ۶. بهبود فرآیند CI/CD

Build، تست، اسکن امنیتی و انتشار Image باید به‌صورت خودکار انجام شوند. همچنین بهتر است Imageها با Tag مشخص یا Digest قابل‌شناسایی باشند.

نتیجه: برای بهینه‌سازی Dockerfile از `Multi-stage Build`، ترتیب مناسب دستورات برای استفاده از Cache، Base Image سبک و امن، اجرای Non-root و مدیریت صحیح منابع JVM استفاده می‌کنیم. هدف فقط کاهش حجم Image نیست؛ امنیت، سرعت Build و قابلیت نگهداری نیز اهمیت دارند.

# سؤال 40

سؤال: در پیاده‌سازی امنیت سرویس‌های جاوا، چه تفاوت‌هایی بین Authorization و Authentication وجود دارد؟

راهنما: به تفاوت فرآیند احراز هویت و مجوز دسترسی دقت کنید.

سطح: Senior

### پاسخ

`Authentication` و `Authorization` دو مفهوم متفاوت در امنیت هستند.

Authentication یعنی احراز هویت؛ یعنی سیستم بررسی کند کاربر یا Client واقعاً چه هویتی دارد.

مثلاً کاربر با Username و Password وارد سیستم می‌شود یا با استفاده از Token معتبر هویت خود را اثبات می‌کند.

Authorization یعنی بررسی مجوز دسترسی؛ یعنی پس از مشخص شدن هویت، سیستم بررسی کند کاربر اجازه انجام عملیات موردنظر را دارد یا خیر.

برای مثال، در یک سامانه بانکی:

* Authentication بررسی می‌کند درخواست متعلق به کدام کاربر است.

* Authorization بررسی می‌کند آیا آن کاربر اجازه مشاهده حساب یا انجام تراکنش موردنظر را دارد.

### مثال در Spring Security

برای محدود کردن دسترسی بر اساس Role:

Java

```
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long userId) {
    // delete user
}
```

در این مثال، فرض بر این است که Authentication قبلاً انجام شده و Principal معتبر در اختیار Spring Security قرار دارد. سپس Authorization بررسی می‌کند که کاربر Role موردنیاز را داشته باشد.

### تفاوت‌های اصلی

* Authentication: هویت کاربر را مشخص می‌کند.

* Authorization: مجوز دسترسی به Resource یا عملیات را بررسی می‌کند.

* Authentication معمولاً قبل از Authorization انجام می‌شود.

* Authorization می‌تواند بر اساس Role، Permission، مالکیت Resource یا Policyهای پیچیده‌تر انجام شود.

برای مثال، داشتن Role برابر `USER` لزوماً به این معنی نیست که کاربر اجازه مشاهده اطلاعات تمام کاربران را دارد. ممکن است فقط مجاز باشد اطلاعات حساب خودش را مشاهده کند.

در REST API معمولاً پاسخ‌های زیر را می‌بینیم:

* `401 Unauthorized`: احراز هویت انجام نشده یا اطلاعات هویتی معتبر نیست.

* `403 Forbidden`: هویت مشخص است، اما مجوز کافی برای عملیات وجود ندارد.

نتیجه: `Authentication` پاسخ می‌دهد «چه کسی هستی؟» و `Authorization` پاسخ می‌دهد «اجازه انجام چه کاری را داری؟». یک سیستم امن باید هر دو را پیاده‌سازی کند و صرفاً معتبر بودن هویت را به معنی مجاز بودن تمام عملیات در نظر نگیرد.


# سؤال 41

سؤال: در پروژه‌های Enterprise، چه راهکارهایی برای افزایش امنیت Authentication پیشنهاد می‌شود؟

راهنما: به اهمیت جلوگیری از نفوذ و سرقت هویت توجه کنید.

سطح: Senior

### پاسخ

در پروژه‌های Enterprise، هدف از امن‌سازی `Authentication` این است که از دسترسی غیرمجاز، سرقت هویت و حملاتی مانند Brute Force و Credential Stuffing جلوگیری کنیم.

راهکارهای مهم عبارت‌اند از:

۱. استفاده از Password Hashing امن

Passwordها نباید به‌صورت Plain Text یا با الگوریتم‌های سریع مانند MD5 و SHA-1 ذخیره شوند. باید از الگوریتم‌هایی مانند `Argon2id`، `bcrypt` یا `scrypt` با تنظیمات مناسب استفاده کرد.

۲. Multi-Factor Authentication (MFA)

با استفاده از عامل دوم، مانند Authenticator App یا Security Key، حتی در صورت افشای Password نیز احتمال دسترسی غیرمجاز کاهش می‌یابد.

۳. محافظت در برابر Brute Force

با استفاده از Rate Limiting، محدودیت تلاش‌های ناموفق، تشخیص الگوهای مشکوک و در صورت نیاز تأخیر تدریجی، می‌توان حملات حدس Password را کنترل کرد. قفل‌کردن حساب نیز باید با دقت طراحی شود تا امکان سوءاستفاده برای Denial of Service ایجاد نشود.

۴. استفاده از HTTPS

اطلاعات Authentication باید از طریق ارتباط رمزنگاری‌شده منتقل شوند تا احتمال شنود و سرقت Credentialها کاهش یابد.

۵. مدیریت امن Session و Token

* استفاده از Tokenهای کوتاه‌عمر

* مدیریت صحیح Refresh Token و امکان لغو آن

* جلوگیری از افشای Token در Logها و URLها

* تنظیم امن Cookieها با `HttpOnly`، `Secure` و `SameSite` در صورت استفاده از Cookie

* اعتبارسنجی Signature، Expiration و سایر Claimهای لازم در Token

۶. استفاده از استانداردهای معتبر

برای سیستم‌های Enterprise می‌توان از `OAuth 2.0` و `OpenID Connect` استفاده کرد. این استانداردها به‌ترتیب برای Authorization و لایه Authentication مبتنی بر OAuth کاربرد دارند.

۷. Logging و Monitoring

ثبت تلاش‌های ناموفق، تشخیص رفتارهای مشکوک و ارسال Alert به تیم امنیت، به شناسایی حملات کمک می‌کند. البته Password و Token نباید در Log ثبت شوند.

نتیجه: برای افزایش امنیت Authentication باید Passwordها را امن ذخیره کنیم، از MFA و HTTPS استفاده کنیم، Brute Force را کنترل کنیم، Session و Token را صحیح مدیریت کنیم و با Logging و Monitoring رفتارهای مشکوک را شناسایی کنیم.

# سؤال 42

سؤال: در انتخاب نوع پایگاه داده برای یک سرویس جاوا، چه عواملی را باید لحاظ کرد؟

راهنما: به تفاوت‌های NoSQL و SQL و نیازمندی‌های پروژه توجه کنید.

سطح: Senior

### پاسخ

انتخاب Database باید بر اساس نیازهای داده‌ای و عملیاتی سیستم انجام شود، نه صرفاً محبوبیت یک تکنولوژی یا حجم داده.

مهم‌ترین عوامل عبارت‌اند از:

۱. ساختار داده‌ها

اگر داده‌ها ساختار مشخص و ارتباطات پیچیده دارند، `Relational Database` مانند PostgreSQL یا Oracle معمولاً انتخاب مناسبی است.

اگر داده‌ها ساختار انعطاف‌پذیرتری دارند یا مدل دسترسی با Document، Key-Value یا Graph سازگارتر است، ممکن است یکی از انواع `NoSQL` مناسب‌تر باشد.

۲. Transaction و Consistency

اگر عملیات به Transactionهای چندمرحله‌ای و تضمین‌های قوی سازگاری نیاز دارند، باید قابلیت‌های Transaction و Isolation سطح Database بررسی شوند.

بسیاری از SQL Databaseها از ACID Transactions پشتیبانی می‌کنند؛ بااین‌حال قابلیت‌های Transaction در NoSQL نیز به محصول و نوع عملیات بستگی دارد.

۳. الگوی دسترسی به داده

باید مشخص شود که سیستم بیشتر عملیات `Read` انجام می‌دهد یا `Write`، آیا Queryهای پیچیده دارد، آیا نیاز به جستجوی متنی وجود دارد و داده‌ها چگونه فیلتر و مرتب می‌شوند.

۴. Scalability و حجم داده

باید نیاز به Vertical Scaling، Replication، Partitioning و در صورت لزوم Sharding بررسی شود.

۵. Performance و Latency

نوع Indexها، تعداد Queryها، Connection Pool، حجم Transactionها و Latency قابل قبول روی انتخاب اثر می‌گذارند.

۶. عملیات و نگهداری

مواردی مانند Backup، Recovery، Monitoring، هزینه، تخصص تیم، ابزارهای مدیریتی و سازگاری با زیرساخت موجود نیز اهمیت دارند.

برای مثال، در یک سامانه بانکی که تراکنش‌های مالی و ارتباط بین موجودیت‌ها اهمیت زیادی دارند، یک Relational Database می‌تواند انتخاب مناسبی باشد. در مقابل، برای ذخیره‌سازی Session یا داده‌های Cache، یک Key-Value Store ممکن است مناسب‌تر باشد.

نتیجه: انتخاب Database به ساختار داده، Transaction، Consistency، الگوی دسترسی، Performance، Scalability، هزینه و توان عملیاتی تیم بستگی دارد. SQL یا NoSQL به‌تنهایی معیار کافی برای انتخاب نیستند.

# سؤال 43

سؤال: در چه شرایطی استفاده از Sharding در پایگاه داده توصیه می‌شود؟

راهنما: به چالش‌های مقیاس‌پذیری پایگاه داده فکر کنید.

سطح: Senior

### پاسخ

`Sharding` به معنی تقسیم داده‌های یک Database بین چند بخش مستقل یا Shard است؛ به‌طوری‌که هر Shard بخشی از داده‌ها را نگهداری کند.

این روش زمانی مطرح می‌شود که یک Database به‌تنهایی دیگر نتواند نیازهای سیستم را از نظر ظرفیت ذخیره‌سازی، Throughput یا منابع پردازشی برآورده کند.

### چه زمانی Sharding مناسب است؟

۱. افزایش حجم داده‌ها

اگر حجم داده‌ها از ظرفیت عملی یک Database بیشتر شود، می‌توان داده‌ها را بین چند Shard تقسیم کرد.

۲. افزایش Throughput

اگر یک Database به Bottleneck تبدیل شده باشد، تقسیم بار خواندن و نوشتن بین چند Shard می‌تواند ظرفیت کلی را افزایش دهد.

۳. نیاز به Horizontal Scaling

Sharding امکان توزیع داده‌ها و بار پردازشی بین چند Node را فراهم می‌کند.

### انتخاب Shard Key

یکی از مهم‌ترین تصمیم‌ها، انتخاب `Shard Key` مناسب است.

برای مثال، اگر داده‌های کاربران بر اساس `userId` تقسیم شوند، درخواست‌های مربوط به هر کاربر می‌توانند به Shard مشخصی هدایت شوند.

Shard Key نامناسب ممکن است باعث ایجاد `Hotspot` شود؛ یعنی یک Shard بار بسیار بیشتری از سایر Shardها دریافت کند.

### چالش‌های Sharding

* پیچیده‌تر شدن Queryهای بین چند Shard

* دشوارتر شدن بعضی Transactionهای چندبخشی

* Rebalancing داده‌ها هنگام تغییر ظرفیت

* مدیریت Indexها و Schema

* افزایش پیچیدگی Backup، Monitoring و Recovery

نکته مهم این است که Sharding معمولاً اولین راهکار برای Performance نیست. ابتدا باید Queryها، Indexها، Connection Pool، Cache و منابع Database بررسی شوند. اگر این روش‌ها کافی نباشند، Sharding می‌تواند مطرح شود.

نتیجه: Sharding زمانی مناسب است که یک Database به محدودیت واقعی ظرفیت یا Throughput رسیده باشد و روش‌های ساده‌تر مقیاس‌پذیری کافی نباشند. انتخاب Shard Key، جلوگیری از Hotspot و مدیریت Queryها و Transactionهای توزیع‌شده از مهم‌ترین ملاحظات طراحی هستند.

# سؤال 44

سؤال: در ORMهای جاوا مانند Hibernate، چه مشکلاتی ممکن است در Lazy Loading رخ دهد و چگونه باید آن‌ها را مدیریت کرد؟

راهنما: به تاثیر Session و نحوه بارگذاری داده‌ها توجه کنید.

سطح: Senior

### پاسخ

در Hibernate، `Lazy Loading` به این معنی است که ارتباطات یا داده‌های مرتبط فقط زمانی بارگذاری می‌شوند که واقعاً به آن‌ها دسترسی پیدا کنیم، نه لزوماً هنگام بارگذاری Entity اصلی.

این روش می‌تواند از بارگذاری غیرضروری داده‌ها جلوگیری کند، اما اگر به‌درستی مدیریت نشود، مشکلاتی ایجاد می‌کند.

### ۱. LazyInitializationException

اگر به یک Association با تنظیم `LAZY` خارج از Hibernate Session یا Persistence Context فعال دسترسی پیدا کنیم، ممکن است با `LazyInitializationException` مواجه شویم.

مثلاً:

Java

```
Order order = orderRepository.findById(id)
        .orElseThrow();

// اگر Persistence Context بسته شده باشد
order.getItems().size();
```

اگر `items` هنوز بارگذاری نشده باشد و Session دیگر در دسترس نباشد، دسترسی به آن می‌تواند Exception ایجاد کند.

راهکار: داده‌های موردنیاز را در محدوده Transaction و از طریق Query مناسب بارگذاری کنیم، نه اینکه صرفاً برای جلوگیری از Exception، همه ارتباطات را Eager کنیم.

### ۲. مشکل N+1 Query

فرض کنیم لیستی از ۱۰۰ سفارش بارگذاری کنیم و برای هر سفارش به Customer آن دسترسی پیدا کنیم.

ممکن است یک Query برای سفارش‌ها و Queryهای جداگانه برای Customerها اجرا شود؛ در نتیجه تعداد Queryها به‌طور قابل‌توجهی افزایش پیدا کند.

راهکارهای متداول:

* استفاده از `JOIN FETCH`

* استفاده از `EntityGraph`

* نوشتن Projection یا DTO Query

* بررسی SQLهای تولیدشده و اندازه‌گیری Performance

### ۳. بارگذاری بیش‌ازحد داده‌ها

استفاده از `EAGER` برای تمام ارتباطات می‌تواند باعث افزایش حجم داده، پیچیدگی Query و مصرف حافظه شود.

### ۴. Serialization و API

اگر Entityها مستقیماً به JSON تبدیل شوند، Serialization ممکن است باعث بارگذاری ناخواسته Associationها شود یا در ارتباطات دوطرفه به چرخه ارجاع منجر شود.

راهکار مناسب معمولاً استفاده از DTO و کنترل دقیق داده‌های خروجی API است.

نکته مهم این است که فعال بودن `Open Session in View` ممکن است بعضی خطاهای Lazy Loading را پنهان کند، اما لزوماً مشکل N+1 یا اجرای Queryهای ناخواسته را حل نمی‌کند.

نتیجه: مهم‌ترین مشکلات Lazy Loading شامل `LazyInitializationException`، `N+1 Query`، بارگذاری بیش‌ازحد داده و Queryهای ناخواسته هنگام Serialization است. راهکار، مدیریت درست Transaction و Persistence Context، استفاده از Fetch Plan مناسب و بررسی Queryهای واقعی است.

# سؤال 45

سؤال: در چه شرایطی استفاده از Queryهای Native نسبت به HQL در ORMها ارجحیت دارد؟

راهنما: به محدودیت‌های HQL و نیاز به بهینه‌سازی توجه کنید.

سطح: Senior

### پاسخ

در Hibernate، `HQL` یا `JPQL` برای Query کردن Entityها و ویژگی‌های مدل شی‌گرا استفاده می‌شود؛ درحالی‌که `Native SQL` مستقیماً از SQL مربوط به Database استفاده می‌کند.

انتخاب بین این دو به پیچیدگی Query، قابلیت‌های موردنیاز Database و میزان وابستگی قابل‌قبول به Vendor بستگی دارد.

### چه زمانی Native SQL مناسب‌تر است؟

۱. استفاده از قابلیت‌های خاص Database

اگر به قابلیت‌هایی نیاز داشته باشیم که HQL یا JPQL به‌صورت مستقیم پوشش نمی‌دهند، مانند بعضی قابلیت‌های اختصاصی Database، ممکن است Native SQL انتخاب مناسب‌تری باشد.

۲. Queryهای پیچیده

در Queryهای پیچیده شامل CTE، Window Functionها، گزارش‌گیری یا عملیات تحلیلی خاص، SQL مستقیم ممکن است خواناتر و قابل‌کنترل‌تر باشد؛ البته قابلیت‌های HQL نیز به نسخه Hibernate و امکانات آن بستگی دارند.

۳. Performance Optimization

اگر Profiling نشان دهد Query تولیدشده توسط ORM مناسب نیست، می‌توان Native SQL را برای کنترل بیشتر روی Joinها، Hintها و ساختار Query بررسی کرد.

البته Native SQL الزاماً سریع‌تر از HQL نیست؛ در بسیاری از موارد Hibernate می‌تواند Query مناسبی تولید کند. تصمیم باید بر اساس Execution Plan و Benchmark واقعی باشد.

### مزایای HQL

* کار با Entityها و Propertyهای آن‌ها

* استقلال نسبی از نوع Database

* سازگاری بهتر با مدل شی‌گرا

* کاهش وابستگی مستقیم به Schema و SQL اختصاصی

### معایب Native SQL

* افزایش وابستگی به نوع Database

* احتمال دشوارتر شدن نگهداری هنگام تغییر Schema

* نیاز به مدیریت دقیق‌تر Mapping نتیجه Query

* کاهش Portability در صورت استفاده از قابلیت‌های اختصاصی Database

در هر دو روش باید از Parameter Binding استفاده کنیم و از اتصال مستقیم ورودی کاربر به Query با String Concatenation پرهیز کنیم تا خطر SQL Injection کاهش یابد.

نتیجه: `Native SQL` زمانی مناسب‌تر است که به قابلیت‌های اختصاصی Database، Queryهای پیچیده یا کنترل دقیق‌تری روی SQL نیاز داشته باشیم. در غیر این صورت، `HQL/JPQL` معمولاً به دلیل خوانایی، کار با Entityها و وابستگی کمتر به Database انتخاب مناسبی است. تصمیم نهایی باید بر اساس نیاز واقعی و اندازه‌گیری Performance باشد.
# سؤال 46

**سؤال:** در رعایت اصول Clean Code، چه تکنیک‌هایی برای بهبود خوانایی کد در پروژه‌های جاوا پیشنهاد می‌کنید؟

**راهنما:** به تاثیر خوانایی بر نگهداری و توسعه توجه کنید.

**سطح:** Senior

### پاسخ

برای بهبود خوانایی کد در Java، هدف اصلی این است که کد تا حد ممکن **واضح، قابل فهم و قابل تغییر** باشد و نیاز به توضیحات اضافی کاهش پیدا کند.

مهم‌ترین تکنیک‌ها:

* استفاده از نام‌های معنادار برای `Class`، `Method`، `Variable` و `Exception`.
* هر `Method` یک مسئولیت مشخص داشته باشد و بیش از حد بزرگ و پیچیده نباشد.
* رعایت `Single Responsibility` و سایر اصول `SOLID`.
* کاهش `if/else`های تو در تو و `Nested Logic` با استفاده از `Early Return` و شکستن منطق پیچیده به متدهای کوچک‌تر.
* جلوگیری از `Magic Number` و `Magic String` و استفاده از `Constants` یا `Enum`.
* حذف کدهای تکراری با رعایت `DRY`، البته بدون ایجاد abstractionهای غیرضروری.
* استفاده از `Exception`های مشخص و پرهیز از `catch`های عمومی مثل `catch (Exception e)` بدون دلیل.
* رعایت `Formatting` و یک استاندارد مشخص برای نام‌گذاری و ساختار کد.
* استفاده مناسب از `Comments`؛ کامنت باید بیشتر دلیل یک تصمیم غیر بدیهی را توضیح دهد، نه اینکه صرفاً کد را تکرار کند.
* نوشتن `Unit Test`های خوانا که رفتار مورد انتظار سیستم را مشخص کنند.

در پروژه‌های Enterprise، خوانایی مستقیماً روی **Maintenance، Code Review، Debugging و Onboarding اعضای جدید** تأثیر دارد.

**نتیجه:**
Clean Code یعنی کدی که با نام‌گذاری مناسب، مسئولیت‌های مشخص، پیچیدگی کم و ساختار منظم، برای توسعه‌دهنده دیگری هم به‌راحتی قابل فهم و تغییر باشد.

---

# سؤال 47

**سؤال:** در چه شرایطی Refactoring کد ضروری است و چه مزایایی دارد؟

**راهنما:** به تاثیر Refactoring بر کیفیت و توسعه‌پذیری فکر کنید.

**سطح:** Senior

### پاسخ

`Refactoring` یعنی تغییر ساختار داخلی کد بدون تغییر در رفتار قابل مشاهده آن.

Refactoring زمانی اهمیت بیشتری پیدا می‌کند که کد دارای `Code Smell`هایی مانند:

* متدها و کلاس‌های بیش از حد بزرگ
* `Duplicate Code`
* وابستگی شدید بین بخش‌ها
* `Long Parameter List`
* `Nested Conditional`های پیچیده
* مسئولیت‌های متعدد در یک کلاس
* تغییر یک قابلیت که نیازمند اصلاح چندین بخش نامرتبط است
* سخت بودن نوشتن یا نگهداری `Test`

باشد.

بهتر است Refactoring به‌صورت **تدریجی و همراه با Test** انجام شود، نه اینکه بدون کنترل بخش بزرگی از سیستم بازنویسی شود.

مهم‌ترین مزایا:

* افزایش `Maintainability`
* کاهش `Technical Debt`
* کاهش پیچیدگی و احتمال خطا
* افزایش `Testability`
* ساده‌تر شدن اضافه کردن قابلیت‌های جدید
* کاهش هزینه تغییرات در آینده

نکته مهم این است که Refactoring نباید صرفاً برای زیباتر شدن کد انجام شود؛ باید **ارزش فنی مشخصی** ایجاد کند و با نیاز پروژه متناسب باشد.

**نتیجه:**
Refactoring زمانی ضروری است که ساختار فعلی کد، تغییر، تست و نگهداری را دشوار کرده باشد. هدف آن تغییر ساختار بدون تغییر رفتار و در نتیجه بهبود کیفیت، Maintainability و توسعه‌پذیری است.

---

# سؤال 48

**سؤال:** در پیاده‌سازی CI/CD برای پروژه‌های جاوا، چه ملاحظاتی برای اطمینان از کیفیت و امنیت باید رعایت شود؟

**راهنما:** به تاثیر تست و امنیت در چرخه استقرار توجه کنید.

**سطح:** Senior

### پاسخ

در `CI/CD` باید Pipeline طوری طراحی شود که قبل از Deployment، کیفیت و امنیت نرم‌افزار به‌صورت خودکار بررسی شود.

در بخش `CI` معمولاً این مراحل مهم هستند:

1. **Build و Dependency Resolution**
2. اجرای `Unit Test` و `Integration Test`
3. بررسی `Code Quality` و `Static Analysis`
4. بررسی `Code Coverage` در صورت داشتن threshold مناسب
5. `Dependency / Vulnerability Scanning`
6. بررسی Secretها و جلوگیری از قرار گرفتن credential در repository
7. Build کردن Artifact یا Docker Image

در بخش `CD` نیز باید مواردی مانند این‌ها در نظر گرفته شوند:

* استفاده از `Environment`های جداگانه مانند Test، Staging و Production
* مدیریت Secretها خارج از source code
* استفاده از `Artifact` مشخص و immutable برای Deployment
* `Approval Gate` برای محیط‌های حساس در صورت نیاز
* `Rollback` یا `Roll-forward` strategy
* Health Check بعد از Deployment
* `Observability` و بررسی Metrics/Logs بعد از Release
* استفاده از `Least Privilege` برای دسترسی‌های Pipeline و Deployment

همچنین Pipeline نباید فقط روی Application Code تمرکز کند؛ امنیت `Dependencies`، Container Image، CI Runner و Credentials نیز باید بررسی شود.

**نتیجه:**
یک CI/CD مناسب باید قبل از Deployment، Build، Test، Static Analysis و Security Scanning را خودکار کند و در CD نیز Secret Management، کنترل دسترسی، Health Check و Rollback را در نظر بگیرد.

---

# سؤال 49

**سؤال:** در انتخاب استراتژی Caching برای سرویس‌های جاوا، چه عواملی باید بررسی شود؟

**راهنما:** به تاثیر Cache بر کارایی و هماهنگی داده‌ها توجه کنید.

**سطح:** Senior

### پاسخ

در انتخاب `Caching Strategy` فقط افزایش سرعت مهم نیست؛ باید بین **Performance، Consistency، Memory و Complexity** تعادل برقرار شود.

مهم‌ترین عوامل عبارت‌اند از:

* **Access Pattern:** داده چقدر خوانده و چقدر تغییر داده می‌شود؟
* **Data Volatility:** داده چقدر تغییر می‌کند؟
* **Consistency Requirement:** آیا `Stale Data` قابل قبول است؟
* **Cache Size و Memory:** چه مقدار داده می‌توان نگه داشت؟
* **TTL:** داده چه مدت می‌تواند معتبر باشد؟
* **Eviction Policy:** مانند `LRU` در چه شرایطی باید داده حذف شود؟
* **Cache Hit Ratio:** چند درصد درخواست‌ها از Cache پاسخ می‌گیرند؟
* **Distributed بودن سرویس:** در چند instance اجرا می‌شود؟

برای یک سرویس ساده و Single Instance ممکن است `In-Memory Cache` کافی باشد. اما در یک سیستم Distributed معمولاً Cache مشترکی مانند `Redis` مناسب‌تر است.

همچنین باید مشخص شود از چه الگوی caching استفاده می‌شود؛ مثلاً:

* `Cache-Aside`
* `Read-Through`
* `Write-Through`
* `Write-Behind`

یکی از چالش‌های مهم نیز **Cache Invalidation** است؛ یعنی وقتی داده اصلی تغییر می‌کند، Cache چگونه به‌روز یا invalidate شود.

در Spring می‌توان از `Spring Cache Abstraction` و Providerهایی مانند Caffeine یا Redis استفاده کرد.

**نتیجه:**
برای انتخاب Cache باید Access Pattern، میزان تغییر داده، نیاز Consistency، TTL، Eviction، حجم Cache، Distributed بودن سیستم و هزینه Invalidation بررسی شود. Cache نامناسب می‌تواند علاوه بر کاهش Consistency، پیچیدگی سیستم را افزایش دهد.

---

# سؤال 50

**سؤال:** در انتخاب ابزار Build برای پروژه‌های جاوا (مانند Maven یا Gradle)، چه معیارهایی باید مدنظر قرار گیرد؟

**راهنما:** به تاثیر ابزار Build بر فرآیند توسعه و استقرار توجه کنید.

**سطح:** Senior

### پاسخ

در انتخاب `Build Tool` نباید فقط به محبوبیت ابزار توجه کرد؛ باید نیاز پروژه و تیم را در نظر گرفت.

معیارهای مهم عبارت‌اند از:

* **Dependency Management:** مدیریت dependencyها، نسخه‌ها و transitive dependencies.
* **Build Performance:** سرعت Build و قابلیت‌هایی مانند incremental build و caching.
* **پشتیبانی از CI/CD:** اجرای پایدار و قابل تکرار در Pipeline.
* **Plugin Ecosystem:** وجود Pluginهای مورد نیاز پروژه.
* **Multi-Module Support:** اهمیت زیادی در پروژه‌های Enterprise دارد.
* **Reproducible Builds:** اطمینان از اینکه Build در محیط‌های مختلف نتیجه قابل پیش‌بینی داشته باشد.
* **Maintainability:** خوانایی و سادگی فایل‌های Build.
* **Team Expertise:** میزان آشنایی تیم با ابزار.
* **Integration:** سازگاری با IDE، Docker، Testing و ابزارهای Quality/Security.
* **Build Configuration Complexity:** اینکه پروژه چقدر نیاز به Build Logic سفارشی دارد.

`Maven` به دلیل ساختار convention-based و استاندارد بودن `pom.xml` در بسیاری از پروژه‌های Enterprise انتخاب رایجی است.

`Gradle` انعطاف‌پذیری و قابلیت شخصی‌سازی بیشتری دارد و در پروژه‌های پیچیده می‌تواند Build Logic قدرتمندتری ارائه دهد.

بنابراین نمی‌توان گفت یکی همیشه بهتر از دیگری است؛ انتخاب باید بر اساس **نیاز پروژه، پیچیدگی Build، سرعت، اکوسیستم و توانایی تیم** انجام شود.

**نتیجه:**
در انتخاب Maven یا Gradle باید Dependency Management، Performance، CI/CD، Pluginها، Multi-Module بودن، Maintainability، Reproducibility و تخصص تیم را بررسی کرد و ابزار را بر اساس نیاز واقعی پروژه انتخاب کرد.
