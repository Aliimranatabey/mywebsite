<template>
  <svg class="app-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path :d="path" />
  </svg>
</template>

<script setup>
/**
 * Material Design ikonu, SVG olarak.
 *
 * Eskiden @mdi/font kullanılıyordu: 7000+ ikonluk font ve ~300 KB CSS, sadece
 * 8 ikon için. Burada sadece kullandıklarımız pakete girer. Yeni ikon lazımsa
 * https://pictogrammers.com/library/mdi/ adresinden adını bul, aşağıya ekle.
 */
import { computed } from 'vue'
import {
  mdiArrowLeft,
  mdiArrowRight,
  mdiImagePlusOutline,
  mdiMoonWaningCrescent,
  mdiPencilOutline,
  mdiSchoolOutline,
  mdiTrashCanOutline,
  mdiWhiteBalanceSunny,
} from '@mdi/js'

const ICONS = {
  'arrow-left': mdiArrowLeft,
  'arrow-right': mdiArrowRight,
  'image-plus-outline': mdiImagePlusOutline,
  'moon-waning-crescent': mdiMoonWaningCrescent,
  'pencil-outline': mdiPencilOutline,
  'school-outline': mdiSchoolOutline,
  'trash-can-outline': mdiTrashCanOutline,
  'white-balance-sunny': mdiWhiteBalanceSunny,
}

const props = defineProps({
  name: { type: String, required: true },
})

const path = computed(() => ICONS[props.name] ?? '')
</script>

<style scoped>
/* Font ikonu gibi davransın: yazı boyutunu ve rengini takip etsin */
.app-icon {
  display: inline-block;
  width: 1em;
  height: 1em;
  fill: currentColor;
  vertical-align: -0.125em;
  flex-shrink: 0;
}
</style>
