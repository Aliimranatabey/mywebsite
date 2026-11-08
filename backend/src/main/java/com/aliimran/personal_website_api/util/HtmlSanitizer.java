package com.aliimran.personal_website_api.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
      // Spam bağlantılarının arama motorlarında işe yaramaması için
      .addEnforcedAttribute("a", "rel", "nofollow noopener noreferrer")
      .preserveRelativeLinks(false);

  /*
   * style ve class serbest kalırsa yazı, sitenin tamamını kaplayan sahte bir
   * giriş ekranı çizebilir (position:fixed) ya da sitenin kendi sınıflarını
   * taklit edebilir. Sadece Quill'in ürettikleri kalır: renk ve ql-* sınıfları.
   */
  private static final Pattern ALLOWED_CLASS = Pattern.compile("^ql-[a-z0-9-]+$");
  private static final Pattern ALLOWED_STYLE = Pattern.compile(
      "^(color|background-color)\\s*:\\s*(#[0-9a-f]{3,8}|rgba?\\([0-9.,\\s%]+\\)|[a-z]+)$");

  private HtmlSanitizer() {
  }

  public static String clean(String html) {
    if (html == null) {
      return null;
    }
    Document doc = Jsoup.parseBodyFragment(Jsoup.clean(html, SAFELIST));

    for (Element el : doc.body().select("[class]")) {
      String kept = el.classNames().stream()
          .filter(c -> ALLOWED_CLASS.matcher(c).matches())
          .collect(Collectors.joining(" "));
      setOrRemove(el, "class", kept);
    }
    for (Element el : doc.body().select("[style]")) {
      String kept = Arrays.stream(el.attr("style").split(";"))
          .map(d -> d.trim().toLowerCase(Locale.ROOT))
          .filter(d -> ALLOWED_STYLE.matcher(d).matches())
          .collect(Collectors.joining("; "));
      setOrRemove(el, "style", kept);
    }
    return doc.body().html();
  }

  private static void setOrRemove(Element el, String attr, String value) {
    if (value.isEmpty()) {
      el.removeAttr(attr);
    } else {
      el.attr(attr, value);
    }
  }
}
