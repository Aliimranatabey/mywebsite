<template>
  <header
    ref="headerEl"
    class="gnav"
    :class="{ 'gnav--open': panel || mobileOpen }"
    @mouseenter="cancelClose"
    @mouseleave="closeSoon"
    @focusout="onFocusOut"
    @keydown.esc="closeAll(true)"
  >
    <div class="gnav-bar glass">
      <div class="container gnav-inner">
        <router-link to="/" class="brand">
          <BrandMark class="brand-mark" />
          Ali İmran
        </router-link>

        <nav class="gnav-links" aria-label="Ana menü">
          <router-link
            v-for="item in items"
            :key="item.to"
            :to="item.to"
            class="gnav-link"
            active-class="is-active"
            :data-panel="item.panel"
            :aria-expanded="item.panel ? String(panel === item.panel) : undefined"
            @mouseenter="item.panel ? openSoon(item.panel) : closeSoon()"
            @focus="item.panel ? openPanel(item.panel) : (panel = null)"
            @keydown.down.prevent="item.panel && focusPanel(item.panel)"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <div class="gnav-actions">
          <button
            class="icon-btn"
            :aria-label="isDark ? 'Açık temaya geç' : 'Koyu temaya geç'"
            :title="isDark ? 'Açık tema' : 'Koyu tema'"
            @click="toggle"
          >
            <span
              class="mdi"
              :class="isDark ? 'mdi-white-balance-sunny' : 'mdi-moon-waning-crescent'"
              aria-hidden="true"
            ></span>
          </button>

          <v-menu v-if="auth.isLoggedIn" location="bottom end" offset="10">
            <template #activator="{ props }">
              <button v-bind="props" class="avatar-btn" :aria-label="`${auth.user.displayName} menüsü`">
                <AuthorAvatar :name="auth.user.displayName" size="1.6rem" />
              </button>
            </template>
            <div class="account glass">
              <p class="account-name">{{ auth.user.displayName }}</p>
              <p class="account-email">{{ auth.user.email }}</p>
              <hr class="rule" />
              <router-link to="/blog/new" class="account-item">Yeni yazı</router-link>
              <button class="account-item" @click="handleLogout">Çıkış yap</button>
            </div>
          </v-menu>

          <router-link v-else to="/login" class="gnav-link gnav-login">Giriş</router-link>

          <button
            class="burger"
            :class="{ 'burger--open': mobileOpen }"
            :aria-label="mobileOpen ? 'Menüyü kapat' : 'Menüyü aç'"
            :aria-expanded="String(mobileOpen)"
            @click="mobileOpen = !mobileOpen"
          >
            <span></span>
            <span></span>
          </button>
        </div>
      </div>

      <!-- Yazı okurken ne kadar ilerlendiği -->
      <div
        v-if="isReading"
        class="progress"
        :style="{ transform: `scaleX(${readProgress})` }"
        aria-hidden="true"
      ></div>
    </div>

    <!-- Masaüstü: üstüne gelince aşağı açılan panel -->
    <Transition name="flyout">
      <div v-if="panel" ref="flyoutEl" class="flyout glass" @mouseenter="cancelClose">
        <Transition name="swap" mode="out-in">
          <div v-if="panel === 'posts'" key="posts" class="container flyout-inner">
            <div class="fly-col fly-col--main" style="--i: 0">
              <p class="fly-label">Yazılar</p>
              <router-link to="/blog" class="fly-big">Tüm yazılar</router-link>
              <router-link :to="auth.isLoggedIn ? '/blog/new' : '/register'" class="fly-big">
                {{ auth.isLoggedIn ? 'Yeni yazı' : 'Yazmaya başla' }}
              </router-link>
            </div>

            <div class="fly-col" style="--i: 1">
              <p class="fly-label">Son yazılar</p>
              <p v-if="latest === null" class="fly-note">Yükleniyor…</p>
              <p v-else-if="latest.length === 0" class="fly-note">Henüz yazı yok.</p>
              <router-link v-for="post in latest" :key="post.id" :to="`/blog/${post.id}`" class="fly-post">
                <span class="fly-post-title">{{ post.title }}</span>
                <span class="fly-post-date">{{ formatDate(post.createdAt) }}</span>
              </router-link>
            </div>
          </div>

          <div v-else key="about" class="container flyout-inner">
            <div class="fly-col fly-col--main" style="--i: 0">
              <p class="fly-label">Ali İmran Atabey</p>
              <router-link to="/resume" class="fly-big">Hakkımda</router-link>
              <router-link to="/contact" class="fly-big">İletişim</router-link>
            </div>

            <div class="fly-col" style="--i: 1">
              <p class="fly-label">Özgeçmiş</p>
              <router-link to="/resume#deneyim" class="fly-small">Deneyim</router-link>
              <router-link to="/resume#egitim" class="fly-small">Eğitim</router-link>
              <router-link to="/resume#teknolojiler" class="fly-small">Teknolojiler</router-link>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>

    <!-- Mobil: tam ekran menü -->
    <Transition name="sheet">
      <nav v-if="mobileOpen" class="sheet" aria-label="Mobil menü">
        <div class="container sheet-inner">
          <router-link
            v-for="(item, i) in items"
            :key="item.to"
            :to="item.to"
            class="sheet-link"
            :style="{ '--i': i }"
          >
            {{ item.label }}
          </router-link>

          <hr class="rule sheet-rule" />

          <template v-if="auth.isLoggedIn">
            <router-link to="/blog/new" class="sheet-small" style="--i: 4">Yeni yazı</router-link>
            <button class="sheet-small" style="--i: 5" @click="handleLogout">Çıkış yap</button>
          </template>
          <template v-else>
            <router-link to="/login" class="sheet-small" style="--i: 4">Giriş yap</router-link>
            <router-link to="/register" class="sheet-small" style="--i: 5">Yazmaya başla</router-link>
          </template>
        </div>
      </nav>
    </Transition>

    <!-- Panel açıkken sayfa arkada bulanıklaşır. Header'ın içinde olduğu için
         imleç buraya inince header'dan çıkmış sayılmaz; kapatmayı kendisi tetikler. -->
    <Transition name="scrim">
      <div v-if="panel" class="scrim" aria-hidden="true" @mouseenter="closeSoon" @click="closeAll()"></div>
    </Transition>
  </header>

</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { useThemeMode } from '@/composables/useThemeMode'
import { formatDate } from '@/utils/text'
import AuthorAvatar from '@/components/AuthorAvatar.vue'
import BrandMark from '@/components/BrandMark.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const { isDark, toggle } = useThemeMode()

const items = [
  { label: 'Yazılar', to: '/blog', panel: 'posts' },
  { label: 'Hakkımda', to: '/resume', panel: 'about' },
  { label: 'İletişim', to: '/contact' },
]

const headerEl = ref(null)
const flyoutEl = ref(null)
const panel = ref(null) // 'posts' | 'about' | null
const mobileOpen = ref(false)

// --- Panel açma/kapama: imleç kısa süre dışarı kaçınca hemen kapanmasın ---
let openTimer = 0
let closeTimer = 0

function openPanel(name) {
  clearTimeout(closeTimer)
  panel.value = name
  if (name === 'posts') loadLatest()
}

function openSoon(name) {
  clearTimeout(closeTimer)
  clearTimeout(openTimer)
  // Panel zaten açıksa anında geçiş, değilse kısa bir niyet beklemesi
  if (panel.value) openPanel(name)
  else openTimer = setTimeout(() => openPanel(name), 90)
}

function closeSoon() {
  clearTimeout(openTimer)
  clearTimeout(closeTimer)
  closeTimer = setTimeout(() => (panel.value = null), 150)
}

function cancelClose() {
  clearTimeout(closeTimer)
}

function closeAll(returnFocus = false) {
  clearTimeout(openTimer)
  clearTimeout(closeTimer)
  const was = panel.value
  panel.value = null
  mobileOpen.value = false
  if (returnFocus && was) {
    headerEl.value?.querySelector(`[data-panel="${was}"]`)?.focus({ preventScroll: true })
  }
}

// Klavye: tetikleyicide aşağı ok paneldeki ilk bağlantıya götürür
async function focusPanel(name) {
  openPanel(name)
  await nextTick()
  flyoutEl.value?.querySelector('a')?.focus()
}

// Odak menünün dışına çıkınca panel kapanır
function onFocusOut(e) {
  if (!headerEl.value?.contains(e.relatedTarget)) panel.value = null
}

// --- "Yazılar" panelindeki son yazılar: ilk açılışta bir kez yüklenir ---
const latest = ref(null)
let latestRequested = false

async function loadLatest() {
  if (latestRequested) return
  latestRequested = true
  try {
    const { data } = await api.getBlogs(0, 4)
    latest.value = data.content ?? []
  } catch {
    latest.value = []
    latestRequested = false // bir sonraki açılışta yeniden denensin
  }
}

// Sayfa değişince her şey kapanır
watch(() => route.fullPath, () => closeAll())

// Mobil menü açıkken arkadaki sayfa kaymasın
watch(mobileOpen, (open) => {
  document.documentElement.style.overflow = open ? 'hidden' : ''
})

// --- Okuma ilerlemesi (sadece yazı detayında) ---
const isReading = computed(() => route.name === 'blog-detail')
const readProgress = ref(0)
let frame = 0

function measure() {
  frame = 0
  const max = document.documentElement.scrollHeight - window.innerHeight
  readProgress.value = max > 0 ? Math.min(1, window.scrollY / max) : 0
}

function onScroll() {
  if (isReading.value && !frame) frame = requestAnimationFrame(measure)
}

watch(isReading, (reading) => reading && nextTick(measure))

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  if (frame) cancelAnimationFrame(frame)
  clearTimeout(openTimer)
  clearTimeout(closeTimer)
  document.documentElement.style.overflow = ''
})

async function handleLogout() {
  await auth.logout()
  closeAll()
  router.push('/')
}
</script>

<style scoped>
/* ============================================================
   Çubuk
   ============================================================ */

.gnav {
  position: sticky;
  top: 0;
  z-index: 1000;
  font-size: 0.8125rem;
}

.gnav-bar {
  position: relative;
  height: var(--nav-h);
  transition: background-color 0.3s ease;
}

/* Panel ya da mobil menü açıkken çubuk panelle tek parça görünsün */
.gnav--open .gnav-bar {
  background: var(--glass-bg-strong);
  box-shadow: none;
}

.gnav-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 1.5rem;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-family: var(--font-display);
  font-size: 1.0625rem;
  font-weight: 500;
  letter-spacing: -0.01em;
  color: var(--text);
  white-space: nowrap;
}

.brand:hover {
  text-decoration: none;
}

/* Nokta dahil işaret, yazının büyük harf yüksekliğinden biraz uzun dursun */
.brand-mark {
  font-size: 1.45rem;
  margin-top: -0.2rem;
}

.gnav-links {
  display: flex;
  justify-content: center;
  gap: 0.25rem;
  flex: 1;
}

.gnav-link {
  display: inline-flex;
  align-items: center;
  height: var(--nav-h);
  padding: 0 0.85rem;
  color: var(--text);
  opacity: 0.8;
  transition: opacity 0.2s ease;
}

.gnav-link:hover,
.gnav-link.is-active,
.gnav-link[aria-expanded='true'] {
  opacity: 1;
  text-decoration: none;
}

/* Aktif sayfa kalınlaşmaz (yanındakiler kaymasın); ince bir alt çizgiyle belli olur */
.gnav-link.is-active {
  box-shadow: inset 0 -1.5px 0 var(--text);
}

.gnav-actions {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.icon-btn,
.avatar-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: none;
  color: var(--text);
  opacity: 0.8;
  font-size: 1.05rem;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.icon-btn:hover,
.avatar-btn:hover {
  opacity: 1;
}

/* İki çizgi; açılınca çarpıya döner */
.burger {
  display: none;
  position: relative;
  width: 2.25rem;
  height: 2.25rem;
  padding: 0;
  border: 0;
  background: none;
  cursor: pointer;
}

.burger span {
  position: absolute;
  left: 0.6rem;
  right: 0.6rem;
  height: 1.5px;
  border-radius: 1px;
  background: var(--text);
  transition:
    transform 0.35s var(--ease),
    top 0.35s var(--ease);
}

.burger span:first-child {
  top: 0.85rem;
}

.burger span:last-child {
  top: 1.3rem;
}

.burger--open span:first-child {
  top: 1.08rem;
  transform: rotate(45deg);
}

.burger--open span:last-child {
  top: 1.08rem;
  transform: rotate(-45deg);
}

.progress {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 2px;
  background: var(--accent);
  transform-origin: left;
  transform: scaleX(0);
}

/* ============================================================
   Hesap menüsü
   ============================================================ */

.account {
  min-width: 14rem;
  padding: 0.5rem;
  border-radius: var(--radius);
  background: var(--glass-bg-strong);
  box-shadow: var(--shadow-lg);
}

.account-name {
  font-weight: 600;
  color: var(--text);
  margin: 0;
  padding: 0.5rem 0.75rem 0;
}

.account-email {
  font-size: 0.8125rem;
  color: var(--text-muted);
  margin: 0;
  padding: 0 0.75rem 0.5rem;
}

.rule {
  border: 0;
  border-top: 1px solid var(--border);
  margin: 0.35rem 0;
}

.account-item {
  display: block;
  width: 100%;
  text-align: left;
  font-family: var(--font);
  font-size: 0.9375rem;
  color: var(--text);
  background: none;
  border: 0;
  border-radius: var(--radius-sm);
  padding: 0.55rem 0.75rem;
  cursor: pointer;
}

.account-item:hover {
  text-decoration: none;
  background: var(--border);
}

/* ============================================================
   Açılır panel
   ============================================================ */

.flyout {
  position: absolute;
  top: var(--nav-h);
  left: 0;
  right: 0;
  background: var(--glass-bg-strong);
  box-shadow: 0 1px 0 var(--glass-shade);
  overflow: hidden;
}

.flyout-inner {
  display: flex;
  gap: clamp(2.5rem, 7vw, 6rem);
  min-height: 15rem;
  padding-top: 2.25rem;
  padding-bottom: 3rem;
}

.fly-col {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
  min-width: 0;
  animation: fly-in 0.45s var(--ease) both;
  animation-delay: calc(var(--i) * 50ms + 60ms);
}

/* Sabit genişlik: iki panelde ikinci sütun aynı yerden başlasın */
.fly-col--main {
  flex: 0 0 14rem;
}

.fly-col:not(.fly-col--main) {
  flex: 1;
  max-width: 28rem;
}

.fly-label {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin: 0 0 0.35rem;
}

.fly-big {
  font-family: var(--font-display);
  font-size: 1.6rem;
  font-weight: 450;
  line-height: 1.2;
  letter-spacing: -0.015em;
  color: var(--text);
}

.fly-small,
.fly-post-title {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text);
}

.fly-big:hover,
.fly-small:hover,
.fly-post:hover .fly-post-title {
  color: var(--accent);
  text-decoration: none;
}

.fly-post {
  display: flex;
  flex-direction: column;
  max-width: 100%;
  padding: 0.15rem 0;
}

.fly-post:hover {
  text-decoration: none;
}

.fly-post-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fly-post-date,
.fly-note {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin: 0;
}

@keyframes fly-in {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
}

/* Panel yukarıdan aşağı açılır */
.flyout-enter-active,
.flyout-leave-active {
  transition: clip-path 0.4s var(--ease);
}

.flyout-leave-active {
  transition-duration: 0.25s;
}

.flyout-enter-from,
.flyout-leave-to {
  clip-path: inset(0 0 100% 0);
}

.flyout-enter-to,
.flyout-leave-from {
  clip-path: inset(0 0 0 0);
}

/* Paneller arası geçiş */
.swap-enter-active,
.swap-leave-active {
  transition: opacity 0.15s ease;
}

.swap-enter-from,
.swap-leave-to {
  opacity: 0;
}

.scrim {
  position: fixed;
  inset: 0;
  z-index: -1; /* header'ın içinde: çubuğun ve panelin altında, sayfanın üstünde */
  background: var(--scrim);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

@media (prefers-reduced-transparency: reduce) {
  .scrim {
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
  }
}

.scrim-enter-active,
.scrim-leave-active {
  transition: opacity 0.35s ease;
}

.scrim-enter-from,
.scrim-leave-to {
  opacity: 0;
}

/* ============================================================
   Mobil
   ============================================================ */

.sheet {
  position: fixed;
  top: var(--nav-h);
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--glass-bg-strong);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  overflow-y: auto;
}

.sheet-inner {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding-top: 1.5rem;
  padding-bottom: 3rem;
}

.sheet-link,
.sheet-small {
  animation: fly-in 0.45s var(--ease) both;
  animation-delay: calc(var(--i) * 40ms + 80ms);
}

.sheet-link {
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 450;
  letter-spacing: -0.02em;
  line-height: 1.5;
  color: var(--text);
}

.sheet-rule {
  align-self: stretch;
  margin: 1.25rem 0 1rem;
}

.sheet-small {
  font-family: var(--font);
  font-size: 1.0625rem;
  line-height: 2.4;
  color: var(--text-muted);
  background: none;
  border: 0;
  padding: 0;
  cursor: pointer;
}

.sheet-link:hover,
.sheet-small:hover {
  color: var(--accent);
  text-decoration: none;
}

.sheet-enter-active,
.sheet-leave-active {
  transition:
    opacity 0.3s ease,
    clip-path 0.4s var(--ease);
}

.sheet-enter-from,
.sheet-leave-to {
  opacity: 0;
  clip-path: inset(0 0 100% 0);
}

.sheet-enter-to,
.sheet-leave-from {
  clip-path: inset(0 0 0 0);
}

@media (max-width: 833px) {
  .gnav-links,
  .gnav-login {
    display: none;
  }

  .gnav-actions {
    margin-left: auto;
  }

  .burger {
    display: inline-block;
  }
}

@media (min-width: 834px) {
  .sheet {
    display: none;
  }
}
</style>
