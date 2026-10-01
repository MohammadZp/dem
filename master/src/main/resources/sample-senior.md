
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


