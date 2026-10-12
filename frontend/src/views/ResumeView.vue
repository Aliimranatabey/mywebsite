<template>
  <v-container class="py-12">
    <v-row justify="center">
      <v-col cols="12" md="10" lg="8">
        <v-card class="pa-8" elevation="2">
          <div class="d-flex align-center mb-8">
            <v-avatar color="primary" size="100" class="mr-6">
              <span class="text-h3 text-white">Aİ</span>
            </v-avatar>
            <div>
              <h1 class="text-h3 font-weight-bold mb-2">Ali İmran Atabey</h1>
              <p class="text-h6 text-grey-darken-1">Yazılım Geliştirici</p>
            </div>
          </div>

          <v-divider class="mb-8"></v-divider>

          <section class="mb-8">
            <h2 class="text-h4 mb-4 d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-account</v-icon>
              Hakkımda
            </h2>
            <p class="text-body-1">
              Merhaba! Ben Ali İmran. Yazılım geliştirme konusunda tutkulu, yeni teknolojileri öğrenmeye hevesli biriyim.
              Yazılım geliştirme alanında kendini sürekli geliştirmeyi hedefleyen bir mühendis olarak; Java,
              Spring Boot ve Vue.js teknolojileriyle çeşitli projelerde aktif görev aldım. Büyük veri analizi
              konusunda deneyimliyim ve bu alanda Tableau platformunu etkin şekilde kullanmaktayım.
              Ayrıca Python diliyle süreçleri otomatikleştiren çözümler geliştirdim. Hem arka uç (backend)
              hem de ön uç (frontend) teknolojilerinde ölçeklenebilir, sürdürülebilir ve verimli uygulamalar
              geliştirmeye odaklanıyor; modern yazılım geliştirme yaklaşımlarını yetkinliğime entegre ederek
sürekli kendimi ileri taşımayı hedefliyorum.
            </p>
          </section>

          <section class="mb-8">
            <h2 class="text-h4 mb-4 d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-briefcase</v-icon>
              Deneyim
            </h2>
            <v-timeline density="compact" align="start">
              <v-timeline-item dot-color="primary" size="small">
                <div class="mb-4">
                  <div class="font-weight-bold">Retfox / Turk Telekom</div>
                  <div class="text-caption">Yazılım ve Veri Uzmanı | 2023 Ağustos - Devam Ediyor</div>
                  <div><b>◦ Otomasyon ve Veri Analizi : </b> Türk Telekom bünyesinde fiziksel süreçleri otomatikleştirmeye yönelik çeşitli Python tabanlı projeler geliştirdim. Ping otomasyonu, hız testi otomasyonu, torrent izleme ve seri bağlantı kontrol sistemleri gibi uygulamalar hayata geçirildi. Ayrıca, büyük veri setleri üzerinde kapsamlı analizler yaparak anlamlı içgörüler elde edilmesi sağlandı.</div>
                  <div><b>◦ Büyük Veri Yönetimi ve Tableau Raporlaması : </b> Her 5 dakikada bir 10 milyon satırlık veri toplayan bir sistemde görev aldım. Bu veriler işlenerek Oracle veritabanına aktarıldı ve Tableau ile görselleştirildi. Büyük veri kümelerinin analizinde, yorumlanmasında ve yöneticilere sunulacak şekilde raporlanmasında aktif rol aldım. Edindiğim uzmanlık doğrultusunda geniş ekipler için Tableau eğitimleri verdim ve Tableau, veri yapıları ve algoritma analizleri gibi konularda kapsamlı dokümantasyonlar hazırladım.</div>
                </div>
              </v-timeline-item>
              <v-timeline-item dot-color="primary" size="small">
                <div class="mb-4">
                  <div class="font-weight-bold">Smartiys</div>
                  <div class="text-caption">Stajyer Öğrenci | Haziran 2021 - Eylül 2021 & Mart 2023 Haziran 2023</div>
                  <div><b>◦ Smart ERP : </b> 200 günlük staj sürecim boyunca, Spring Boot kullanarak yeni RESTful API’ler geliştirdim. Arka uçta Spring Framework ile çalışırken, öncelikle HTML5, CSS ve JavaScript teknolojilerini kullandım; sonrasında ise web servis projelerinde Vue.js’e geçiş yaptım. Veritabanı olarak PostgreSQL tercih edildi. Ayrıca, fabrika otomasyonlarına yönelik geliştirilen ERP yazılım çözümlerinin geliştirme ve destek süreçlerine katkı sağladım.</div>
                  <div><b>◦ Agile (Çevik) Geliştirme Süreci : </b> Jira üzerinden kullanıcı hikâyelerini yöneterek, çevik yöntemle çalışan çok disiplinli bir ekiple sprint bazlı çalışma deneyimi kazandım. Proje geliştirme sürecinde takım içi koordinasyon, günlük stand-up ve sprint planlama toplantılarına aktif olarak katıldım.</div>
                </div>
              </v-timeline-item>
            </v-timeline>
          </section>

          <section>
            <h2 class="text-h4 mb-4 d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-school</v-icon>
              Eğitim
            </h2>
            <v-list>
              <v-list-item>
                <v-list-item-title class="font-weight-bold">Yazılım Mühendisliği</v-list-item-title>
                <v-list-item-subtitle>Fırat Üniversitesi - 2019/2023</v-list-item-subtitle>
              </v-list-item>
            </v-list>
          </section>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

