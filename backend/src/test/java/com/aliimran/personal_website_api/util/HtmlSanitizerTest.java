package com.aliimran.personal_website_api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlSanitizerTest {

  @Test
  @DisplayName("script etiketi tamamen atılır")
  void stripsScriptTags() {
    String dirty = "<p>Merhaba</p><script>alert('xss')</script>";
    String clean = HtmlSanitizer.clean(dirty);

    assertFalse(clean.contains("script"));
    assertFalse(clean.contains("alert"));
    assertTrue(clean.contains("Merhaba"));
  }

  @Test
  @DisplayName("onerror/onclick gibi olay nitelikleri atılır")
  void stripsEventHandlers() {
    String clean = HtmlSanitizer.clean("<img src=\"x\" onerror=\"alert(1)\">");

    assertFalse(clean.contains("onerror"));
    assertFalse(clean.contains("alert"));
  }

  @Test
  @DisplayName("javascript: protokollü bağlantı atılır")
  void stripsJavascriptProtocol() {
    String clean = HtmlSanitizer.clean("<a href=\"javascript:alert(1)\">tık</a>");

    assertFalse(clean.contains("javascript:"));
  }

  @Test
  @DisplayName("iframe atılır")
  void stripsIframe() {
    assertFalse(HtmlSanitizer.clean("<iframe src=\"http://kotu.site\"></iframe>").contains("iframe"));
  }

  @Test
  @DisplayName("normal zengin metin korunur")
  void keepsLegitimateFormatting() {
    String dirty = "<h2>Başlık</h2><p><strong>kalın</strong> ve <em>italik</em></p>"
        + "<ul><li>madde</li></ul><a href=\"https://ornek.com\">bağlantı</a>";
    String clean = HtmlSanitizer.clean(dirty);

    assertTrue(clean.contains("<h2>"));
    assertTrue(clean.contains("<strong>"));
    assertTrue(clean.contains("<em>"));
    assertTrue(clean.contains("<li>"));
    assertTrue(clean.contains("https://ornek.com"));
  }

  @Test
  @DisplayName("null güvenli")
  void handlesNull() {
    assertNull(HtmlSanitizer.clean(null));
  }
}
