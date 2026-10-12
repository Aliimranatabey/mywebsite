package com.aliimran.personal_website_api.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Kullanıcının yazdığı HTML'i temizler.
 *
 * Neden gerekli: içerik QuillEditor'dan HTML olarak geliyor ve frontend'de
 * v-html ile basılıyor. Site çok kullanıcılı olduğu için temizlenmemiş HTML,
 * bir kullanıcının yazdığı script'in başka herkesin tarayıcısında çalışması
 * anlamına gelir (XSS).
 */
public final class HtmlSanitizer {

  /** Zengin metin için gereken etiketler; script/iframe/onclick gibi her şey atılır. */
  private static final Safelist SAFELIST = Safelist.relaxed()
      .addTags("figure", "figcaption", "span", "hr")
      .addAttributes("span", "class", "style")
      .addAttributes("p", "class", "style")
      .addAttributes("img", "src", "alt", "title", "width", "height")
      .addProtocols("img", "src", "http", "https", "data")
      .addAttributes(":all", "class")
      // rel=nofollow eklemek dış bağlantılarda SEO/güvenlik açısından iyi pratik
      .preserveRelativeLinks(false);

  private HtmlSanitizer() {
  }

  public static String clean(String html) {
    if (html == null) {
      return null;
    }
    return Jsoup.clean(html, SAFELIST);
  }
}
