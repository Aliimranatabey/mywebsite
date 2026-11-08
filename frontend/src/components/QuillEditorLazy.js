import { defineAsyncComponent } from 'vue'

/**
 * Yazı editörü, sadece ihtiyaç olduğunda yüklenir.
 *
 * Quill ve CSS'i ~250 KB; eskiden her ziyaretçi ana sayfada bunu indiriyordu.
 * Artık sadece yazı yazma/düzenleme ekranı açılınca iner.
 */
export default defineAsyncComponent(async () => {
  const [{ QuillEditor }] = await Promise.all([
    import('@vueup/vue-quill'),
    import('@vueup/vue-quill/dist/vue-quill.snow.css'),
  ])
  return QuillEditor
})
