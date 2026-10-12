<template>
  <!-- AİA monogramı: A'nın ortasındaki gövde İ'yi, vurgu rengindeki nokta İ'nin noktasını oluşturur -->
  <svg class="mark" viewBox="0 0 100 100" aria-hidden="true" focusable="false">
    <path
      fill="currentColor"
      d="M44 30 L8 90 L17 90 L44 45 Z M56 30 L92 90 L83 90 L56 45 Z M44 30 H56 V90 H44 Z M30.2 68 H44 V73 H27.2 Z M69.8 68 H56 V73 H72.8 Z"
    />
    <circle class="mark-dot" cx="50" cy="16" r="7.5" />
  </svg>
</template>

<style scoped>
.mark {
  display: block;
  width: 1em;
  height: 1em;
  flex-shrink: 0;
}

.mark-dot {
  fill: var(--accent);
}
</style>
