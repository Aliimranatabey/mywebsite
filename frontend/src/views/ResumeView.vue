<template>
  <div class="container narrow page">
    <!-- Kimlik -->
    <header class="intro">
      <AuthorAvatar name="Ali İmran Atabey" size="4.5rem" />
      <div>
        <h1 class="h1">Ali İmran Atabey</h1>
        <p class="role">Yazılım Geliştirici</p>
      </div>
    </header>

    <p class="lead about">
      Merhaba! Ben Ali İmran. Yazılım geliştirme konusunda tutkulu, yeni teknolojileri öğrenmeye
      hevesli biriyim. Java, Spring Boot ve Vue.js ile çeşitli projelerde aktif görev aldım. Büyük
      veri analizinde deneyimliyim ve Tableau'yu etkin şekilde kullanıyorum. Python ile süreçleri
      otomatikleştiren çözümler geliştirdim. Hem arka uçta hem ön uçta ölçeklenebilir, sürdürülebilir
      ve verimli uygulamalar geliştirmeye odaklanıyorum.
    </p>

    <!-- Deneyim -->
    <section id="deneyim" class="section">
      <h2 class="h2 section-title">Deneyim</h2>

      <ol class="timeline">
        <li v-for="job in experience" :key="job.company" class="tl-item">
          <span class="tl-dot" aria-hidden="true"></span>
          <div class="surface tl-card">
            <div class="tl-head">
              <div>
                <h3 class="tl-title">{{ job.company }}</h3>
                <p class="tl-role">{{ job.role }}</p>
              </div>
              <span class="badge">{{ job.period }}</span>
            </div>

            <div v-for="item in job.items" :key="item.heading" class="tl-entry">
              <h4 class="tl-entry-title">{{ item.heading }}</h4>
              <p class="tl-entry-text">{{ item.text }}</p>
            </div>
          </div>
        </li>
      </ol>
    </section>

    <!-- Eğitim -->
    <section id="egitim" class="section">
      <h2 class="h2 section-title">Eğitim</h2>
      <div class="surface edu">
        <span class="mdi mdi-school-outline edu-icon" aria-hidden="true"></span>
        <div>
          <h3 class="tl-title">Fırat Üniversitesi</h3>
          <p class="tl-role">Yazılım Mühendisliği · 2019 — 2023</p>
        </div>
      </div>
    </section>

    <!-- Teknolojiler -->
    <section id="teknolojiler" class="section">
      <h2 class="h2 section-title">Çalıştığım teknolojiler</h2>
      <ul class="skills">
        <li v-for="skill in skills" :key="skill">{{ skill }}</li>
      </ul>
    </section>

    <section class="section cta surface">
      <div>
        <h2 class="h2">Birlikte çalışalım mı?</h2>
        <p class="muted mt-2">Bir soru ya da proje fikri varsa yaz.</p>
      </div>
      <router-link to="/contact" class="btn btn-primary">
        İletişime geç
      </router-link>
    </section>
  </div>
</template>

<script setup>
import AuthorAvatar from '@/components/AuthorAvatar.vue'

// Özgeçmiş içeriği veri olarak duruyor; şablon tekrarı olmadan tek yerden düzenlenir
const experience = [
  {
    company: 'Retfox / Türk Telekom',
    role: 'Yazılım ve Veri Uzmanı',
    period: 'Ağustos 2023 — Devam ediyor',
    items: [
      {
        heading: 'Otomasyon ve Veri Analizi',
        text: 'Türk Telekom bünyesinde fiziksel süreçleri otomatikleştirmeye yönelik çeşitli Python tabanlı projeler geliştirdim. Ping otomasyonu, hız testi otomasyonu, torrent izleme ve seri bağlantı kontrol sistemleri gibi uygulamalar hayata geçirildi. Ayrıca büyük veri setleri üzerinde kapsamlı analizler yaparak anlamlı içgörüler elde edilmesi sağlandı.',
      },
      {
        heading: 'Büyük Veri Yönetimi ve Tableau Raporlaması',
        text: 'Her 5 dakikada bir 10 milyon satırlık veri toplayan bir sistemde görev aldım. Bu veriler işlenerek Oracle veritabanına aktarıldı ve Tableau ile görselleştirildi. Büyük veri kümelerinin analizinde, yorumlanmasında ve yöneticilere sunulacak şekilde raporlanmasında aktif rol aldım. Edindiğim uzmanlık doğrultusunda geniş ekipler için Tableau eğitimleri verdim; Tableau, veri yapıları ve algoritma analizleri gibi konularda kapsamlı dokümantasyonlar hazırladım.',
      },
    ],
  },
  {
    company: 'Smartiys',
    role: 'Stajyer Öğrenci',
    period: 'Haziran — Eylül 2021 · Mart — Haziran 2023',
    items: [
      {
        heading: 'Smart ERP',
        text: '200 günlük staj sürecim boyunca Spring Boot kullanarak yeni RESTful API’ler geliştirdim. Arka uçta Spring Framework ile çalışırken, öncelikle HTML5, CSS ve JavaScript teknolojilerini kullandım; sonrasında web servis projelerinde Vue.js’e geçiş yaptım. Veritabanı olarak PostgreSQL tercih edildi. Ayrıca fabrika otomasyonlarına yönelik geliştirilen ERP yazılım çözümlerinin geliştirme ve destek süreçlerine katkı sağladım.',
      },
      {
        heading: 'Çevik (Agile) Geliştirme Süreci',
        text: 'Jira üzerinden kullanıcı hikâyelerini yöneterek, çevik yöntemle çalışan çok disiplinli bir ekiple sprint bazlı çalışma deneyimi kazandım. Proje geliştirme sürecinde takım içi koordinasyon, günlük stand-up ve sprint planlama toplantılarına aktif olarak katıldım.',
      },
    ],
  },
]

const skills = [
  'Java',
  'Spring Boot',
  'Vue.js',
  'JavaScript',
  'Python',
  'PostgreSQL',
  'Oracle',
  'Tableau',
  'Docker',
  'Git',
  'Jira / Agile',
]
</script>

<style scoped>
.narrow {
  max-width: 52rem;
}

.intro {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.role {
  font-weight: 500;
  color: var(--text-muted);
  margin: 0.4rem 0 0;
}

.about {
  margin-top: 2rem;
}

.section {
  margin-top: clamp(3rem, 7vw, 4.5rem);
}

.section-title {
  margin: 0 0 1.5rem;
}

/* --- Zaman çizelgesi --- */
.timeline {
  list-style: none;
  margin: 0;
  padding: 0 0 0 1.5rem;
  border-left: 1px solid var(--border-strong);
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.tl-item {
  position: relative;
}

.tl-dot {
  position: absolute;
  left: calc(-1.5rem - 5px);
  top: 1.85rem;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 0 4px var(--bg);
}

.tl-card {
  padding: 1.5rem;
}

.tl-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.tl-title {
  font-size: 1.15rem;
  font-weight: 600;
  color: var(--text);
  margin: 0;
}

.tl-role {
  font-size: 0.9375rem;
  color: var(--text-muted);
  margin: 0.25rem 0 0;
}

.tl-entry {
  margin-top: 1.25rem;
}

.tl-entry-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text);
  margin: 0 0 0.35rem;
}

.tl-entry-text {
  font-size: 0.9375rem;
  line-height: 1.7;
  color: var(--text-soft);
  margin: 0;
}

/* --- Eğitim --- */
.edu {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
}

.edu-icon {
  font-size: 1.75rem;
  color: var(--accent);
}

/* --- Teknolojiler --- */
.skills {
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin: 0;
  padding: 0;
}

.skills li {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-soft);
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 0.4rem 0.9rem;
  transition:
    border-color 0.2s ease,
    color 0.2s ease;
}

.skills li:hover {
  color: var(--accent);
  border-color: var(--accent);
}

/* --- Kapanış --- */
.cta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1.25rem;
  padding: 1.75rem 2rem;
}

.cta .muted {
  margin: 0;
}
</style>
